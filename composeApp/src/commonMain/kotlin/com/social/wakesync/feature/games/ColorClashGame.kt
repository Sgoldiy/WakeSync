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

/** One Stroop color: on-screen ink value + human-readable, color-blind-safe name. */
private data class ClashColor(val name: String, val ink: Color)

private val ClashColors = listOf(
    ClashColor("Red", Color(0xFFFF4757)),
    ClashColor("Blue", Color(0xFF3742FA)),
    ClashColor("Green", Color(0xFF2ED573)),
    ClashColor("Yellow", Color(0xFFFFC048))
)

/**
 * Color Clash — a Stroop-effect wake-up challenge.
 *
 * A color WORD is printed in a different font INK; the player must tap the
 * ink color, not the word. Exactly 5 trials with escalating pressure:
 * answer-time limits shrink each trial (always generous enough to read).
 * A wrong tap fails immediately: feedback, then a fresh set of 5 trials
 * restarts from Trial 1. Completing all 5 calls [onGameCompleted].
 *
 * Accessibility: every answer button always shows the color NAME in high
 * contrast next to its ink swatch — the game never relies on color alone.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun ColorClashGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Tuning: per-trial answer windows, always readable ────────────────────
    fun trialTimeLimit(t: Int): Int = when (t) {
        1 -> 5000 // normal
        2 -> 4000 // slightly faster
        3 -> 3500 // more conflict comes from generation, not just speed
        4 -> 3000 // faster
        else -> 2500 // fastest — still ~2.5s, never impossible
    }

    // ── Game state ────────────────────────────────────────────────────────────
    var trial by remember { mutableIntStateOf(1) } // 1..5
    var wordColor by remember { mutableStateOf(ClashColors[0]) }
    var inkColor by remember { mutableStateOf(ClashColors[1]) }
    var failedAnswer by remember { mutableStateOf<ClashColor?>(null) } // wrong pick, brief feedback
    var isComplete by remember { mutableStateOf(false) }
    var flashCorrect by remember { mutableStateOf<ClashColor?>(null) }
    var setVersion by remember { mutableIntStateOf(0) } // bumps regenerate the whole 5-trial set

    val haptic = LocalHapticFeedback.current

    // ── Trial generation: fresh random word/ink pair per trial ───────────────
    // Trials 3+ force MORE conflicting combos (ink word ≠ ink at higher rate).
    LaunchedEffect(trial, setVersion) {
        if (isComplete) return@LaunchedEffect
        flashCorrect = null
        var w: ClashColor
        var i: ClashColor
        var guard = 0
        do {
            w = ClashColors.random(Random)
            i = ClashColors.random(Random)
            guard++
            // Trial 1–2 allow a matching (easy) pair occasionally; 3+ force conflict.
            val conflictRequired = trial >= 3
            val differs = w.name != i.name
        } while ((conflictRequired && !differs) || guard > 30)
        wordColor = w
        inkColor = i
    }

    // ── Per-trial countdown: cancelled and restarted per trial — no leaks ────
    var msLeft by remember { mutableIntStateOf(trialTimeLimit(1)) }
    LaunchedEffect(trial, setVersion) {
        if (isComplete) return@LaunchedEffect
        msLeft = trialTimeLimit(trial)
        while (msLeft > 0) {
            delay(50)
            msLeft -= 50
        }
        // Time ran out: counts as a wrong answer.
        if (!isComplete) {
            failedAnswer = ClashColor(name = "⏱ Time", ink = AppColorPalette.LossRed)
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
        }
    }

    // Failure feedback cooldown → fresh 5-trial set from Trial 1.
    LaunchedEffect(failedAnswer) {
        if (failedAnswer != null) {
            delay(900)
            trial = 1
            setVersion++
            failedAnswer = null
        }
    }

    // Correct-flash decay.
    LaunchedEffect(flashCorrect) {
        if (flashCorrect != null) {
            delay(300)
            flashCorrect = null
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onAnswerPicked(color: ClashColor) {
        if (isComplete || failedAnswer != null || flashCorrect != null) return
        if (color.name == inkColor.name) {
            GameCombo.onCorrectTap() // rising-pitch streak
            if (trial >= 5) {
                isComplete = true
                GameCombo.reset()
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                flashCorrect = color
                GameSounds.chime()
                trial++ // restarts generation + countdown effects
            }
        } else {
            GameCombo.reset()
            failedAnswer = color
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Text(
            text = "Color Clash",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap the FONT COLOR,\nnot the word.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    failedAnswer != null -> GamePhase.WRONG
                    else -> GamePhase.ACT
                },
                text = when {
                    failedAnswer != null -> if (failedAnswer?.name == "⏱ Time") "⏱ TOO SLOW — new set incoming!" else "❌ WRONG — that's the WORD, not the ink!"
                    else -> "✋ Which INK is it? — $msLeft ms"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = "Trial $trial / 5",
                phase = GamePhase.INFO,
                titleFamily = titleFamily
            )

            // Time bar — shrinks as the window closes; red when urgent.
            val timeFraction = msLeft.toFloat() / trialTimeLimit(trial).toFloat()
            val barScale by animateFloatAsState(timeFraction, tween(60), label = "timeBar")
            Box(
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(barScale.coerceIn(0f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            if (timeFraction < 0.3f) AppColorPalette.LossRed else AppColorPalette.CyanCta
                        )
                )
            }

            // The BIG Stroop word — swatches for accessibility only; the ink is the answer.
            AnimatedContent(
                targetState = wordColor to inkColor,
                transitionSpec = { (fadeIn(tween(160)) + scaleIn(initialScale = 0.92f, animationSpec = tween(160))) togetherWith fadeOut(tween(100)) },
                label = "stroopWord"
            ) { (w, i) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = w.name.uppercase(),
                        color = i.ink,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        textAlign = TextAlign.Center
                    )
                    // Non-color clue: a small swatch of the true ink (color-independent fallback).
                    Box(
                        Modifier
                            .height(8.dp)
                            .fillMaxWidth(0.16f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(i.ink)
                    )
                }
            }

            // ── Answer buttons: 2×2, huge, always labeled ─────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ClashColors.chunked(2).forEach { rowColors ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowColors.forEach { color ->
                            ClashAnswerButton(
                                color = color,
                                state = when {
                                    failedAnswer == color -> ClashButtonState.WRONG
                                    flashCorrect == color -> ClashButtonState.CORRECT
                                    else -> ClashButtonState.IDLE
                                },
                                onClick = { onAnswerPicked(color) },
                                interFamily = interFamily,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // First-trial coaching
            if (trial == 1 && failedAnswer == null) {
                HowToHint(
                    text = "How to play: ignore what the word SAYS — tap the color it's printed in.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Color Clash Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "5/5 trials — eyes wide awake 👁️",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

private enum class ClashButtonState { IDLE, CORRECT, WRONG }

/**
 * Big, high-contrast answer button: ink swatch + always-visible color NAME.
 * The name label uses a fixed near-white so contrast stays strong on any ink.
 */
