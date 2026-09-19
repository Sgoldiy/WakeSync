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

internal const val SEQ_ROUNDS = 5
private const val ANSWER_COUNT = 4
private const val RESULT_SHOW_MS = 950L
private const val ERROR_SHOW_MS = 750L

/** One generated sequence puzzle: displayed terms (null = the "?" slot) + options. */
internal data class SequencePuzzle(
    val terms: List<Int?>, // exactly one null
    val answer: Int,
    val options: List<Int> // 4 unique ints, one == answer
) {
    val missingIndex: Int get() = terms.indexOf(null)
}

/**
 * Number Sequence — a pattern-recognition + mental-math wake-up challenge.
 *
 * 5 rounds of escalating difficulty, each showing a sequence with one missing
 * number and 4 large answer buttons (exactly one correct, 3 plausible
 * distractors, never duplicates). Templates per round guarantee a single clear
 * pattern — never ambiguous, never fractional, never huge numbers.
 *
 * Wrong answer: "✕ Try Again", buzz, and a COMPLETELY FRESH, DIFFERENT
 * sequence for the same round (correct answer never revealed). Correct:
 * highlight, show the completed sequence briefly ("2 → 4 → 6 → 8 → 10"),
 * then next round. Per-round solve time is tracked; the final panel shows the
 * average. All alarm logic lives outside this composable.
 */
