package com.social.wakesync.feature.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import kotlin.random.Random

internal const val SHAKE_TARGET = 12
internal const val SHAKE_ACCEL_THRESHOLD = 17f // m/s² magnitude over gravity (~1.7g swing)
internal const val SHAKE_MIN_INTERVAL_MS = 280L // debounce between valid shakes

/**
 * Expect/actual accelerometer wrapper. The Android actual registers a
 * SENSOR_DELAY_GAME accelerometer listener and applies threshold + debounce +
 * noise filtering before invoking [onShake]. Returns null when no usable
 * accelerometer exists — the game then falls back to tap-to-charge so the
 * alarm can never become impossible to dismiss.
 */
internal expect class ShakeSensor {
    val isAvailable: Boolean
    fun start(onShake: () -> Unit)
    fun stop()
}

internal expect fun createShakeSensor(): ShakeSensor?

/** Mood emoji for a given shake progress (0..12). */
internal fun moodEmojiFor(shakes: Int): String = when {
    shakes >= 12 -> "🤖"
    shakes >= 9 -> "😤"
    shakes >= 6 -> "😵‍💫"
    shakes >= 3 -> "😴"
    else -> "🥱"
}

/**
 * Shake Energy — a physical shake-to-wake challenge.
 *
 * The player performs 12 valid shakes to fill the energy meter. A valid shake
 * is a genuine phone movement: acceleration magnitude above ~1.7g, filtered,
 * and debounced to a minimum interval so sensor jitter never counts. Every
 * valid shake bumps the counter, animates the bar and the mood emoji
 * (🥱 → 😴 → 😵‍💫 → 😤 → 🤖), and gives a light haptic tick.
 *
 * No usable accelerometer → an equally-large "Tap to charge" fallback keeps
 * the alarm dismissible on any device.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun ShakeEnergyGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var shakes by remember { mutableIntStateOf(0) }
    var lastShakeFlash by remember { mutableIntStateOf(0) } // bumps drive emoji bounce
    var isComplete by remember { mutableStateOf(false) }
    var boardVersion by remember { mutableIntStateOf(0) } // GameRouter re-seed token

    val haptic = LocalHapticFeedback.current

    // Sensor availability is stable per device — query once.
    val sensorAvailable = remember { createShakeSensor()?.isAvailable == true }

    fun onValidShake() {
        if (isComplete) return
        GameSounds.tick()
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        shakes++
        lastShakeFlash++
        if (shakes >= SHAKE_TARGET) {
            isComplete = true
            GameCombo.reset()
            GameSounds.spark()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameCompleted()
        }
    }

    // ── Sensor lifecycle: registered ONLY while the game is active. ─────────
    // DisposableEffect guarantees unregistration on leave/dispose — no leaks,
    // no battery drain after the game ends. Completion also detaches early.
    if (sensorAvailable) {
        DisposableEffect(boardVersion) {
            val sensor = createShakeSensor()
            val listener: () -> Unit = { onValidShake() }
            sensor?.start(listener)
            onDispose { sensor?.stop() }
        }
    }

    // Re-seed for a new practice round / ladder stage.
    LaunchedEffect(boardVersion) {
        shakes = 0
        lastShakeFlash = 0
        isComplete = false
    }

    val progress = (shakes.toFloat() / SHAKE_TARGET).coerceIn(0f, 1f)
    val mood = moodEmojiFor(shakes)

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
            text = "Shake Energy",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (sensorAvailable) "Shake your phone! 12 shakes to wake up."
                   else "No shake sensor — tap to charge! 12 taps to wake up.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = GamePhase.ACT,
                text = if (sensorAvailable) "📱 SHAKE! SHAKE! — $shakes / $SHAKE_TARGET"
                       else "✋ TAP! TAP! — $shakes / $SHAKE_TARGET",
                interFamily = interFamily
            )

            StepCounter(
                text = "$shakes / $SHAKE_TARGET",
                phase = GamePhase.ACT,
                titleFamily = titleFamily
            )

            // Animated energy meter
            val barScale by animateFloatAsState(progress, spring(dampingRatio = 0.8f, stiffness = 200f), label = "energyBar")
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(26.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.5.dp, AppColorPalette.CyanCta.copy(alpha = 0.35f), RoundedCornerShape(99.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(barScale.coerceAtLeast(0.02f))
                            .height(20.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                if (progress >= 1f) {
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(AppColorPalette.WinGreen, AppColorPalette.WinGreen)
                                    )
                                } else {
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(AppColorPalette.CyanCta, AppColorPalette.WinGreen)
                                    )
                                }
                            )
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = AppColorPalette.CyanCta,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }

            // Animated mood emoji — scales in on every valid shake.
            key(lastShakeFlash) {
                val bounce by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.35f, stiffness = 380f),
                    label = "moodBounce"
                )
                Box(Modifier.scale(1f + (bounce - 1f) * 0.55f), contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = mood,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = 0.5f))) togetherWith
                                (fadeOut(tween(180)) + scaleOut(targetScale = 1.25f))
                        },
                        label = "moodEmoji"
                    ) { emoji ->
                        Text(emoji, fontSize = 78.sp)
                    }
                }
            }

            // ── The action surface: sensor-driven or tap fallback ─────────────
            if (!sensorAvailable) {
                val padInteraction = remember { MutableInteractionSource() }
                val padPressed by padInteraction.collectIsPressedAsState()
                val padScale by animateFloatAsState(
                    if (padPressed) 0.94f else 1f,
                    spring(dampingRatio = 0.6f, stiffness = 700f),
                    label = "chargePad"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .scale(padScale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppColorPalette.WinGreen.copy(alpha = 0.18f))
                        .border(3.dp, AppColorPalette.WinGreen, RoundedCornerShape(24.dp))
                        .clickable(interactionSource = padInteraction, indication = null) { onValidShake() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TAP TO CHARGE ⚡",
                        color = AppColorPalette.WinGreen,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Keep going — energy is building!",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🤖", fontSize = 72.sp)
            Text(
                text = "FULLY AWAKE!",
                color = AppColorPalette.WinGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Battery 100% — human mode reactivated ⚡",
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
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * resets the meter to 0. Victory maps to [onSuccess].
 */
@Composable
fun ShakeEnergyGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        ShakeEnergyGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
