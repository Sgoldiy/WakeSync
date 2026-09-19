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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.graphicsLayer
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

/**
 * Memory Chain — a Simon-style memory game used as a wake-up challenge.
 *
 * Three rounds with growing chains (4 → 5 → 6 tiles). Each round flashes a
 * random sequence (tiles may repeat), then the player must reproduce it.
 * A wrong tap fails the attempt, generates a brand-new sequence, and restarts
 * the round. Completing round 3 calls [MemoryChainGame]'s onGameCompleted.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun MemoryChainGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var round by remember { mutableIntStateOf(1) } // 1..3
    var chainLength by remember { mutableIntStateOf(4) } // 4 → 5 → 6
    var sequence by remember { mutableStateOf(List(4) { 0 }) }
    var inputStep by remember { mutableIntStateOf(0) } // how many tiles the user has matched
    var failedStep by remember { mutableIntStateOf(-1) } // tile that was tapped wrongly, -1 = none
    var isComplete by remember { mutableStateOf(false) }
    var roundJustCleared by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    // Demo/input scheduling lives here: keys (round, sequence, complete) restart
    // the effect cleanly, and leaving composition cancels the coroutine —
    // no leaked timers, no stale flashes.
    var showRoundIntro by remember { mutableStateOf(true) }

    // Generate a fresh chain: never identical to the previous one, no immediate doubles.
    LaunchedEffect(round) {
        chainLength = 3 + round // round 1 → 4, 2 → 5, 3 → 6
        sequence = generateChain(chainLength, previous = if (round == 1) null else sequence)
        inputStep = 0
        failedStep = -1
        roundJustCleared = false
        showRoundIntro = true
    }

    // ── Demonstration phase (blocks input while running) ─────────────────────
    var flashIndex by remember { mutableIntStateOf(-1) } // tile currently flashing, -1 = idle
    var isDemoPlaying by remember { mutableStateOf(true) }

    LaunchedEffect(sequence) {
        isDemoPlaying = true
        delay(650) // let the round intro register before flashing
        showRoundIntro = false
        for (tile in sequence) {
            flashIndex = tile
            GameSounds.tick()
            delay(420) // visible hold
            flashIndex = -1
            delay(180) // gap between flashes
        }
        isDemoPlaying = false
    }

    // ── Round cleared → advance or complete ──────────────────────────────────
    LaunchedEffect(roundJustCleared) {
        if (roundJustCleared && round < 3) {
            delay(900) // let the success state breathe
            round++
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onTileTapped(index: Int) {
        if (isDemoPlaying || isComplete || roundJustCleared || failedStep != -1) return
        val expected = sequence.getOrNull(inputStep) ?: return

        if (index == expected) {
            GameCombo.onCorrectTap() // rising-pitch streak chime
            inputStep++
            if (inputStep >= sequence.size) {
                roundJustCleared = true
                GameCombo.reset()
                if (round >= 3) {
                    isComplete = true
                    GameSounds.spark()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGameCompleted()
                } else {
                    GameSounds.chime()
                }
            }
        } else {
            // Wrong tap: fail the attempt, then regenerate.
            GameCombo.reset()
            failedStep = index
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
        }
    }

    // Failure cooldown: brief red feedback, then a brand-new sequence restarts the round.
    LaunchedEffect(failedStep) {
        if (failedStep != -1) {
            delay(750)
            sequence = generateChain(chainLength, previous = sequence)
            inputStep = 0
            failedStep = -1
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
            text = "Memory Chain",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Remember the tiles in order.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    failedStep != -1 -> GamePhase.WRONG
                    isDemoPlaying -> GamePhase.WATCH
                    roundJustCleared -> GamePhase.ACT
                    else -> GamePhase.ACT
                },
                text = when {
                    failedStep != -1 -> "❌ WRONG TILE — new chain incoming!"
                    isDemoPlaying -> "👀 WATCH · memorize $chainLength tiles"
                    roundJustCleared -> "✅ CHAIN CLEARED!"
                    else -> "✋ YOUR TURN · repeat all $chainLength"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = if (isDemoPlaying) "Round $round / 3" else "$inputStep / $chainLength",
                phase = if (isDemoPlaying) GamePhase.INFO else GamePhase.ACT,
                titleFamily = titleFamily
            )

            // Chain progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(chainLength) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i < inputStep -> AppColorPalette.WinGreen
                                    isDemoPlaying && i == (sequence.take(maxOf(inputStep, 0)).size) -> AppColorPalette.GoldPremium
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // ── 3×3 tile grid ──────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            ChainTile(
                                index = index,
                                isFlashing = flashIndex == index,
                                isWrong = failedStep == index,
                                isOnPath = !isDemoPlaying && index < inputStep && !roundJustCleared,
                                enabled = !isDemoPlaying && failedStep == -1 && !roundJustCleared && !isComplete,
                                onClick = { onTileTapped(index) },
                                titleFamily = titleFamily,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // First-round coaching hint
            if (round == 1 && !isDemoPlaying) {
                HowToHint(
                    text = "How to play: tiles light up in order → tap them back in the SAME order.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Memory Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 30.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "All 3 chains beaten — brain fully booted 🧠",
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
 * Single grid tile. Flash state is driven entirely by the parent — the tile
 * itself only animates, so recomposition stays cheap and predictable.
 */
