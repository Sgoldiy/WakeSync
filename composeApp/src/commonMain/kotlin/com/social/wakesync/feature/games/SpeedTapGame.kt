package com.social.wakesync.feature.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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

private const val TAP_MAX = 9
private const val CORRECT_HIGHLIGHT_MS = 220L

/**
 * Speed Tap 1–9 — an ascending visual-recognition wake-up challenge.
 *
 * Numbers 1–9 sit shuffled in a 3×3 grid; the player taps them in order
 * 1 → 9 as fast as possible. The timer starts on the FIRST VALID tap and stops
 * when 9 is tapped (measured with the monotonic system clock, 10ms resolution).
 *
 * Wrong tap: target resets to 1, the board reshuffles, the timer restarts —
 * a clean slate, exactly as specced.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun SpeedTapGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var numbers by remember { mutableStateOf(shuffledGrid()) } // index = cell, value = number
    var nextExpected by remember { mutableIntStateOf(1) }
    var isError by remember { mutableStateOf(false) } // wrong-tap flash
    var justTapped by remember { mutableIntStateOf(-1) } // correct-tap highlight
    var isComplete by remember { mutableStateOf(false) }

    // Timer: start at first valid tap, stop at 9. Monotonic nanos → ms.
    var runningSinceNanos by remember { mutableLongStateOf(0L) }
    var elapsedMs by remember { mutableIntStateOf(0) }

    val haptic = LocalHapticFeedback.current

    // Live timer loop — ticks only while a run is in progress; cancelled on
    // completion/dispose automatically (LaunchedEffect keyed on run state).
    LaunchedEffect(runningSinceNanos, nextExpected, isComplete) {
        if (runningSinceNanos == 0L || isComplete || nextExpected > TAP_MAX) return@LaunchedEffect
        while (nextExpected <= TAP_MAX && !isComplete) {
            elapsedMs = ((monotonicNanos() - runningSinceNanos) / 1_000_000L).toInt()
            delay(10) // 10ms resolution — smooth centiseconds display
        }
    }

    // Correct-tap highlight decay.
    LaunchedEffect(justTapped) {
        if (justTapped != -1) {
            delay(CORRECT_HIGHLIGHT_MS)
            justTapped = -1
        }
    }

    // Wrong-tap feedback decay.
    LaunchedEffect(isError) {
        if (isError) {
            delay(550)
            isError = false
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onNumberTapped(number: Int) {
        if (isComplete || isError) return
        if (number == nextExpected) {
            GameCombo.onCorrectTap() // rising-pitch streak
            justTapped = number
            if (number == 1) {
                // First valid tap starts the clock (fresh run).
                runningSinceNanos = monotonicNanos()
                elapsedMs = 0
            }
            if (number >= TAP_MAX) {
                // Stop timing with the final tap.
                elapsedMs = ((monotonicNanos() - runningSinceNanos) / 1_000_000L).toInt()
                isComplete = true
                GameCombo.reset()
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                GameSounds.chime()
                nextExpected++
            }
        } else {
            // Wrong tap: full reset — target 1, new board, timer restarts.
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
            isError = true
            numbers = shuffledGrid()
            nextExpected = 1
            runningSinceNanos = 0L
            elapsedMs = 0
        }
    }

    val secondsDisplay = remember(elapsedMs) { fmt2(elapsedMs / 1000.0) }

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
            text = "Speed Tap 1–9",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap the numbers in ascending order.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    isError -> GamePhase.WRONG
                    else -> GamePhase.ACT
                },
                text = when {
                    isError -> "❌ START AGAIN — board reshuffled!"
                    nextExpected == 1 && runningSinceNanos == 0L -> "✋ Tap 1 to start the clock"
                    else -> "✋ Find $nextExpected"
                },
                interFamily = interFamily
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepCounter(
                    text = "Find: $nextExpected    ·    ⏱ ${secondsDisplay}s",
                    phase = GamePhase.INFO,
                    titleFamily = titleFamily
                )
            }

            // Progress dots: 9 numbers.
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(TAP_MAX) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i + 1 < nextExpected -> AppColorPalette.WinGreen
                                    i + 1 == nextExpected -> AppColorPalette.GoldPremium
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // ── 3×3 grid of large number buttons ──────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (col in 0 until 3) {
                            val number = numbers[row * 3 + col]
                            SpeedTapCell(
                                number = number,
                                isCleared = number < nextExpected,
                                isNext = number == nextExpected,
                                isHighlighted = justTapped == number,
                                isErrorFlash = isError,
                                onClick = { onNumberTapped(number) },
                                titleFamily = titleFamily,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // First-run coaching
            if (nextExpected == 1 && runningSinceNanos == 0L && !isError) {
                HowToHint(
                    text = "How to play: tap 1 → 9 in order. Timer starts on your first tap — wrong tap reshuffles everything!",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Speed Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Time: ${fmt2(elapsedMs / 1000.0)}s ⚡",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * One number cell. Cleared = green-tinted ✓, next = cyan-ringed, others =
 * neutral. Wrong-tap flashes the whole grid red via isErrorFlash.
 */
@Composable
private fun SpeedTapCell(
    number: Int,
    isCleared: Boolean,
    isNext: Boolean,
    isHighlighted: Boolean,
    isErrorFlash: Boolean,
    onClick: () -> Unit,
    titleFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        isHighlighted -> 1.1f
        isErrorFlash -> 0.95f
        pressed -> 0.92f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 700f), label = "tapCell$number")

    val fillColor = when {
        isErrorFlash -> AppColorPalette.LossRed.copy(alpha = 0.25f)
        isHighlighted -> AppColorPalette.WinGreen.copy(alpha = 0.35f)
        isCleared -> AppColorPalette.WinGreen.copy(alpha = 0.18f)
        isNext -> AppColorPalette.CyanCta.copy(alpha = 0.1f)
        else -> AppColorPalette.Surface
    }
    val borderColor = when {
        isErrorFlash -> AppColorPalette.LossRed
        isHighlighted || isCleared -> AppColorPalette.WinGreen
        isNext -> AppColorPalette.CyanCta.copy(alpha = 0.6f)
        else -> Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(fillColor)
            .border(
                if (isNext || isCleared || isHighlighted) 2.dp else 1.5.dp,
                borderColor,
                RoundedCornerShape(18.dp)
            )
            .clickable(
                enabled = !isCleared, // already-tapped cells are dead
                interactionSource = iSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isCleared || isHighlighted) {
            Text(
                text = "✓",
                color = AppColorPalette.WinGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily
            )
        } else {
            Text(
                text = number.toString(),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily
            )
        }
    }
}

/** Shuffled 1–9 layout, never accidentally in ascending order. */
private fun shuffledGrid(): List<Int> {
    var layout: List<Int>
    var guard = 0
    do {
        layout = (1..TAP_MAX).shuffled(Random)
        guard++
    } while (layout == (1..TAP_MAX).toList() && guard < 20)
    return layout
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * reshuffles the grid and resets the clock. Victory maps to [onSuccess].
 */
@Composable
fun SpeedTapGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        SpeedTapGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
