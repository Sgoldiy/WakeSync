package com.social.wakesync.feature.games

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

private const val HITS_NEEDED = 5
/** Edge margin (fraction of the radar's smaller dimension) — orb never clips. */
private const val SAFE_MARGIN_FRACTION = 0.12f
/** Orb radius as a fraction of the radar's smaller dimension, per hit. */
private val ORB_RADIUS_FRACTIONS = floatArrayOf(0.14f, 0.115f, 0.09f, 0.07f, 0.055f)

/**
 * Orb Focus — a precision-tapping wake-up challenge on a live radar Canvas.
 *
 * A glowing, wobbling orb appears at a random SAFE position (always fully
 * inside the playable area, clear of edges). Five hits win; the orb shrinks
 * each hit (large → smallest, always realistically tappable ≥ ~5.5% of the
 * board) and springs smoothly to a new location.
 *
 * Rendering is efficient: the radar is a plain Canvas with a few static rings,
 * one animated sweep line, and one glow orb — a handful of draw ops per frame,
 * no allocation in the draw phase, no pixel loops.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun OrbFocusGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var hits by remember { mutableIntStateOf(0) }
    var isComplete by remember { mutableStateOf(false) }
    var hitFlash by remember { mutableIntStateOf(0) } // bumps drive the hit feedback
    var roundToken by remember { mutableIntStateOf(0) } // GameRouter re-seed
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Orb position in NORMALIZED coordinates (0..1 of the canvas), so it is
    // fully responsive — converted to px only at draw/tap time.
    var orbPos by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var orbRadiusFraction by remember { mutableStateOf(ORB_RADIUS_FRACTIONS[0]) }

    // Wobble phase target: nudges the orb ±1.5% around its anchor.
    var wobbleTarget by remember { mutableStateOf(Offset(0.5f, 0.5f)) }

    val haptic = LocalHapticFeedback.current

    /** Picks a new random safe position, margin-aware in normalized space. */
    fun newSafePosition(): Offset {
        val margin = SAFE_MARGIN_FRACTION
        val x = Random.nextFloat() * (1f - 2 * margin) + margin
        val y = Random.nextFloat() * (1f - 2 * margin) + margin
        return Offset(x, y)
    }

    // New position after each hit + initial spawn.
    LaunchedEffect(hits, roundToken) {
        if (isComplete) return@LaunchedEffect
        orbRadiusFraction = ORB_RADIUS_FRACTIONS.getOrElse(hits) { ORB_RADIUS_FRACTIONS.last() }
        orbPos = newSafePosition()
        wobbleTarget = orbPos
    }

    // Gentle wobble: periodically nudge the spring target slightly off-anchor.
    LaunchedEffect(orbPos) {
        while (!isComplete) {
            delay(700)
            val amp = 0.015f
            wobbleTarget = Offset(
                orbPos.x + (Random.nextFloat() * 2 - 1) * amp,
                orbPos.y + (Random.nextFloat() * 2 - 1) * amp
            )
        }
    }

    // Hit-flash decay.
    LaunchedEffect(hitFlash) {
        if (hitFlash > 0) {
            delay(300)
            hitFlash = 0
        }
    }

    // Spring-animated normalized position (smooth movement between spots).
    val springSpec = spring<Float>(dampingRatio = 0.65f, stiffness = 90f)
    val animX by animateFloatAsState(orbPos.x, springSpec, label = "orbX")
    val animY by animateFloatAsState(orbPos.y, springSpec, label = "orbY")
    val wobbleX by animateFloatAsState(wobbleTarget.x, spring(dampingRatio = 0.4f, stiffness = 30f), label = "wobX")
    val wobbleY by animateFloatAsState(wobbleTarget.y, spring(dampingRatio = 0.4f, stiffness = 30f), label = "wobY")

    val glowPulse = rememberInfiniteTransition(label = "orbGlow")
    val glowAlpha by glowPulse.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowA"
    )
    val sweepAngle by glowPulse.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "sweep"
    )

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onRadarTapped(tapPx: Offset) {
        if (isComplete) return
        val size = canvasSize
        if (size.width <= 0 || size.height <= 0) return

        val orbCenter = Offset(
            (wobbleX.coerceIn(0f, 1f)) * size.width,
            (wobbleY.coerceIn(0f, 1f)) * size.height
        )
        val radiusPx = orbRadiusFraction * minOf(size.width, size.height)
        // Forgiving hit-test: +18% slop around the visual radius for fingers.
        val tapRadius = radiusPx * 1.18f

        val dist = hypot(tapPx.x - orbCenter.x, tapPx.y - orbCenter.y)
        if (dist <= tapRadius) {
            // Single count per tap: the state change immediately invalidates
            // further hits until the next orb spawns.
            GameCombo.onCorrectTap()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            hitFlash++
            if (hits + 1 >= HITS_NEEDED) {
                hits++
                isComplete = true
                GameCombo.reset()
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                hits++
                GameSounds.chime()
                // LaunchedEffect(hits) springs the orb to its new home + size.
            }
        } else {
            GameSounds.tick()
            onGameFailed?.invoke() // optional miss hook — no penalty by default
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
            text = "Orb Focus",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Hit the orb.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = GamePhase.ACT,
                text = if (hits == 0) "🎯 Tap the glowing orb — 5 hits to win"
                       else "🎯 Nice — ${HITS_NEEDED - hits} to go! It's shrinking…",
                interFamily = interFamily
            )

            StepCounter(
                text = "$hits / $HITS_NEEDED",
                phase = GamePhase.ACT,
                titleFamily = titleFamily
            )

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(HITS_NEEDED) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i < hits -> AppColorPalette.WinGreen
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // ── Radar Canvas ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF04121C))
                    .border(1.5.dp, AppColorPalette.CyanCta.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(isComplete) {
                            detectTapGestures { offset -> onRadarTapped(offset) }
                        }
                ) {
                    val side = minOf(size.width, size.height).toFloat()
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Radar rings (static — cheap).
                    val ringColor = AppColorPalette.CyanCta.copy(alpha = 0.14f)
                    repeat(4) { r ->
                        drawCircle(
                            color = ringColor,
                            radius = side * (0.2f + r * 0.2f),
                            center = center,
                            style = Stroke(width = 2f)
                        )
                    }
                    // Crosshair
                    drawLine(ringColor, Offset(center.x, center.y - side / 2), Offset(center.x, center.y + side / 2), 2f)
                    drawLine(ringColor, Offset(center.x - side / 2, center.y), Offset(center.x + side / 2, center.y), 2f)

                    // Sweep beam — a rotating gradient wedge (one arc op).
                    val sweepEnd = sweepAngle % 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to AppColorPalette.CyanCta.copy(alpha = 0f),
                            0.92f to AppColorPalette.CyanCta.copy(alpha = 0.0f),
                            1f to AppColorPalette.CyanCta.copy(alpha = 0.35f)
                        ),
                        startAngle = sweepEnd - 60f,
                        sweepAngle = 60f,
                        useCenter = true,
                        topLeft = Offset(center.x - side / 2, center.y - side / 2),
                        size = androidx.compose.ui.geometry.Size(side, side)
                    )

                    // The orb (glow + core) — two draw ops.
                    val orbCenter = Offset(
                        (if (hitFlash > 0) animX else wobbleX).coerceIn(0.04f, 0.96f) * size.width,
                        (if (hitFlash > 0) animY else wobbleY).coerceIn(0.04f, 0.96f) * size.height
                    )
                    val radiusPx = orbRadiusFraction * side

                    // Glow halo.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColorPalette.CyanCta.copy(alpha = 0.55f * glowAlpha),
                                AppColorPalette.CyanCta.copy(alpha = 0f)
                            ),
                            center = orbCenter,
                            radius = radiusPx * 2.4f
                        ),
                        radius = radiusPx * 2.4f,
                        center = orbCenter
                    )
                    // Core with a subtle white ring so it reads on any backdrop.
                    drawCircle(AppColorPalette.CyanCta, radius = radiusPx, center = orbCenter)
                    drawCircle(
                        Color.White.copy(alpha = 0.65f),
                        radius = radiusPx,
                        center = orbCenter,
                        style = Stroke(width = 3f)
                    )
                }
            }

            // First-hit coaching
            if (hits == 0 && hitFlash == 0) {
                HowToHint(
                    text = "How to play: tap the orb wherever it appears — each hit makes it smaller and faster.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎯", fontSize = 64.sp)
            Text(
                text = "Focus Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "5/5 precision hits — sniper mode unlocked 🎯",
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
 * respawns a fresh large orb. Victory maps to [onSuccess].
 */
@Composable
fun OrbFocusGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        OrbFocusGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