@Composable
private fun ClashAnswerButton(
    color: ClashColor,
    state: ClashButtonState,
    onClick: () -> Unit,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        state != ClashButtonState.IDLE -> 1.05f
        pressed -> 0.93f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 700f), label = "clashBtn")

    val bgColor = when (state) {
        ClashButtonState.CORRECT -> AppColorPalette.WinGreen.copy(alpha = 0.25f)
        ClashButtonState.WRONG -> AppColorPalette.LossRed.copy(alpha = 0.3f)
        ClashButtonState.IDLE -> AppColorPalette.Surface
    }
    val borderColor = when (state) {
        ClashButtonState.CORRECT -> AppColorPalette.WinGreen
        ClashButtonState.WRONG -> AppColorPalette.LossRed
        ClashButtonState.IDLE -> color.ink.copy(alpha = 0.5f)
    }

    Row(
        modifier = modifier
            .height(64.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = iSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        // Ink swatch — the color itself is never the ONLY cue.
        Box(
            Modifier
                .height(22.dp)
                .fillMaxWidth(0.14f)
                .clip(RoundedCornerShape(6.dp))
                .background(color.ink)
        )
        Text(
            text = color.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            fontFamily = interFamily
        )
    }
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * restarts from Trial 1. Victory maps to [onSuccess].
 */
@Composable
fun ColorClashGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        ColorClashGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
