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
import androidx.compose.foundation.layout.height
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

enum class SoloPuzzleType(val displayName: String, val emoji: String) {
    MATH("Speed Math", "🧮"),            // 1. Math Game
    MEMORY("Pattern Memory", "🧩"),        // 2. 3x3 Flashing Pattern Recall
    STROOP("Color Clash", "🎨"),           // 3. Stroop Color Conflict Test
    WORD_UNSCRAMBLE("Word Scramble", "🔤"), // 4. Anagram Unscramble
    SHAKE("Shake Energy", "📱"),          // 5. Rapid Motion Energy Bar
    NUMBER_ORDER("Speed Tap 1-6", "🔢"),   // 6. 1-to-6 Ascending Tap
    ODD_ONE_OUT("Odd One Out", "🔍"),      // 7. Visual Intruder Search
    SLIDING_TILE("Sliding Tiles", "🧱"),    // 8. 1-2-3 Tile Sequence Arrange
    BALANCE_MAZE("Orb Focus", "🎯"),        // 9. Bed-friendly Orb Center Touch Focus
    BED_TAP("Rapid Bed Tap", "👆")         // 10. Bed-friendly Finger Tap Sprint
}

@Composable
fun AlarmPuzzleSolo(
    onDismiss: () -> Unit,
    onFailure: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
    challengeName: String = "Math",
    mathDifficulty: String = "Medium",
    isPracticeDemo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var timeString by remember { mutableStateOf("06:30") }
    var currentStage by remember { mutableIntStateOf(1) }

    var attemptNumber by remember { mutableIntStateOf(1) } // 1 or 2
    var secondsLeft by remember { mutableIntStateOf(60) }
    var activePuzzleType by remember(challengeName) {
        mutableStateOf(getPuzzleTypeFromName(challengeName))
    }

    val totalStages = remember(activePuzzleType, mathDifficulty) {
        if (activePuzzleType == SoloPuzzleType.MATH) {
            when (mathDifficulty) {
                "Easy" -> 1
                "Hard" -> 3
                else -> 2
            }
        } else {
            1
        }
    }

    // Dynamic Clock Updater
    LaunchedEffect(Unit) {
        while (true) {
            val nowTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val rawHour = nowTime.hour
            val hour = if (rawHour == 0) 12 else if (rawHour > 12) rawHour - 12 else rawHour
            val min = nowTime.minute.toString().padStart(2, '0')
            timeString = "$hour:$min"
            delay(1000)
        }
    }

    // 60-Second Countdown Timer Loop (Disabled in Practice Demo Mode)
    LaunchedEffect(attemptNumber, activePuzzleType, currentStage, isPracticeDemo) {
        if (!isPracticeDemo) {
            secondsLeft = 60
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }

            if (attemptNumber == 1) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                attemptNumber = 2
                currentStage = 1
                val availablePuzzles = SoloPuzzleType.values().filter { it != activePuzzleType }
                activePuzzleType = availablePuzzles.random()
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFailure()
            }
        }
    }

    val onStageSuccess = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (currentStage >= totalStages) {
            onDismiss()
        } else {
            currentStage++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Practice Demo Top Bar
            if (isPracticeDemo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        Text("Exit Practice", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
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
                        Text("🎮", fontSize = 14.sp)
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
            }

            // Live Clock & Attempt Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "Wake up. Don't choke.",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    fontFamily = interFamily
                )
                
                // 60s Countdown Timer Badge & Attempt Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (secondsLeft <= 10) AppColorPalette.LossRed.copy(alpha = 0.2f) else AppColorPalette.Surface)
                        .border(1.dp, if (secondsLeft <= 10) AppColorPalette.LossRed else AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⏱️ ${secondsLeft}s",
                        color = if (secondsLeft <= 10) AppColorPalette.LossRed else AppColorPalette.CyanCta,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "· Attempt $attemptNumber of 2",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            // Task Name Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = activePuzzleType.emoji, fontSize = 16.sp)
                Text(
                    text = if (activePuzzleType == SoloPuzzleType.MATH) 
                        "${activePuzzleType.displayName} · Question $currentStage of $totalStages"
                    else 
                        "${activePuzzleType.displayName} Challenge",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFamily
                )
            }

            // Progress Bar (Only for multi-question Math)
            if (activePuzzleType == SoloPuzzleType.MATH && totalStages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (stage in 1..totalStages) {
                        val isDone = stage < currentStage
                        val isCurrent = stage == currentStage
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    when {
                                        isDone -> AppColorPalette.WinGreen
                                        isCurrent -> AppColorPalette.CyanCta
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }

            // Solve Label
            Text(
                text = "SOLVE TO DISMISS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFamily,
                letterSpacing = 1.sp
            )

            // Active Task Component - Routed to Standalone Game Files
            AnimatedContent(
                targetState = Pair(activePuzzleType, currentStage),
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "task_transition"
            ) { (_, stage) ->
                when (activePuzzleType) {
                    SoloPuzzleType.MATH -> SpeedMathGame(stage = stage, onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.MEMORY -> PatternMemoryGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.STROOP -> ColorClashGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.WORD_UNSCRAMBLE -> WordScrambleGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.SHAKE -> ShakeEnergyGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.NUMBER_ORDER -> SpeedTapGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.ODD_ONE_OUT -> OddOneOutGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.SLIDING_TILE -> SlidingTilesGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.BALANCE_MAZE -> OrbFocusGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.BED_TAP -> RapidBedTapGame(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                }
            }
        }
    }
}

internal fun getPuzzleTypeFromName(name: String): SoloPuzzleType {
    val clean = name.trim()
    return when {
        clean.contains("Memory", ignoreCase = true) -> SoloPuzzleType.MEMORY
        clean.contains("Stroop", ignoreCase = true) || clean.contains("Color", ignoreCase = true) -> SoloPuzzleType.STROOP
        clean.contains("Word", ignoreCase = true) || clean.contains("Scramble", ignoreCase = true) -> SoloPuzzleType.WORD_UNSCRAMBLE
        clean.contains("Shake", ignoreCase = true) -> SoloPuzzleType.SHAKE
        clean.contains("Speed Tap", ignoreCase = true) || clean.contains("Tap 1-6", ignoreCase = true) || clean.contains("Order", ignoreCase = true) -> SoloPuzzleType.NUMBER_ORDER
        clean.contains("Odd", ignoreCase = true) || clean.contains("Intruder", ignoreCase = true) -> SoloPuzzleType.ODD_ONE_OUT
        clean.contains("Sliding", ignoreCase = true) || clean.contains("Tile", ignoreCase = true) -> SoloPuzzleType.SLIDING_TILE
        clean.contains("Orb", ignoreCase = true) || clean.contains("Focus", ignoreCase = true) || clean.contains("Balance", ignoreCase = true) -> SoloPuzzleType.BALANCE_MAZE
        clean.contains("Rapid", ignoreCase = true) || clean.contains("Bed", ignoreCase = true) -> SoloPuzzleType.BED_TAP
        else -> SoloPuzzleType.MATH
    }
}
