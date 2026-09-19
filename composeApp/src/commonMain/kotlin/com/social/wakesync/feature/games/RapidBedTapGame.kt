package com.social.wakesync.feature.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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

private const val SPRINT_TARGET = 20
private const val WRONG_FLASH_MS = 450L
private const val CORRECT_FLASH_MS = 160L

/**
 * Thumb Sprint — a rapid alternating-thumb wake-up challenge.
 *
 * Two giant pads (LEFT / RIGHT); 20 alternating taps win. The starting side is
 * RANDOM each game. The expected pad pulses with a subtle breathing animation
 * and an explicit "NEXT: RIGHT" text indicator — never color alone.
 *
 * Forgiving by default: a wrong-side tap is NOT counted, buzzes briefly,
 * points back to the correct side, and keeps all progress. Nothing resets.
 *
 * Rapid-input correctness: taps are handled by plain state handlers with no
 * animation-gated logic — every tap updates an Int and a Boolean immediately,
 * so high-speed alternating taps are never dropped, and nothing blocks the
 * main thread (springs run off the state snapshot, not the input path).
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
/**
 * Public entry — kept under the RapidBedTapGame name used by GameRouter.
 */
@Composable
fun RapidBedTapGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    /** True = RIGHT is expected, false = LEFT. Random starting side. */
    var expectRight by remember { mutableStateOf(Random.nextBoolean()) }
    var taps by remember { mutableIntStateOf(0) }
    var isComplete by remember { mutableStateOf(false) }
    var wrongFlash by remember { mutableStateOf(false) }
    var correctFlashSide by remember { mutableStateOf<Boolean?>(null) } // last correct pad
    var roundToken by remember { mutableIntStateOf(0) } // GameRouter re-seed

    val haptic = LocalHapticFeedback.current

    // Re-seed: fresh random starting side, zero progress.
    LaunchedEffect(roundToken) {
        expectRight = Random.nextBoolean()
        taps = 0
        isComplete = false
        wrongFlash = false
        correctFlashSide = null
    }

    // Feedback decays — non-blocking, state-keyed.
    LaunchedEffect(wrongFlash) {
        if (wrongFlash) {
            delay(WRONG_FLASH_MS)
            wrongFlash = false
        }
    }
    LaunchedEffect(correctFlashSide) {
        if (correctFlashSide != null) {
            delay(CORRECT_FLASH_MS)
            correctFlashSide = null
        }
    }

    // ── Handlers: instant state math, zero blocking ──────────────────────────
    fun onPadTapped(tappedRight: Boolean) {
        if (isComplete || wrongFlash) return
        if (tappedRight == expectRight) {
            // Correct side: count it, flip expectation.
            taps++
            correctFlashSide = tappedRight
            GameSounds.tick()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (taps >= SPRINT_TARGET) {
                isComplete = true
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                if (taps % 5 == 0) GameCombo.onCorrectTap() // milestone rising chime
                expectRight = !expectRight
            }
        } else {
            // Same side twice: NOT counted, progress kept, forgiving.
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
            wrongFlash = true
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Text(
            text = "Thumb Sprint",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Alternate LEFT and RIGHT",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = if (wrongFlash) GamePhase.WRONG else GamePhase.ACT,
                text = when {
                    wrongFlash -> "❌ Wrong side — use the other thumb!"
                    taps == 0 -> "✋ Start with ${if (expectRight) "RIGHT" else "LEFT"}!"
                    taps >= SPRINT_TARGET - 5 -> "🔥 Sprint finish — ${SPRINT_TARGET - taps} to go!"
                    else -> "✋ Alternate! — NEXT: ${if (expectRight) "RIGHT" else "LEFT"}"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = "$taps / $SPRINT_TARGET",
                phase = GamePhase.ACT,
                titleFamily = titleFamily
            )

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(SPRINT_TARGET) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i < taps -> AppColorPalette.WinGreen
                                    i == taps -> AppColorPalette.GoldPremium
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // ── The two giant pads ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ThumbPad(
                    label = "LEFT",
                    emoji = "🤜",
                    isExpected = !expectRight,
                    isWrongFlash = wrongFlash,
                    isCorrectFlash = correctFlashSide == false,
                    onClick = { onPadTapped(false) },
                    titleFamily = titleFamily,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                ThumbPad(
                    label = "RIGHT",
                    emoji = "🤛",
                    isExpected = expectRight,
                    isWrongFlash = wrongFlash,
                    isCorrectFlash = correctFlashSide == true,
                    onClick = { onPadTapped(true) },
                    titleFamily = titleFamily,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // Explicit expected-side indicator (non-color cue).
            Text(
                text = "NEXT: ${if (expectRight) "RIGHT" else "LEFT"} ${if (expectRight) "🤛" else "🤜"}",
                color = AppColorPalette.GoldPremium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = interFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Thumb Sprint Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "20 alternating taps — thumbs fully awake 👆",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * One giant thumb pad. The expected side breathes (subtle pulse) and glows
 * gold; press state scales instantly off the interaction source — the input
 * path never waits on animations.
 */
@Composable
private fun ThumbPad(
    label: String,
    emoji: String,
    isExpected: Boolean,
    isWrongFlash: Boolean,
    isCorrectFlash: Boolean,
    onClick: () -> Unit,
    titleFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        isCorrectFlash -> 1.04f
        isWrongFlash -> 0.96f
        pressed -> 0.93f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 800f), label = "padScale$label")

    // Subtle breathing pulse for the expected pad.
    val breathe = rememberInfiniteTransition(label = "breathe$label")
    val breatheAlpha by breathe.animateFloat(
        initialValue = if (isExpected) 0.10f else 0.06f,
        targetValue = if (isExpected) 0.26f else 0.06f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
        label = "breatheA$label"
    )

    val fillColor = when {
        isWrongFlash -> AppColorPalette.LossRed.copy(alpha = 0.3f)
        isCorrectFlash -> AppColorPalette.WinGreen.copy(alpha = 0.3f)
        isExpected -> AppColorPalette.GoldPremium.copy(alpha = breatheAlpha)
        else -> AppColorPalette.Surface
    }
    val borderColor = when {
        isWrongFlash -> AppColorPalette.LossRed
        isCorrectFlash -> AppColorPalette.WinGreen
        isExpected -> AppColorPalette.GoldPremium
        else -> Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(fillColor)
            .border(
                if (isExpected || isWrongFlash || isCorrectFlash) 3.dp else 1.5.dp,
                borderColor,
                RoundedCornerShape(28.dp)
            )
            .clickable(interactionSource = iSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(emoji, fontSize = 58.sp)
            Text(
                text = label,
                color = if (isExpected) AppColorPalette.GoldPremium else Color.White.copy(alpha = 0.85f),
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            if (isExpected) {
                Text(
                    text = "NEXT!",
                    color = AppColorPalette.GoldPremium,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = titleFamily
                )
            }
        }
    }
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * picks a fresh random starting side. Victory maps to [onSuccess].
 */
@Composable
fun RapidBedTapGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        RapidBedTapGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
