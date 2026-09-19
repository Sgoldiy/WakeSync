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
import androidx.compose.runtime.key
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
import com.social.wakesync.feature.games.ChainMemoryGame
import com.social.wakesync.feature.games.GameSounds
import com.social.wakesync.feature.games.ColorClashGame
import com.social.wakesync.feature.games.NumberHuntGame
import com.social.wakesync.feature.games.NumberSequenceGame
import com.social.wakesync.feature.games.OddOneOutGame
import com.social.wakesync.feature.games.OrbFocusGame
import com.social.wakesync.feature.games.RapidBedTapGame
import com.social.wakesync.feature.games.ReactionRushGame
import com.social.wakesync.feature.games.ShakeEnergyGame
import com.social.wakesync.feature.games.SlidingTilesGame
import com.social.wakesync.feature.games.SpeedTapGame
import com.social.wakesync.feature.games.TargetTapGame
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay

/**
 * Full-screen practice demo — plays ONE game at a time (the one the user tapped 🎮 Try on).
 * Stopwatch + victory panel with Play Again (fresh random task each round).
 */
@Composable
fun GameDemoScreen(
    onDismiss: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    gameName: String
) {
    val haptic = LocalHapticFeedback.current
    val game = remember(gameName) { gameName.ifEmpty { "Memory Chain" } }

    // Round bumps re-seed the game's random state (fresh task every replay).
    var round by remember(game) { mutableIntStateOf(1) }
    var solved by remember(game) { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(game, round, solved) {
        elapsedSeconds = 0
        while (!solved) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val onSolved: () -> Unit = {
        GameSounds.spark()
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
            // ── Top Bar: Exit + PRACTICE DEMO badge ────────────────────────────────
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
                    Text(gameEmojiFor(game), fontSize = 14.sp)
                    Text(
                        "PRACTICE DEMO",
                        color = AppColorPalette.CyanCta,
                        fontSize = 11.sp, fontWeight = FontWeight.Black,
                        fontFamily = titleFamily, letterSpacing = 1.sp
                    )
                }
            }

            // ── Header: game name + stopwatch ──────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                if (solved) {
                    Text("🏆 SOLVED!", color = AppColorPalette.WinGreen, fontSize = 32.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
                    Text("⏱️ $elapsedSeconds s", color = AppColorPalette.CyanCta, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
                } else {
                    Text(
                        game,
                        color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily
                    )
                    Text("⏱️ ${elapsedSeconds}s", color = AppColorPalette.CyanCta, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
                }
            }

            // ── Play Area ──────────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (solved) {
                    VictoryPanel(
                        game = game,
                        elapsedSeconds = elapsedSeconds,
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
                        label = "demo_round_transition"
                    ) { roundKey ->
                        GameRouter(
                            gameName = game,
                            round = roundKey,
                            onSuccess = onSolved,
                            titleFamily = titleFamily,
                            interFamily = interFamily
                        )
                    }
                }
            }

            Text(
                text = "🎮 This is exactly what will greet you when the alarm fires",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily,
                modifier = Modifier.padding(bottom = 18.dp)
            )
        }
    }
}

/** Routes a game name to its composable — the single source of truth used by demo & alarm lock. */
@Composable
fun GameRouter(
    gameName: String,
    round: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val n = gameName.trim()
    when {
        n.contains("Memory Chain", ignoreCase = true) || n.contains("Chain", ignoreCase = true) ->
            ChainMemoryGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Sequence", ignoreCase = true) ->
            key(round) { NumberSequenceGame(onGameCompleted = onSuccess, titleFamily = titleFamily, interFamily = interFamily) }
        n.contains("Stroop", ignoreCase = true) || n.contains("Color", ignoreCase = true) ->
            ColorClashGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Target", ignoreCase = true) ->
            key(round) { TargetTapGame(onGameCompleted = onSuccess, titleFamily = titleFamily, interFamily = interFamily) }
        n.contains("Shake", ignoreCase = true) || n.contains("Charge", ignoreCase = true) ->
            ShakeEnergyGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Speed Tap", ignoreCase = true) || n.contains("Tap 1", ignoreCase = true) ->
            SpeedTapGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Odd", ignoreCase = true) ->
            OddOneOutGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Sliding", ignoreCase = true) || n.contains("Tile", ignoreCase = true) ->
            SlidingTilesGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Orb", ignoreCase = true) || n.contains("Focus", ignoreCase = true) ->
            OrbFocusGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Rapid", ignoreCase = true) || n.contains("Sprint", ignoreCase = true) || n.contains("Bed Tap", ignoreCase = true) ->
            RapidBedTapGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Reaction", ignoreCase = true) ->
            ReactionRushGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        n.contains("Number Hunt", ignoreCase = true) || n.contains("Hunt", ignoreCase = true) ->
            NumberHuntGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
        else -> ChainMemoryGame(onSuccess = onSuccess, round = round, titleFamily = titleFamily, interFamily = interFamily)
    }
}

fun gameEmojiFor(name: String): String = when {
    name.contains("Memory Chain", true) || name.contains("Chain", true) -> "🧠"
    name.contains("Pattern", true) || name.contains("Sequence", true) -> "🧩"
    name.contains("Stroop", true) || name.contains("Color", true) || name.contains("Target", true) -> "🎯"
    name.contains("Word", true) -> "🔤"
    name.contains("Shake", true) || name.contains("Charge", true) -> "📱"
    name.contains("Speed Tap", true) -> "🔢"
    name.contains("Odd", true) -> "🔍"
    name.contains("Sliding", true) || name.contains("Tile", true) -> "🧱"
    name.contains("Orb", true) || name.contains("Focus", true) -> "🎯"
    name.contains("Rapid", true) || name.contains("Sprint", true) -> "👆"
    name.contains("Reaction", true) -> "⚡"
    name.contains("Hunt", true) -> "🧮"
    else -> "🎮"
}

@Composable
private fun VictoryPanel(
    game: String,
    elapsedSeconds: Int,
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
        Text("SOLVED!", color = AppColorPalette.WinGreen, fontSize = 40.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
        Text("$game · $elapsedSeconds s", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(AppColorPalette.CyanCta)
                .clickable { onPlayAgain() }
                .padding(horizontal = 32.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("🔁 Play Again", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = titleFamily)
        }
        Text(
            "← Back to alarm setup",
            color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily,
            modifier = Modifier.clickable { onExit() }
        )
    }
}
