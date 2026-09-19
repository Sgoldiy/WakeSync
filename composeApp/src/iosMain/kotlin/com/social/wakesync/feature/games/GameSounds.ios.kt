package com.social.wakesync.feature.games

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.isOtherAudioPlaying
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/**
 * iOS implementation: real synthesized audio — every effect is generated as a
 * small in-memory WAV (pure math, no bundled assets) and played through
 * [AVAudioPlayer] on the Playback audio session, so sounds are audible even
 * with the ringer switch off (critical for a wake-up game). Haptics fire
 * alongside, matching the same grammar as Android.
 *
 * Effects:
 *   chime       — bright ascending two-note chime (ACT phase)
 *   buzz        — low detuned harsh buzz (WRONG phase)
 *   spark       — quick sparkle arpeggio (win)
 *   tick        — tiny neutral click (correct tap mid-flow)
 *   chimeRising — one semitone higher per combo step (pitch = 440·2^(n/12))
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
actual object GameSounds {

    private const val SAMPLE_RATE = 22050

    private val impactLight = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val impactHeavy = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    private val notification = UINotificationFeedbackGenerator()

    /**
     * Playback category: audible even on silent/ringer-off (critical for a
     * wake-up game) — set once, best-effort. Sessions must also be activated
     * before audio will sound, so [play] re-activates defensively.
     */
    private val sessionReady: Boolean by lazy {
        try {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
            true
        } catch (_: Throwable) {
            false
        }
    }

    // AVAudioPlayer must be retained while playing — keep the last few alive.
    private val players = mutableListOf<AVAudioPlayer>()

    // ── WAV synthesis (pure Kotlin — no assets) ─────────────────────────────

    private fun wavBytes(samples: ShortArray): ByteArray {
        val dataSize = samples.size * 2
        val out = ByteArray(44 + dataSize)
        fun le32(off: Int, v: Int) {
            out[off] = v.toByte(); out[off + 1] = (v shr 8).toByte()
            out[off + 2] = (v shr 16).toByte(); out[off + 3] = (v shr 24).toByte()
        }
        fun le16(off: Int, v: Int) { out[off] = v.toByte(); out[off + 1] = (v shr 8).toByte() }
        "RIFF".forEachIndexed { i, c -> out[i] = c.code.toByte() }
        le32(4, 36 + dataSize)
        "WAVE".forEachIndexed { i, c -> out[8 + i] = c.code.toByte() }
        "fmt ".forEachIndexed { i, c -> out[12 + i] = c.code.toByte() }
        le32(16, 16) // PCM chunk
        le16(20, 1)  // PCM format
        le16(22, 1)  // mono
        le32(24, SAMPLE_RATE)
        le32(28, SAMPLE_RATE * 2) // byte rate
        le16(32, 2)  // block align
        le16(34, 16) // bits per sample
        "data".forEachIndexed { i, c -> out[36 + i] = c.code.toByte() }
        le32(40, dataSize)
        for (i in samples.indices) {
            val s = samples[i].toInt()
            out[44 + i * 2] = s.toByte()
            out[45 + i * 2] = (s shr 8).toByte()
        }
        return out
    }

    /** One or more sine notes rendered back-to-back. Notes are (freqHz, durationMs). */
    private fun notesWav(notes: List<Pair<Double, Long>>, volume: Double, decay: Boolean = true): ByteArray {
        val chunks = notes.map { (freq, ms) ->
            val n = (SAMPLE_RATE * ms / 1000).toInt()
            ShortArray(n) { i ->
                val t = i.toDouble() / SAMPLE_RATE
                val env = if (decay) 1.0 - i.toDouble() / n else 1.0
                (sin(2 * PI * freq * t) * env * volume * Short.MAX_VALUE)
                    .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        val all = chunks.fold(ShortArray(0)) { acc, c -> acc + c }
        return wavBytes(all)
    }

    /** Detuned low growl for the buzz — two close sines beating against each other. */
    private fun buzzWav(): ByteArray {
        val ms = 280L
        val n = (SAMPLE_RATE * ms / 1000).toInt()
        val samples = ShortArray(n) { i ->
            val t = i.toDouble() / SAMPLE_RATE
            val beat = (sin(2 * PI * 85.0 * t) + sin(2 * PI * 97.0 * t) + sin(2 * PI * 51.0 * t)) / 3.0
            val env = 0.6 + 0.4 * (1.0 - i.toDouble() / n)
            (beat * env * 0.9 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return wavBytes(samples)
    }

    // ── Cached sound data ───────────────────────────────────────────────────

    private val chimeWav by lazy { notesWav(listOf(880.0 to 110L, 1174.66 to 150L), volume = 0.55) }
    private val buzzWavData by lazy { buzzWav() }
    private val sparkWav by lazy {
        notesWav(listOf(1046.5 to 70L, 1318.5 to 70L, 1567.98 to 70L, 2093.0 to 130L), volume = 0.5)
    }
    private val tickWav by lazy { notesWav(listOf(1500.0 to 22L), volume = 0.28) }
    private val risingWavs: List<ByteArray> = (1..GameCombo.MAX).map { step ->
        val freq = 440.0 * 2.0.pow(step / 12.0)
        notesWav(listOf(freq to 130L), volume = 0.5)
    }

    // ── Playback ────────────────────────────────────────────────────────────

    private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    private fun play(wav: ByteArray) {
        try {
            if (!sessionReady) return
            val session = AVAudioSession.sharedInstance()
            if (!session.isOtherAudioPlaying()) {
                session.setActive(true, error = null) // best-effort; may fail during calls
            }
            val player = AVAudioPlayer(data = wav.toNSData(), fileTypeHint = null, error = null) ?: return
            player.volume = 1.0f
            player.prepareToPlay()
            player.play()
            players.removeAll { !it.isPlaying() }
            players.add(player)
            if (players.size > 8) players.removeAt(0)
        } catch (_: Throwable) {
            // Audio unavailable — haptics still carry the phase signal.
        }
    }

    // ── Phase effects ───────────────────────────────────────────────────────

    actual fun chime() {
        notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        play(chimeWav)
    }

    actual fun buzz() {
        notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        play(buzzWavData)
    }

    actual fun spark() {
        notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        impactHeavy.impactOccurred()
        play(sparkWav)
    }

    actual fun tick() {
        impactLight.impactOccurred()
        play(tickWav)
    }

    actual fun chimeRising(comboStep: Int) {
        val step = comboStep.coerceIn(1, GameCombo.MAX)
        impactLight.impactOccurred()
        play(risingWavs[step - 1])
    }
}
