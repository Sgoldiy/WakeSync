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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlin.random.Random

internal const val TARGET_ROUNDS = 5
private const val TARGETS_MIN = 2
private const val ERROR_FLASH_MS = 420L
private const val ROUND_CLEAR_MS = 750L

/** Controlled object vocabulary: 5 shapes × 5 colors. */
internal enum class TargetShape(val label: String) { CIRCLE("Circle"), SQUARE("Square"), TRIANGLE("Triangle"), STAR("Star"), DIAMOND("Diamond") }
internal enum class TargetColor(val label: String, val ink: Color) {
    BLUE("Blue", Color(0xFF3742FA)),
    RED("Red", Color(0xFFFF4757)),
    GREEN("Green", Color(0xFF2ED573)),
    YELLOW("Yellow", Color(0xFFFFC048)),
    PURPLE("Purple", Color(0xFFA855F7))
}

internal data class TargetObject(val shape: TargetShape, val color: TargetColor) {
    val description: String get() = "$color.label $shape.label" // accessibility
}

internal data class TargetBoard(
    val side: Int,
    val cells: List<TargetObject?>, // side*side, null = empty cell
    val targets: Set<Int> // indices that match the target
) {
    val targetCount: Int get() = targets.size
}

internal data class TargetSpec(val color: TargetColor, val shape: TargetShape) {
    fun matches(o: TargetObject): Boolean = o.color == color && o.shape == shape
}

/**
 * Target Tap — a selective-attention wake-up challenge.
 *
 * 5 escalating rounds: 3×3 easy → 4×4 shape+color → 4×4 heavy distractors →
 * 5×5 large grid → hard mode with same-color traps. The player taps ONLY the
 * objects matching the round's target spec; wrong taps are forgiving (flash,
 * buzz, no penalty). All targets found → fast round transition; 5 rounds →
 * onGameCompleted().
 *
 * Board stability: generated ONCE per round into remember state — normal
 * recomposition never regenerates it. Only round start / retry / re-seed
 * generates a new board.
 *
 * Fully self-contained: no alarm logic, no network, no resources.
 */
