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

private const val HUNT_TARGETS = 10 // find 1 → 10
private const val BOARD_SIZE = 12 // 12 expression tiles (4×3 responsive grid)
private const val WRONG_FEEDBACK_MS = 500L
private const val CORRECT_FEEDBACK_MS = 450L

/** One board tile: display text + the integer value the expression evaluates to. */
internal data class HuntTile(val display: String, val value: Int)

/**
 * Number Hunt — a visual-search + mental-math wake-up challenge.
 *
 * The player hunts expressions equal to a running target: 1 → 2 → … → 10.
 * Every board contains EXACTLY ONE expression equal to the target; all other
 * expressions evaluate to different values and are pairwise distinct strings
 * (no duplicates, no confusion). Tapping the correct tile highlights it, shows
 * success feedback, increments the target, and generates a completely new
 * board. Wrong taps show brief error feedback and never advance the target —
 * the player just keeps hunting.
 *
 * Expression generator guarantees:
 *  - only +, −, ×, and exact integer ÷ (no fractions ever),
 *  - small, mentally manageable operands,
 *  - no negative results, no division by zero,
 *  - exactly one tile matches the target; distractors never collide with it
 *    or with each other.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun NumberHuntGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var target by remember { mutableIntStateOf(1) } // 1..10
    var board by remember { mutableStateOf(generateBoard(target = 1, avoid = null)) }
    var found by remember { mutableIntStateOf(0) } // tiles found so far (== target - 1)
    var isComplete by remember { mutableStateOf(false) }
    var wrongTileValue by remember { mutableIntStateOf(-1) } // value of wrongly-tapped expr
    var correctTileValue by remember { mutableIntStateOf(-1) } // brief highlight on success
    var boardVersion by remember { mutableIntStateOf(0) } // bumps regenerate the board

    val haptic = LocalHapticFeedback.current

    // Board generation keyed on target: each new target → a completely new board.
    LaunchedEffect(target, boardVersion) {
        if (isComplete) return@LaunchedEffect
        board = generateBoard(target = target, avoid = board)
        wrongTileValue = -1
        correctTileValue = -1
    }

    // Success-highlight decay.
    LaunchedEffect(correctTileValue) {
        if (correctTileValue != -1) {
            delay(CORRECT_FEEDBACK_MS)
            correctTileValue = -1
        }
    }

    // Error-feedback decay (target unchanged — the player keeps hunting).
    LaunchedEffect(wrongTileValue) {
        if (wrongTileValue != -1) {
            delay(WRONG_FEEDBACK_MS)
            wrongTileValue = -1
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onTileTapped(tile: HuntTile) {
        if (isComplete || wrongTileValue != -1 || correctTileValue != -1) return
        if (tile.value == target) {
            GameCombo.onCorrectTap() // rising-pitch streak
            correctTileValue = tile.value
            if (target >= HUNT_TARGETS) {
                isComplete = true
                GameCombo.reset()
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                GameSounds.chime()
                // Increment target; the keyed effect generates a fresh board.
                target++
                found++
            }
        } else {
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
            wrongTileValue = tile.value // error feedback only; same target continues
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
            text = "Number Hunt",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Find the expression equal to:",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            // BIG target number — the thing you're hunting.
            StepCounter(
                text = target.toString(),
                phase = GamePhase.ACT,
                titleFamily = titleFamily
            )

            PhaseBanner(
                phase = when {
                    wrongTileValue != -1 -> GamePhase.WRONG
                    else -> GamePhase.ACT
                },
                text = when {
                    wrongTileValue != -1 -> "❌ WRONG TILE — keep hunting $target!"
                    found == 0 -> "✋ Scan the tiles — one equals $target"
                    else -> "✋ Found $found — now hunt $target"
                },
                interFamily = interFamily
            )

            // Progress dots: 10 targets.
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(HUNT_TARGETS) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                when {
                                    i < found -> AppColorPalette.WinGreen
                                    i == target - 1 -> AppColorPalette.GoldPremium
                                    else -> Color.White.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
            }

            // ── Responsive 3-column expression grid ───────────────────────────
            key(boardVersion, target) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    board.chunked(3).forEach { rowTiles ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowTiles.forEach { tile ->
                                HuntTileCard(
                                    tile = tile,
                                    state = when {
                                        correctTileValue == tile.value && tile.value == target -> HuntTileState.CORRECT
                                        wrongTileValue == tile.value && tile.value != target -> HuntTileState.WRONG
                                        else -> HuntTileState.IDLE
                                    },
                                    onClick = { onTileTapped(tile) },
                                    titleFamily = titleFamily,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Ragged-row filler
                            repeat(3 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // First-target coaching
            if (target == 1 && wrongTileValue == -1) {
                HowToHint(
                    text = "How to play: do the math in your head — tap the tile whose answer is the big number.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Number Hunt Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "All 10 targets found — math brain online 🧮",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

private enum class HuntTileState { IDLE, CORRECT, WRONG }

/**
 * One expression card. Value equality (not identity) drives the CORRECT/WRONG
 * states — safe because the board guarantees exactly one tile per target value.
 */