@Composable
private fun ChainTile(
    index: Int,
    isFlashing: Boolean,
    isWrong: Boolean,
    isOnPath: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    titleFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    // Brighten + scale-up + glow on flash; red shake-ish pulse on wrong tap.
    val targetScale = when {
        isWrong -> 1.1f
        isFlashing -> 1.12f
        pressed -> 0.92f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "chainTileScale$index"
    )

    val baseColor = AppColorPalette.Surface
    val accent = AppColorPalette.CyanCta
    val flashPulse = rememberInfiniteTransition(label = "chainFlash$index")
    val glowAlpha by flashPulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
        label = "chainGlow$index"
    )

    val borderColor = when {
        isWrong -> AppColorPalette.LossRed
        isFlashing -> accent.copy(alpha = glowAlpha)
        isOnPath -> AppColorPalette.WinGreen
        enabled -> accent.copy(alpha = 0.35f)
        else -> Color.White.copy(alpha = 0.08f)
    }
    val fillColor = when {
        isWrong -> AppColorPalette.LossRed.copy(alpha = 0.35f)
        isFlashing -> accent.copy(alpha = 0.55f * glowAlpha)
        isOnPath -> AppColorPalette.WinGreen.copy(alpha = 0.25f)
        else -> baseColor
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(fillColor)
            .border(width = if (isFlashing || isWrong || isOnPath) 3.dp else 1.5.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
            .graphicsLayer {
                if (isFlashing) {
                    // Cheap glow: soft shadow layer tinted with the accent.
                    shadowElevation = 24f * glowAlpha
                    shape = RoundedCornerShape(20.dp)
                    clip = false
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = iSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isWrong) {
            Text("✕", color = AppColorPalette.LossRed, fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = titleFamily)
        } else if (isFlashing) {
            // Sequence position number makes the order readable, not just a blink.
            Text("•", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        }
    }
}

/**
 * Generates a random chain of [length] tile indices (0..8). Tiles may repeat,
 * but never twice in a row (would be unreadable), and the chain is guaranteed
 * different from [previous] so retries always produce a new task.
 */
private fun generateChain(length: Int, previous: List<Int>?): List<Int> {
    var candidate: List<Int>
    var guard = 0
    do {
        candidate = buildList {
            var last = -1
            repeat(length) {
                var next = Random.nextInt(9)
                while (next == last) next = Random.nextInt(9)
                add(next)
                last = next
            }
        }
        guard++
    } while (previous != null && candidate == previous && guard < 20)
    return candidate
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * starts from round 1 with a fresh chain. Victory maps to [onSuccess].
 */
@Composable
fun ChainMemoryGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        MemoryChainGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
