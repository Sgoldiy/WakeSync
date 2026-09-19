package com.social.wakesync.feature.games

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Android implementation: synthesized tones (no bundled audio assets needed)
 * through ToneGenerator on the notification stream, plus short haptic
 * reinforcement. Every call is defensive — a missing vibrator or audio focus
 * problem must never crash an alarm unlock.
 */
    actual object GameSounds {

    private val appContext: Context? by lazy {
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val current = activityThread.getMethod("currentApplication").invoke(null) as? Context
            current
        } catch (_: Throwable) {
            null
        }
    }

    private fun tone(toneType: Int, durationMs: Int, volume: Int = 80) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, volume)
            tg.startTone(toneType, durationMs)
            // ToneGenerator needs a beat before release() or the tone gets cut.
            Thread {
                try {
                    Thread.sleep(durationMs + 120L)
                    tg.release()
                } catch (_: InterruptedException) {
                    tg.release()
                }
            }.start()
        } catch (_: Throwable) {
            // No audio available (e.g. alarm stream busy) — haptics still fire.
        }
    }

    private fun vibrate(pattern: LongArray) {
        try {
            val vibrator: Vibrator? = appContext?.let { ctx ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                        ?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
            }
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (_: Throwable) {
        }
    }

    actual fun chime() {
        // Bright ascending two-note: C6 then E6.
        tone(ToneGenerator.TONE_PROP_BEEP2, 90)
        vibrate(longArrayOf(0, 25))
    }

    actual fun buzz() {
        // Low harsh double-buzz.
        tone(ToneGenerator.TONE_SUP_ERROR, 160)
        vibrate(longArrayOf(0, 60, 40, 60))
    }

    actual fun spark() {
        // Sparkle: quick triple beep rise.
        tone(ToneGenerator.TONE_PROP_ACK, 60)
        vibrate(longArrayOf(0, 20, 30, 20, 30, 40))
    }

    actual fun tick() {
        // Tiny neutral tick — watch-phase heartbeat.
        tone(ToneGenerator.TONE_PROP_BEEP, 40, volume = 55)
        vibrate(longArrayOf(0, 12))
    }

    actual fun chimeRising(comboStep: Int) {
        // ToneGenerator pitches are preset tones; we approximate the rising
        // scale by stepping through increasingly bright tone qualities and
        // shortening the beep — the ear clearly hears the climb.
        val toneType = when (comboStep) {
            in 1..2 -> ToneGenerator.TONE_PROP_BEEP
            in 3..4 -> ToneGenerator.TONE_PROP_BEEP2
            in 5..6 -> ToneGenerator.TONE_PROP_ACK
            in 7..8 -> ToneGenerator.TONE_PROP_BEEP2
            in 9..10 -> ToneGenerator.TONE_CDMA_CONFIRM
            else -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
        }
        val vol = (55 + comboStep * 2).coerceAtMost(90)
        tone(toneType, (70 - comboStep * 2).coerceAtLeast(40), volume = vol)
        vibrate(longArrayOf(0, (15 + comboStep).toLong()))
    }
}