@Composable
fun TargetTapGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var round by remember { mutableIntStateOf(1) }
    var roundToken by remember { mutableIntStateOf(0) } // GameRouter re-seed + retry token
    var spec by remember { mutableStateOf(TargetSpec(TargetColor.BLUE, TargetShape.CIRCLE)) }
    var board by remember { mutableStateOf(TargetBoard(3, emptyList(), emptySet())) }
    val found = remember { mutableStateListOf<Int>() } // tapped target indices
    var wrongIndex by remember { mutableIntStateOf(-1) }
    var roundClearing by remember { mutableStateOf(false) }
    var isComplete by remember { mutableStateOf(false) }
    var roundStartNanos by remember { mutableLongStateOf(0L) }
    var roundSecs by remember { mutableStateOf(0.0) }

    val haptic = LocalHapticFeedback.current

    // Board generation — ONCE per round / retry / re-seed (state-keyed).
    LaunchedEffect(round, roundToken) {
        if (isComplete) return@LaunchedEffect
        spec = generateSpec(round, previous = spec, rng = Random)
        board = generateBoard(round, spec, rng = Random)
        found.clear()
        wrongIndex = -1
        roundClearing = false
        roundStartNanos = monotonicNanos()
    }

    // Wrong-flash decay.
    LaunchedEffect(wrongIndex) {
        if (wrongIndex != -1) {
            delay(ERROR_FLASH_MS)
            wrongIndex = -1
        }
    }

    // Round-clear transition: brief ✓ celebration → next round / completion.
    LaunchedEffect(roundClearing) {
        if (roundClearing) {
            delay(ROUND_CLEAR_MS)
            if (round >= TARGET_ROUNDS) {
                isComplete = true
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                round++
            }
        }
    }

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onCellTapped(index: Int) {
        if (isComplete || roundClearing || wrongIndex != -1) return
        val obj = board.cells.getOrNull(index) ?: return
        if (index in found) return // already completed — untappable

        if (spec.matches(obj)) {
            found.add(index)
            GameCombo.onCorrectTap()
            GameSounds.tick()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (found.size >= board.targetCount) {
                roundSecs = (monotonicNanos() - roundStartNanos) / 1_000_000_000.0
                roundClearing = true
                GameCombo.reset()
                GameSounds.chime()
            }
        } else {
            // Forgiving: error flash only — round stays active, no penalty.
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
            wrongIndex = index
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Text(
            text = "Target Tap",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tap ONLY the objects matching:",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    wrongIndex != -1 -> GamePhase.WRONG
                    roundClearing -> GamePhase.ACT
                    else -> GamePhase.ACT
                },
                text = when {
                    wrongIndex != -1 -> "❌ Wrong object — keep hunting!"
                    roundClearing -> "✓ Round Complete!"
                    else -> "🎯 TAP ALL ${spec.color.label.uppercase()} ${spec.shape.label.uppercase()}S"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = "${found.size} / ${board.targetCount} targets",
                phase = GamePhase.ACT,
                titleFamily = titleFamily
            )

            // Round progress dots + explicit target swatch (visual + text).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(TARGET_ROUNDS) { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
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

            // Big target card: shape swatch in target color + words (a11y pairing).
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColorPalette.Surface)
                    .border(2.dp, spec.color.ink, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TargetGlyph(shape = spec.shape, color = spec.color.ink, size = 22.dp)
                Text(
                    text = "${spec.color.label} ${spec.shape.label}S",
                    color = spec.color.ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }

            // ── The grid (generated once per round — stable across recomposition)
            key(round, roundToken) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(board.side) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(board.side) { col ->
                                val index = row * board.side + col
                                val obj = board.cells[index]
                                Box(modifier = Modifier.weight(1f)) {
                                    if (obj != null) {
                                        TargetCell(
                                            obj = obj,
                                            isFound = index in found,
                                            isWrongFlash = wrongIndex == index,
                                            onClick = { onCellTapped(index) }
                                        )
                                    } else {
                                        Spacer(Modifier.aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Timer + coaching (unstressed).
            Text(
                text = if (round == 1 && found.isEmpty()) {
                    "How to play: tap every ${spec.color.label.lowercase()} ${spec.shape.label.lowercase()} — nothing else!"
                } else {
                    "Round $round / $TARGET_ROUNDS"
                },
                color = if (round == 1 && found.isEmpty()) AppColorPalette.GoldPremium else Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎯", fontSize = 64.sp)
            Text(
                text = "Target Tap Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 26.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "5 / 5 rounds — selective focus online 🎯",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Board generation (pure functions — testable, no Compose deps) ───────────

/** Round → target spec, avoiding the immediately-previous spec for variety. */
internal fun generateSpec(round: Int, previous: TargetSpec, rng: Random): TargetSpec {
    var candidate: TargetSpec
    var guard = 0
    do {
        candidate = TargetSpec(
            color = TargetColor.entries[rng.nextInt(TargetColor.entries.size)],
            shape = TargetShape.entries[rng.nextInt(TargetShape.entries.size)]
        )
        guard++
    } while (candidate == previous && guard < 15)
    return candidate
}

/**
 * Builds a valid board for [round]:
 *  - grid grows 3×3 → 4×4 → 4×4 → 5×5 → 5×5,
 *  - 2–4 (larger rounds up to 5) targets scattered on distinct cells,
 *  - distractors varied; from round 3 many share the target color or shape
 *    (round 5 packs same-color different-shape traps per the hard-mode spec),
 *  - every cell holds at most one object; no two targets overlap.
 */
internal fun generateBoard(round: Int, spec: TargetSpec, rng: Random): TargetBoard {
    val side = when (round) {
        1 -> 3
        2, 3 -> 4
        else -> 5
    }
    val cellCount = side * side
    val cells = arrayOfNulls<TargetObject>(cellCount)

    val maxTargets = if (side <= 3) 4 else 5
    val targetCount = rng.nextInt(TARGETS_MIN, maxTargets + 1).coerceAtMost(cellCount / 3)

    // Scatter targets on distinct, non-adjacent-ish cells (no stacking).
    val targetIndices = mutableSetOf<Int>()
    while (targetIndices.size < targetCount) {
        targetIndices.add(rng.nextInt(cellCount))
    }
    targetIndices.forEach { cells[it] = TargetObject(spec.shape, spec.color) }

    // Fill remaining cells with varied distractors.
    for (i in 0 until cellCount) {
        if (cells[i] != null) continue
        var obj: TargetObject
        var guard = 0
        do {
            val color = when {
                round >= 5 && rng.nextInt(100) < 55 -> spec.color // hard mode: color traps
                round >= 3 && rng.nextInt(100) < 30 -> spec.color // similar-color distractors
                else -> TargetColor.entries[rng.nextInt(TargetColor.entries.size)]
            }
            val shape = when {
                round >= 3 && rng.nextInt(100) < 35 -> spec.shape // similar-shape distractors
                else -> TargetShape.entries[rng.nextInt(TargetShape.entries.size)]
            }
            obj = TargetObject(shape, color)
            guard++
        } while (spec.matches(obj) && guard < 15) // never place an uncounted target
        cells[i] = obj
    }

    return TargetBoard(side = side, cells = cells.toList(), targets = targetIndices)
}

/** One grid object: big touch target, semantic description, animated states. */
@Composable
private fun TargetCell(
    obj: TargetObject,
    isFound: Boolean,
    isWrongFlash: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        isFound -> 1.0f
        isWrongFlash -> 0.9f
        pressed -> 0.9f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 700f), label = "ttCell")

    val borderColor = when {
        isFound -> AppColorPalette.WinGreen
        isWrongFlash -> AppColorPalette.LossRed
        else -> Color.White.copy(alpha = 0.1f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFound) AppColorPalette.WinGreen.copy(alpha = 0.14f) else AppColorPalette.Surface)
            .border(if (isFound || isWrongFlash) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .then(
                if (isFound) Modifier else Modifier
                    .clickable(interactionSource = iSource, indication = null) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
            )
            .semantics { contentDescription = obj.description },
        contentAlignment = Alignment.Center
    ) {
        if (isFound) {
            Text("✓", color = AppColorPalette.WinGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
        } else {
            TargetGlyph(shape = obj.shape, color = obj.color.ink, size = 24.dp)
        }
    }
}

/** Vector glyph per shape — reliable rendering on every device (no emoji). */
@Composable
private fun TargetGlyph(shape: TargetShape, color: Color, size: androidx.compose.ui.unit.Dp) {
    when (shape) {
        TargetShape.CIRCLE -> Box(
            Modifier.height(size).aspectRatio(1f).clip(CircleShape).background(color)
        )
        TargetShape.SQUARE -> Box(
            Modifier.height(size).aspectRatio(1f).clip(RoundedCornerShape(3.dp)).background(color)
        )
        TargetShape.TRIANGLE -> Box(
            Modifier
                .height(size)
                .aspectRatio(1f)
                .clip(
                    GenericShape { s, _ ->
                        moveTo(s.width / 2f, 0f)
                        lineTo(s.width, s.height)
                        lineTo(0f, s.height)
                        close()
                    }
                )
                .background(color)
        )
        TargetShape.DIAMOND -> Box(
            Modifier.height(size).aspectRatio(1f).scale(0.72f).rotate(45f).clip(RoundedCornerShape(3.dp)).background(color)
        )
        TargetShape.STAR -> Box(
            Modifier.height(size).aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val path = starPath(size = this.size.minDimension / 2f, center = center)
                drawPath(path, color)
            }
        }
    }
}

/** Simple solid 5-point star path. */
private fun starPath(size: Float, center: Offset): Path = Path().apply {
    val outer = size
    val inner = size * 0.45f
    for (i in 0 until 10) {
        val angle = kotlin.math.PI * (i / 5.0) - kotlin.math.PI / 2
        val r = if (i % 2 == 0) outer else inner
        val x = center.x + r * kotlin.math.cos(angle).toFloat()
        val y = center.y + r * kotlin.math.sin(angle).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}


