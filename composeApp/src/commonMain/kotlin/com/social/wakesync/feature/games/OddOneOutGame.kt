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

private const val TOTAL_ROUNDS = 3

/** One emoji family: base + ranked intruders (index 0 = most distinct). */
internal data class OddFamily(val base: String, val intruders: List<String>)

/**
 * 8 controlled families. Intruders are ranked MOST → LEAST distinct so round
 * difficulty can dial subtlety while staying realistically detectable on any
 * device's emoji font (differences chosen to survive renderer variance).
 */
private val OddFamilies = listOf(
    OddFamily("😀", listOf("🐱", "🥶", "😃")),   // obvious / distinct / subtle
    OddFamily("🐶", listOf("🌸", "🐭", "🐱")),   // animal vs flower / small animal / similar animal
    OddFamily("🍎", listOf("🚗", "🍏", "🍐")),   // object / distinct fruit / similar fruit
    OddFamily("🚗", listOf("🍎", "🚙", "🚕")),   // object / similar car / near-identical car
    OddFamily("⭐", listOf("🍎", "✨", "🌟")),   // object / sparkle / near-identical star
    OddFamily("❤️", listOf("🐶", "💛", "🧡")),   // object / pale heart / orange heart
    OddFamily("🌸", listOf("🚕", "🌷", "🌺")),   // object / similar flower / near-identical flower
    OddFamily("☀️", listOf("⭐", "🌤️", "⛅"))    // object / weather sibling / near-identical weather
)

/**
 * Odd One Out — a visual-perception wake-up challenge.
 *
 * 3 rounds: 3×3 with an obvious intruder → 4×4 with a smaller difference →
 * 4×4 with a subtle one. Exactly one tile differs; tapping it advances.
 * A wrong tap fails the attempt: brief feedback, then a FRESH board restarts
 * from Round 1 (the failed board is never reused).
 *
 * Reliability note: families use base + ranked intruders rather than relying
 * on renderer-specific color deltas, and round 3 always keeps at least one
 * glyph-level difference — detectable but subtle on any Android emoji set.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun OddOneOutGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var round by remember { mutableIntStateOf(1) } // 1..3
    var family by remember { mutableStateOf(OddFamilies[0]) }
    var gridSide by remember { mutableIntStateOf(3) }
    var intruderIdx by remember { mutableIntStateOf(4) } // board position of the odd tile
    var boardVersion by remember { mutableIntStateOf(0) }
    var pickedWrong by remember { mutableIntStateOf(-1) } // wrongly-tapped position flash
    var pickedRight by remember { mutableStateOf(false) } // success animation flag
    var isComplete by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    // Board generation per round: grid grows and the difference gets subtler.
    LaunchedEffect(round, boardVersion) {
        if (isComplete) return@LaunchedEffect
        gridSide = if (round == 1) 3 else 4
        // Rounds 2+ avoid reusing the immediately-previous family for variety.
        var f: OddFamily
        var guard = 0
        do {
            f = OddFamilies.random(Random)
            guard++
        } while (f == family && guard < 20 && (round > 1 || boardVersion > 0))
        family = f

        val cellCount = gridSide * gridSide
        var pos: Int
        var posGuard = 0
        do {
            pos = Random.nextInt(cellCount)
            posGuard++
        } while (pos == intruderIdx && posGuard < 20 && (round > 1 || boardVersion > 0))
        intruderIdx = pos

        pickedWrong = -1
        pickedRight = false
    }

    /** Intruder subtlety per round: obvious → smaller → subtle. */
    fun intruderGlyph(): String = when (round) {
        1 -> family.intruders[0]
        2 -> family.intruders[1]
        else -> family.intruders[2]
    }

    // Success-animation decay → next round (keyed so it can't double-fire).
    LaunchedEffect(pickedRight) {
        if (pickedRight && !isComplete) {
            delay(750)
            round++
        }
    }

    // Wrong-tap feedback decay (fresh board, restart from round 1).
    LaunchedEffect(pickedWrong) {
        if (pickedWrong != -1) {
            delay(800)
            round = 1
            boardVersion++
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onTileTapped(position: Int) {
        if (isComplete || pickedRight || pickedWrong != -1) return
        if (position == intruderIdx) {
            GameCombo.onCorrectTap() // rising-pitch streak
            if (round >= TOTAL_ROUNDS) {
                isComplete = true
                GameCombo.reset()
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                pickedRight = true
                GameSounds.chime()
            }
        } else {
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
            pickedWrong = position
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
            text = "Odd One Out",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Find the different one.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    pickedWrong != -1 -> GamePhase.WRONG
                    pickedRight -> GamePhase.ACT
                    else -> GamePhase.ACT
                },
                text = when {
                    pickedWrong != -1 -> "❌ NOT IT — fresh board, round 1!"
                    pickedRight -> "✅ FOUND IT!"
                    round == 1 -> "✋ Scan the grid — one emoji differs"
                    else -> "✋ Getting subtle — look closely"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = "Round $round / $TOTAL_ROUNDS",
                phase = GamePhase.INFO,
                titleFamily = titleFamily
            )

            // Round progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(TOTAL_ROUNDS) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i + 1 < round -> AppColorPalette.WinGreen
                                    i + 1 == round -> AppColorPalette.GoldPremium
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // ── Responsive grid (3×3 → 4×4) ───────────────────────────────────
            key(round, boardVersion) {
                val cellCount = gridSide * gridSide
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(gridSide) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(gridSide) { col ->
                                val position = row * gridSide + col
                                OddTile(
                                    emoji = if (position == intruderIdx) intruderGlyph() else family.base,
                                    isOdd = position == intruderIdx,
                                    isWrongFlash = pickedWrong == position,
                                    isFound = pickedRight && position == intruderIdx,
                                    subtle = round == TOTAL_ROUNDS,
                                    onClick = { onTileTapped(position) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // First-round coaching
            if (round == 1 && pickedWrong == -1) {
                HowToHint(
                    text = "How to play: every tile shows the same emoji — tap the ONE that's different.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Odd One Out Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Eagle eyes unlocked — all 3 rounds spotted 🔍",
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
 * One grid tile. Large square touch target, press-scale spring, distinct
 * found/wrong states. Round 3 tiles render slightly smaller emojis to add
 * subtlety without hurting tap comfort.
 */
@Composable
private fun OddTile(
    emoji: String,
    isOdd: Boolean,
    isWrongFlash: Boolean,
    isFound: Boolean,
    subtle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        isFound -> 1.12f
        isWrongFlash -> 0.9f
        pressed -> 0.92f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 700f), label = "oddTile")

    val fillColor = when {
        isFound -> AppColorPalette.WinGreen.copy(alpha = 0.3f)
        isWrongFlash -> AppColorPalette.LossRed.copy(alpha = 0.3f)
        else -> AppColorPalette.Surface
    }
    val borderColor = when {
        isFound -> AppColorPalette.WinGreen
        isWrongFlash -> AppColorPalette.LossRed
        else -> Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(if (subtle) 14.dp else 16.dp))
            .background(fillColor)
            .border(if (isFound || isWrongFlash) 2.5.dp else 1.5.dp, borderColor, RoundedCornerShape(if (subtle) 14.dp else 16.dp))
            .clickable(interactionSource = iSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = if (subtle) 30.sp else 34.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * restarts from Round 1 with fresh boards. Victory maps to [onSuccess].
 */
@Composable
fun OddOneOutGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        OddOneOutGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
