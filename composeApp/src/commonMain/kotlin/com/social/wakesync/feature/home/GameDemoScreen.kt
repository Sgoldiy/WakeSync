package com.social.wakesync.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.feature.games.ColorClashGame
import com.social.wakesync.feature.games.OddOneOutGame
import com.social.wakesync.feature.games.OrbFocusGame
import com.social.wakesync.feature.games.PatternMemoryGame
import com.social.wakesync.feature.games.RapidBedTapGame
import com.social.wakesync.feature.games.ShakeEnergyGame
import com.social.wakesync.feature.games.SlidingTilesGame
import com.social.wakesync.feature.games.SpeedMathGame
import com.social.wakesync.feature.games.SpeedTapGame
import com.social.wakesync.feature.games.WordScrambleGame
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay

/**
 * Full-screen practice demo arcade. Every game is fully playable, and on success
 * the player gets a "Play Again" screen so they can instantly replay with a brand-new
 * randomly generated task (no repeats between rounds).
 */
@Composable
fun GameDemoScreen(
    onDismiss: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    challengeName: String,
    mathDifficulty: String = "Medium"
) {
    val haptic = LocalHapticFeedback.current

    // Resolved once per entry; "Mystery" picks a random game.
    val puzzleType = remember(challengeName) { getPuzzleTypeFromName(challengeName) }

    // Round counter — bumping it re-seeds every game's `remember(round)` state,
    // so each replay generates a completely fresh random task.
    var round by remember { mutableIntStateOf(1) }
    var solved by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Stop-watch: runs while playing, freezes on solve.
    LaunchedEffect(round, solved) {
        elapsedSeconds = 0
        while (!solved) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val onSolved: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        solved = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Top Bar: Exit + Game identity + stopwatch ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("←", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Exit", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColorPalette.CyanCta.copy(alpha = 0.15f))
                        .border(1.dp, AppColorPalette.CyanCta, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(puzzleType.emoji, fontSize = 14.sp)
                    Text(
                        text = "PRACTICE DEMO",
                        color = AppColorPalette.CyanCta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = titleFamily,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ── Header: game name + stopwatch ──────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = puzzleType.displayName,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily
                )
                Text(
                    text = "⏱️ ${elapsedSeconds}s · Round $round",
                    color = AppColorPalette.CyanCta,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }

            // ── Play Area ──────────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (solved) {
                    VictoryPanel(
                        puzzleType = puzzleType,
                        elapsedSeconds = elapsedSeconds,
                        round = round,
                        onPlayAgain = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            round++
                            solved = false
                        },
                        onExit = onDismiss,
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                } else {
                    AnimatedContent(
                        targetState = round,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                        label = "game_round_transition"
                    ) { roundKey ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "SOLVE IT — FULL GAME, REAL RULES",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFamily,
                                letterSpacing = 1.sp
                            )
                            when (puzzleType) {
                                SoloPuzzleType.MATH -> SpeedMathGame(stage = roundKey, onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily)
                                SoloPuzzleType.MEMORY -> PatternMemoryGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily, round = roundKey)
                                SoloPuzzleType.STROOP -> ColorClashGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily)
                                SoloPuzzleType.WORD_UNSCRAMBLE -> WordScrambleGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily, round = roundKey)
                                SoloPuzzleType.SHAKE -> ShakeEnergyGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily)
                                SoloPuzzleType.NUMBER_ORDER -> SpeedTapGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily, round = roundKey)
                                SoloPuzzleType.ODD_ONE_OUT -> OddOneOutGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily, round = roundKey)
                                SoloPuzzleType.SLIDING_TILE -> SlidingTilesGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily, round = roundKey)
                                SoloPuzzleType.BALANCE_MAZE -> OrbFocusGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily)
                                SoloPuzzleType.BED_TAP -> RapidBedTapGame(onSuccess = onSolved, titleFamily = titleFamily, interFamily = interFamily)
                            }
                        }
                    }
                }
            }

            // ── Bottom hint ────────────────────────────────────────────────────────
            Text(
                text = "🎮 This is exactly what will greet you when the alarm fires",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily,
                modifier = Modifier.padding(bottom = 18.dp)
            )
        }
    }
}

@Composable
private fun VictoryPanel(
    puzzleType: SoloPuzzleType,
    elapsedSeconds: Int,
    round: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("🏆", fontSize = 72.sp)
        Text(
            text = "SOLVED!",
            color = AppColorPalette.WinGreen,
            fontSize = 40.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily
        )
        Text(
            text = "${puzzleType.emoji} ${puzzleType.displayName} · $elapsedSeconds s",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily
        )

        // Play Again → re-seeds the game with a fresh random task
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppColorPalette.CyanCta)
                .clickable { onPlayAgain() }
                .padding(horizontal = 32.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔁 Play Again",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = titleFamily
            )
        }

        Text(
            text = "← Back to alarm setup",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            modifier = Modifier.clickable { onExit() }
        )
    }
}
