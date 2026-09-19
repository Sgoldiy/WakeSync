package com.social.wakesync.feature.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val WINS_NEEDED = 3
private const val RESULT_SHOW_MS = 900L
private const val TOO_EARLY_SHOW_MS = 800L

/**
 * Reaction Rush — a go/no-go reaction-time wake-up challenge.
 *
 * One large pad: red = WAIT (hands off), green = TAP NOW. Three successful
 * reactions win; each round's red phase uses a fresh UNPREDICTABLE delay
 * (round 1: 1–3s, round 2: 1–2.5s, round 3: 1–2s — never fixed).
 *
 * Reaction time = monotonic nanos between green appearing and the tap, shown
 * in milliseconds. Early tap fails the attempt ("Too Early!"), keeps the same
 * round, and restarts with a new random delay — the timing can't be learned.
 *
 * Cheat/bug guards: input is gated by explicit phase state (no double-count
 * after success, no taps during result display), timers are coroutine-keyed
 * (auto-cancelled on completion/dispose), and all measurement is monotonic.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun ReactionRushGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var round by remember { mutableIntStateOf(1) } // 1..3
    var wins by remember { mutableIntStateOf(0) }
    var isGreen by remember { mutableStateOf(false) }
    var showTooEarly by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf<Int?>(null) } // last reaction in ms
    var isComplete by remember { mutableStateOf(false) }
    var bestMs by remember { mutableIntStateOf(Int.MAX_VALUE) }
    val reactions = remember { mutableStateOf(listOf<Int>()) }

    // Monotonic timestamps (nanos): green onset + tap.
    var greenAtNanos by remember { mutableLongStateOf(0L) }
    var tapAtNanos by remember { mutableLongStateOf(0L) }

    val haptic = LocalHapticFeedback.current

    // Round delay ranges (ms) — shrink each round, always randomized.
    fun delayRange(): LongArray = when (round) {
        1 -> longArrayOf(1000, 3000)
        2 -> longArrayOf(1000, 2500)
        else -> longArrayOf(1000, 2000)
    }

    // ── Red phase: randomized wait, then flip green. ─────────────────────────
    // Keyed on (round, wins, tooEarly/restart token) via `greenFlipKey` so an
    // early tap produces a brand-new random delay — never learnable.
    var greenFlipKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(greenFlipKey) {
        if (isComplete || isGreen || showTooEarly || showResult != null) return@LaunchedEffect
        val (lo, hi) = delayRange().let { it[0] to it[1] }
        val wait = Random.nextLong(lo, hi + 1)
        delay(wait)
        greenAtNanos = monotonicNanos() // monotonic onset
        isGreen = true
        GameSounds.tick()
    }

    // Feedback decay → next round / restart, cleanly sequenced.
    LaunchedEffect(showTooEarly) {
        if (showTooEarly) {
            delay(TOO_EARLY_SHOW_MS)
            showTooEarly = false
            greenFlipKey++ // new random delay for the retried round
        }
    }

    LaunchedEffect(showResult) {
        if (showResult != null) {
            delay(RESULT_SHOW_MS)
            showResult = null
            if (wins < WINS_NEEDED) {
                round++
                greenFlipKey++
            }
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onPadTapped() {
        if (isComplete || showTooEarly || showResult != null) return // no double-count
        if (isGreen) {
            // Reaction = tap minus green onset, monotonic, rounded to ms.
            tapAtNanos = monotonicNanos()
            val reactionMs = ((tapAtNanos - greenAtNanos) / 1_000_000L).toInt().coerceAtLeast(1)
            isGreen = false
            wins++
            reactions.value = reactions.value + reactionMs
            if (reactionMs < bestMs) bestMs = reactionMs

            GameCombo.onCorrectTap()
            if (wins >= WINS_NEEDED) {
                isComplete = true
                GameCombo.reset()
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                showResult = reactionMs
                GameSounds.chime()
            }
        } else {
            // Early tap: fail the attempt, keep the round, randomize again.
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
            isGreen = false // kill any in-flight green flip state
            showTooEarly = true
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = "Reaction Rush",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Wait for GREEN — then smash it.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    showTooEarly -> GamePhase.WRONG
                    isGreen -> GamePhase.ACT
                    else -> GamePhase.WATCH
                },
                text = when {
                    showTooEarly -> "⚡ Too Early! — wait for green"
                    isGreen -> "🟢 TAP NOW! TAP NOW!"
                    else -> "🔴 WAIT... hands off"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = "Round $round / $WINS_NEEDED    ·    Best: ${if (bestMs == Int.MAX_VALUE) "—" else "${bestMs}ms"}",
                phase = GamePhase.INFO,
                titleFamily = titleFamily
            )

            // Win progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(WINS_NEEDED) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i < wins -> AppColorPalette.WinGreen
                                    i == wins && !showTooEarly -> AppColorPalette.GoldPremium
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // Last reaction readout / too-early flash
            AnimatedContent(
                targetState = showResult to showTooEarly,
                transitionSpec = { (fadeIn(tween(140)) + scaleIn(initialScale = 0.9f, animationSpec = tween(140))) togetherWith fadeOut(tween(90)) },
                label = "reactionReadout"
            ) { (result, tooEarly) ->
                when {
                    tooEarly -> Text(
                        text = "Too Early!",
                        color = AppColorPalette.LossRed,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily
                    )
                    result != null -> Text(
                        text = "$result ms",
                        color = AppColorPalette.WinGreen,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily
                    )
                    else -> Text(
                        text = " ",
                        fontSize = 24.sp
                    )
                }
            }

            // ── The giant pad ──────────────────────────────────────────────────
            val padInteraction = remember { MutableInteractionSource() }
            val padPressed by padInteraction.collectIsPressedAsState()
            val padScale by animateFloatAsState(
                if (padPressed) 0.94f else 1f,
                spring(dampingRatio = 0.6f, stiffness = 700f),
                label = "padScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
                    .scale(padScale)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        when {
                            showTooEarly -> AppColorPalette.LossRed.copy(alpha = 0.35f)
                            isGreen -> AppColorPalette.WinGreen.copy(alpha = 0.85f)
                            else -> AppColorPalette.LossRed.copy(alpha = 0.16f)
                        }
                    )
                    .border(
                        3.dp,
                        when {
                            showTooEarly -> AppColorPalette.LossRed
                            isGreen -> AppColorPalette.WinGreen
                            else -> AppColorPalette.LossRed.copy(alpha = 0.5f)
                        },
                        RoundedCornerShape(28.dp)
                    )
                    .clickable(interactionSource = padInteraction, indication = null) { onPadTapped() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when {
                            showTooEarly -> "⚡"
                            isGreen -> "🟢"
                            else -> "🔴"
                        },
                        fontSize = 56.sp
                    )
                    Text(
                        text = when {
                            showTooEarly -> "TOO EARLY"
                            isGreen -> "TAP NOW!"
                            else -> "WAIT..."
                        },
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // First-round coaching
            if (wins == 0 && !showTooEarly && showResult == null && round == 1 && !isGreen) {
                HowToHint(
                    text = "How to play: red = don't touch. The moment it flips green — smash it. Fastest of 3 wins.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Reaction Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Best: $bestMs ms ⚡ · ${reactions.value.joinToString(" · ") { "${it}ms" }}",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * restarts all 3 reactions with fresh random delays. Victory maps to [onSuccess].
 */
@Composable
fun ReactionRushGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        ReactionRushGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