@Composable
private fun HuntTileCard(
    tile: HuntTile,
    state: HuntTileState,
    onClick: () -> Unit,
    titleFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        state == HuntTileState.CORRECT -> 1.06f
        state == HuntTileState.WRONG -> 0.92f
        pressed -> 0.93f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 700f), label = "huntTile")

    val fillColor = when (state) {
        HuntTileState.CORRECT -> AppColorPalette.WinGreen.copy(alpha = 0.3f)
        HuntTileState.WRONG -> AppColorPalette.LossRed.copy(alpha = 0.3f)
        HuntTileState.IDLE -> AppColorPalette.Surface
    }
    val borderColor = when (state) {
        HuntTileState.CORRECT -> AppColorPalette.WinGreen
        HuntTileState.WRONG -> AppColorPalette.LossRed
        HuntTileState.IDLE -> Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .height(64.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(fillColor)
            .border(if (state == HuntTileState.IDLE) 1.5.dp else 2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(interactionSource = iSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.display,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
    }
}

// ── Expression generation (pure functions — testable, no Compose deps) ──────

/** Renders an expression with display symbols (× and ÷, not * and /). */
private fun renderExpr(a: Int, op: Char, b: Int): String = "$a $op $b"

/**
 * Builds one random, mentally-manageable expression evaluating EXACTLY to
 * [value]. Only exact integer division is ever produced (b != 0 and a % b == 0),
 * subtraction never goes negative, operands stay small.
 */
private fun buildExpressionFor(value: Int, rng: Random): HuntTile? {
    val forms = mutableListOf<Triple<String, Char, () -> Pair<Int, Int>>>()
    // We pick a form first, then derive operands that hit the target exactly.
    return when (rng.nextInt(4)) {
        0 -> { // a + b = value, a,b in 1..12
            val a = rng.nextInt(1, value.coerceAtLeast(2))
            val b = value - a
            if (b < 1 || a > 12 || b > 12) null
            else HuntTile(renderExpr(a, '+', b), value)
        }
        1 -> { // a - b = value → a = value + b, b in 1..9, a ≤ 20
            val b = rng.nextInt(1, 10)
            val a = value + b
            if (a > 20) null
            else HuntTile(renderExpr(a, '−', b), value)
        }
        2 -> { // a × b = value → pick a divisor b of value, 2..9
            val divisors = (2..9).filter { value % it == 0 && value / it in 2..9 }
            if (divisors.isEmpty()) null
            else {
                val b = divisors.random(rng)
                val a = value / b
                HuntTile(renderExpr(a, '×', b), value)
            }
        }
        else -> { // a ÷ b = value → a = value * b, b in 2..5, a ≤ 36
            val b = rng.nextInt(2, 6)
            val a = value * b
            if (a > 36) null
            else HuntTile(renderExpr(a, '÷', b), value)
        }
    }
}

/** Fallback when a form isn't viable for the value: always-viable a + 0-ish variant. */
private fun fallbackExpressionFor(value: Int, rng: Random): HuntTile {
    // (value - k) + k with k in 1..5 — always valid for value ≥ 2; for value 1 use 1 × 1.
    return if (value == 1) {
        HuntTile(renderExpr(1, '×', 1), 1)
    } else {
        val k = rng.nextInt(1, (value).coerceAtMost(6))
        val a = value - k
        val b = k
        if (a >= 1) HuntTile(renderExpr(a, '+', b), value)
        else HuntTile(renderExpr(value, '×', 1), value)
    }
}

/**
 * Generates a full board for [target]:
 *  - exactly one tile evaluates to [target],
 *  - every other tile evaluates to a DIFFERENT value (never the target,
 *    never another distractor's value),
 *  - all display strings are pairwise distinct (no duplicate expressions),
 *  - regenerates deterministically-safe until all constraints hold
 *    (bounded guard + fallback keeps it total).
 */
internal fun generateBoard(target: Int, avoid: List<HuntTile>?): List<HuntTile> {
    val rng = Random
    val tiles = ArrayList<HuntTile>(BOARD_SIZE)
    val usedDisplays = HashSet<String>()
    val usedValues = HashSet<Int>()

    // 1) The single correct tile.
    var correct: HuntTile? = buildExpressionFor(target, rng)
    var guard = 0
    while (correct == null && guard < 10) {
        correct = buildExpressionFor(target, rng)
        guard++
    }
    if (correct == null) correct = fallbackExpressionFor(target, rng)
    tiles.add(correct)
    usedDisplays.add(correct.display)
    usedValues.add(correct.value)

    // 2) Distractors: values ≠ target and mutually distinct.
    while (tiles.size < BOARD_SIZE) {
        val wrongValue = rng.nextInt(1, 21) // 1..20, comfortably mental-math range
        if (wrongValue == target || wrongValue in usedValues) continue

        var candidate = buildExpressionFor(wrongValue, rng)
        var attempt = 0
        while ((candidate == null || candidate.display in usedDisplays) && attempt < 10) {
            candidate = buildExpressionFor(wrongValue, rng)
            attempt++
        }
        if (candidate == null || candidate.display in usedDisplays) continue

        tiles.add(candidate)
        usedDisplays.add(candidate.display)
        usedValues.add(wrongValue)
    }

    val shuffled = tiles.shuffled(rng)

    // Never ship the exact same board layout twice in a row.
    return if (avoid != null && shuffled == avoid && guard < 15) generateBoard(target, avoid) else shuffled
}

/**
 * Adapter used by GameRouter (practice demo + real alarm ladder).
 * [round] re-seeds the whole game — a new practice round or ladder stage
 * restarts the hunt from target 1 with fresh boards. Victory maps to [onSuccess].
 */
@Composable
fun NumberHuntGame(
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    key(round) {
        NumberHuntGame(
            onGameCompleted = onSuccess,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    }
}