@Composable
fun NumberSequenceGame(
    onGameCompleted: () -> Unit,
    onGameFailed: (() -> Unit)? = null,
    titleFamily: FontFamily = FontFamily.Default,
    interFamily: FontFamily = FontFamily.Default
) {
    // ── Game state ────────────────────────────────────────────────────────────
    var round by remember { mutableIntStateOf(1) } // 1..5
    var puzzle by remember { mutableStateOf(generatePuzzle(1, previous = null)) }
    var pickedAnswer by remember { mutableStateOf<Int?>(null) } // correct pick flash
    var wrongPick by remember { mutableStateOf<Int?>(null) } // wrong pick flash
    var isComplete by remember { mutableStateOf(false) }
    var roundToken by remember { mutableIntStateOf(0) } // GameRouter re-seed
    var puzzleVersion by remember { mutableIntStateOf(0) } // fresh-sequence token

    // Timer: per-round solve time (unstressed display; average at the end).
    var roundStartNanos by remember { mutableLongStateOf(0L) }
    var solveTimesSecs = remember { mutableStateOf(listOf<Double>()) }
    var liveTenths by remember { mutableIntStateOf(0) }

    val haptic = LocalHapticFeedback.current

    // New puzzle per round / per retry / per re-seed; timer restarts.
    LaunchedEffect(round, puzzleVersion, roundToken) {
        if (isComplete) return@LaunchedEffect
        puzzle = generatePuzzle(round, previous = puzzle)
        pickedAnswer = null
        wrongPick = null
        roundStartNanos = monotonicNanos()
        liveTenths = 0
    }

    // Non-stressful live timer: 100ms ticks while the round is unsolved.
    LaunchedEffect(roundStartNanos, pickedAnswer, isComplete) {
        if (roundStartNanos == 0L || pickedAnswer != null || isComplete) return@LaunchedEffect
        while (pickedAnswer == null && !isComplete) {
            delay(100)
            liveTenths++
        }
    }

    // Correct-answer flow: brief completed-sequence reveal → next round.
    LaunchedEffect(pickedAnswer) {
        if (pickedAnswer != null && pickedAnswer == puzzle.answer) {
            delay(RESULT_SHOW_MS)
            if (round >= SEQ_ROUNDS) {
                isComplete = true
                GameSounds.spark()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGameCompleted()
            } else {
                round++
            }
        }
    }

    // Wrong-answer flow: short feedback → fresh, different sequence, same round.
    LaunchedEffect(wrongPick) {
        if (wrongPick != null) {
            delay(ERROR_SHOW_MS)
            puzzleVersion++ // regenerates a different sequence for this round
        }
    }

    fun elapsedSecs(): Double = if (roundStartNanos == 0L) 0.0
        else (monotonicNanos() - roundStartNanos) / 1_000_000_000.0

    // ── Handlers ─────────────────────────────────────────────────────────────
    fun onAnswerPicked(value: Int) {
        if (isComplete || pickedAnswer != null || wrongPick != null) return
        if (value == puzzle.answer) {
            // Record solve time, then celebrate briefly.
            val t = elapsedSecs()
            solveTimesSecs.value = solveTimesSecs.value + t
            pickedAnswer = value
            GameCombo.onCorrectTap()
            GameSounds.chime()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            wrongPick = value
            GameCombo.reset()
            GameSounds.buzz()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFailed?.invoke()
        }
    }

    val averageSecs = if (solveTimesSecs.value.isEmpty()) 0.0
    else solveTimesSecs.value.sum() / solveTimesSecs.value.size

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
            text = "Number Sequence",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Find the missing number.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        if (!isComplete) {
            PhaseBanner(
                phase = when {
                    wrongPick != null -> GamePhase.WRONG
                    pickedAnswer != null -> GamePhase.ACT
                    else -> GamePhase.ACT
                },
                text = when {
                    wrongPick != null -> "✕ Try Again — new sequence!"
                    pickedAnswer != null -> "✓ Correct!"
                    else -> "🧠 Spot the pattern — 1 clear answer"
                },
                interFamily = interFamily
            )

            StepCounter(
                text = "Round $round / $SEQ_ROUNDS    ·    Time: ${fmt1(elapsedSecs().coerceAtLeast(liveTenths / 10.0))}s",
                phase = GamePhase.INFO,
                titleFamily = titleFamily
            )

            // Round progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(SEQ_ROUNDS) { i ->
                    Box(
                        Modifier
                            .height(6.dp)
                            .weight(1f)
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

            // ── The sequence strip with the obvious "?" slot ───────────────────
            AnimatedContent(
                targetState = Triple(puzzle, pickedAnswer, wrongPick),
                transitionSpec = {
                    (fadeIn(tween(180)) + scaleIn(initialScale = 0.94f, animationSpec = tween(180))) togetherWith fadeOut(tween(100))
                },
                label = "sequenceStrip"
            ) { (p, correct, wrong) ->
                key(p, correct, wrong) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            p.terms.forEachIndexed { idx, term ->
                                SequenceCell(
                                    term = term,
                                    isMissing = idx == p.missingIndex,
                                    revealedValue = if (correct != null) p.answer else null,
                                    isError = wrong != null && idx == p.missingIndex,
                                    titleFamily = titleFamily,
                                    modifier = Modifier.weight(1f)
                                )
                                if (idx < p.terms.lastIndex) {
                                    Text("→", color = Color.White.copy(alpha = 0.3f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        // Difficulty label per round (helps set expectations).
                        Text(
                            text = when (round) {
                                1 -> "Easy · + and −"
                                2 -> "Medium · × and ÷"
                                3 -> "Mixed patterns"
                                4 -> "Alternating patterns"
                                else -> "Hard · stay sharp!"
                            },
                            color = AppColorPalette.GoldPremium,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily
                        )
                    }
                }
            }

            // ── 4 big answer buttons (2×2) ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                puzzle.options.chunked(2).forEach { rowOptions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowOptions.forEach { option ->
                            SequenceAnswerButton(
                                value = option,
                                state = when {
                                    pickedAnswer == option -> SeqButtonState.CORRECT
                                    wrongPick == option -> SeqButtonState.WRONG
                                    else -> SeqButtonState.IDLE
                                },
                                enabled = pickedAnswer == null && wrongPick == null,
                                onClick = { onAnswerPicked(option) },
                                interFamily = interFamily,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(2 - rowOptions.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // First-round coaching
            if (round == 1 && pickedAnswer == null && wrongPick == null) {
                HowToHint(
                    text = "How to play: work out the pattern between the numbers, then tap the missing value.",
                    interFamily = interFamily
                )
            }
        } else {
            // Victory panel
            Spacer(modifier = Modifier.height(8.dp))
            Text("🎉", fontSize = 64.sp)
            Text(
                text = "Number Sequence Complete!",
                color = AppColorPalette.WinGreen,
                fontSize = 26.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "5 / 5 · Average: ${fmt1(averageSecs)}s 🧠",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

private enum class SeqButtonState { IDLE, CORRECT, WRONG }

/** One sequence cell: number, or the highlighted "?" slot (reveals on success). */
@Composable
private fun SequenceCell(
    term: Int?,
    isMissing: Boolean,
    revealedValue: Int?,
    isError: Boolean,
    titleFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        if (isMissing && (revealedValue != null || isError)) 1.08f else 1f,
        spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "seqCell"
    )
    val fillColor = when {
        isMissing && revealedValue != null -> AppColorPalette.WinGreen.copy(alpha = 0.3f)
        isMissing && isError -> AppColorPalette.LossRed.copy(alpha = 0.25f)
        isMissing -> AppColorPalette.GoldPremium.copy(alpha = 0.15f)
        else -> AppColorPalette.Surface
    }
    val borderColor = when {
        isMissing && revealedValue != null -> AppColorPalette.WinGreen
        isMissing && isError -> AppColorPalette.LossRed
        isMissing -> AppColorPalette.GoldPremium
        else -> Color.White.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(fillColor)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                isMissing && revealedValue != null -> revealedValue.toString()
                isMissing -> "?"
                else -> term.toString()
            },
            color = when {
                isMissing -> if (revealedValue != null) AppColorPalette.WinGreen else AppColorPalette.GoldPremium
                else -> Color.White
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )
    }
}

/** Large 2×2 answer button with correct/wrong flash states. */
@Composable
private fun SequenceAnswerButton(
    value: Int,
    state: SeqButtonState,
    enabled: Boolean,
    onClick: () -> Unit,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val iSource = remember { MutableInteractionSource() }
    val pressed by iSource.collectIsPressedAsState()

    val targetScale = when {
        state == SeqButtonState.CORRECT -> 1.06f
        state == SeqButtonState.WRONG -> 0.94f
        pressed -> 0.93f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, spring(dampingRatio = 0.5f, stiffness = 700f), label = "seqBtn$value")

    val fillColor = when (state) {
        SeqButtonState.CORRECT -> AppColorPalette.WinGreen.copy(alpha = 0.3f)
        SeqButtonState.WRONG -> AppColorPalette.LossRed.copy(alpha = 0.3f)
        SeqButtonState.IDLE -> AppColorPalette.Surface
    }
    val borderColor = when (state) {
        SeqButtonState.CORRECT -> AppColorPalette.WinGreen
        SeqButtonState.WRONG -> AppColorPalette.LossRed
        SeqButtonState.IDLE -> Color.White.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .height(64.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(fillColor)
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, interactionSource = iSource, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.W900,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )
    }
}

// ── Puzzle generation (pure functions — testable, no Compose deps) ──────────

/**
 * Builds a fresh puzzle for [round] (1..5) using TEMPLATED patterns with
 * randomized parameters. Guarantees: one missing term, one clear unambiguous
 * pattern, small mentally-solvable numbers, exactly one correct option, three
 * plausible near-miss distractors, no duplicate options, and a puzzle that is
 * different from [previous] on retry.
 */
internal fun generatePuzzle(round: Int, previous: SequencePuzzle?): SequencePuzzle {
    val rng = Random
    var candidate: SequencePuzzle
    var guard = 0
    do {
        candidate = buildPuzzle(round.coerceIn(1, SEQ_ROUNDS), rng)
        guard++
    } while (previous != null && candidate.terms == previous.terms && guard < 25)
    return candidate
}

/** Builds one templated puzzle for the given round. */
private fun buildPuzzle(round: Int, rng: Random): SequencePuzzle {
    val (terms, answer) = when (round) {
        1 -> easyTemplate(rng) // + / − arithmetic step
        2 -> multiplyTemplate(rng) // × / ÷ step
        3 -> mixedTemplate(rng) // random pick of easy or multiply with fresh params
        4 -> alternatingTemplate(rng) // two-step alternating ops
        else -> hardTemplate(rng) // steeper growth / division chains / growing steps
    }

    val missingIndex = rng.nextInt(terms.size - 1).coerceIn(1, terms.size - 2) // never first or last
    val shown: List<Int?> = terms.mapIndexed { idx, v -> if (idx == missingIndex) null else v }
    val options = buildOptions(answer, rng)
    return SequencePuzzle(terms = shown, answer = answer, options = options)
}

/** Round 1: constant +step or −step, 5 terms, small numbers. */
private fun easyTemplate(rng: Random): Pair<List<Int>, Int> {
    return if (rng.nextBoolean()) {
        val start = rng.nextInt(1, 12)
        val step = rng.nextInt(1, 7)
        val t = listOf(start, start + step, start + 2 * step, start + 3 * step, start + 4 * step)
        t to t[3]
    } else {
        val start = rng.nextInt(13, 24)
        val step = rng.nextInt(1, 7)
        val t = listOf(start, start - step, start - 2 * step, start - 3 * step, start - 4 * step)
        t to t[3]
    }
}

/** Round 2: ×step or ÷step, 5 terms, kept small. */
private fun multiplyTemplate(rng: Random): Pair<List<Int>, Int> {
    return if (rng.nextBoolean()) {
        val start = rng.nextInt(1, 4)
        val step = rng.nextInt(2, 5) // 2..4
        val t = listOf(start, start * step, start * step * step, start * step * step * step, start * step * step * step * step)
        t to t[3]
    } else {
        val step = rng.nextInt(2, 4) // 2..3
        val last = 1
        val t = listOf(last * step * step * step * step, last * step * step * step, last * step * step, last * step, last)
        t to t[3]
    }
}

/** Round 3: mixed — either pattern family with randomized parameters. */
private fun mixedTemplate(rng: Random): Pair<List<Int>, Int> =
    if (rng.nextBoolean()) easyTemplate(rng) else multiplyTemplate(rng)

/** Round 4: alternating two-step patterns like +3,−1,+3,−1,… or ×2,−2,×2,… */
private fun alternatingTemplate(rng: Random): Pair<List<Int>, Int> {
    return if (rng.nextBoolean()) {
        val up = rng.nextInt(2, 6)
        val down = rng.nextInt(1, up) // keeps numbers drifting upward gently
        var v = rng.nextInt(1, 6)
        val t = buildList {
            repeat(5) {
                add(v)
                v = if (it % 2 == 0) v + up else v - down
            }
        }
        t to t[4]
    } else {
        val times = rng.nextInt(2, 4)
        val down = rng.nextInt(1, 4)
        var v = rng.nextInt(2, 6)
        val t = buildList {
            repeat(5) {
                add(v)
                v = if (it % 2 == 0) v * times else v - down
            }
        }
        t to t[4]
    }
}

/** Round 5: harder but mental — steep × chains, ÷ chains, or growing steps. */
private fun hardTemplate(rng: Random): Pair<List<Int>, Int> {
    return when (rng.nextInt(3)) {
        0 -> { // steeper × chain: 3 → 6 → 12 → 24 → 48
            val start = rng.nextInt(1, 4)
            val step = rng.nextInt(2, 4)
            val t = listOf(start, start * step, start * step * step, start * step * step * step, start * step * step * step * step)
            t to t[4]
        }
        1 -> { // ÷ chain: 81 → 27 → 9 → 3 → 1
            val base = listOf(3, 4)[rng.nextInt(2)]
            val p = rng.nextInt(2, 5) // exponent 2..4
            val t = (p downTo 0).map { pow(base, it) }
            t to t[3]
        }
        else -> { // growing +step: 4 → 7 → 13 → 22 → 34 (+3, +6, +9, +12)
            var v = rng.nextInt(2, 6)
            var step = rng.nextInt(2, 5)
            val t = buildList {
                repeat(5) {
                    add(v)
                    v += step
                    step += rng.nextInt(2, 5)
                }
            }
            t to t[4]
        }
    }
}

private fun pow(base: Int, exp: Int): Int {
    var result = 1
    repeat(exp) { result *= base }
    return result
}

/** Builds 4 unique options: the answer + 3 plausible near-misses. */
private fun buildOptions(answer: Int, rng: Random): List<Int> {
    val options = mutableSetOf(answer)
    val deltas = listOf(-3, -2, -1, 1, 2, 3, 5, -5).shuffled(rng)
    var i = 0
    while (options.size < ANSWER_COUNT && i < deltas.size) {
        val candidate = answer + deltas[i]
        if (candidate > 0 && candidate != answer) options.add(candidate)
        i++
    }
    // Fallbacks for small answers where deltas collided.
    var extra = answer + 4
    while (options.size < ANSWER_COUNT) {
        if (extra > 0 && extra != answer) options.add(extra)
        extra++
    }
    return options.toList().shuffled(rng)
}
