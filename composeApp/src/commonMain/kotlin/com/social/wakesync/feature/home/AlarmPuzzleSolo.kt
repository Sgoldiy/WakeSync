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
import com.social.wakesync.feature.games.GameSounds
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun AlarmPuzzleSolo(
    onDismiss: () -> Unit,
    onFailure: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
    challengeName: String = "Memory Chain,Color Clash,Speed Tap",
    isPracticeDemo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var timeString by remember { mutableStateOf("06:30") }

    // ── 3-Game Ladder: the alarm is dismissed only after ALL selected games are solved.
    val soloLadder = remember(challengeName) {
        AlarmState.activeAlarmGames.ifEmpty { challengeName.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
            .ifEmpty { listOf("Memory Chain", "Color Clash", "Speed Tap") }
    }
    var currentGame by remember(soloLadder) { mutableIntStateOf(1) } // 1-based
    var ladderRound by remember(soloLadder) { mutableIntStateOf(1) } // bumps to re-seed randomness

    var attemptNumber by remember { mutableIntStateOf(1) } // 1 or 2
    var secondsLeft by remember { mutableIntStateOf(60) }
    val totalStages = soloLadder.size

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
    LaunchedEffect(attemptNumber, currentGame, isPracticeDemo) {
        if (!isPracticeDemo) {
            secondsLeft = 60
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }

            if (attemptNumber == 1) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                attemptNumber = 2
                currentGame = 1
                ladderRound++
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFailure()
            }
        }
    }

    val onStageSuccess = {
        if (currentGame >= totalStages) {
            GameSounds.spark() // final game solved — alarm dismissed!
            onDismiss()
        } else {
            GameSounds.chime() // ladder advance
            currentGame++
            ladderRound++
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
                Text(text = gameEmojiFor(soloLadder[currentGame - 1]), fontSize = 16.sp)
                Text(
                    text = "${soloLadder[currentGame - 1]} · Game $currentGame of $totalStages",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFamily
                )
            }

            // 3-Game Ladder Progress Bar
            if (totalStages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (stage in 1..totalStages) {
                        val isDone = stage < currentGame
                        val isCurrent = stage == currentGame
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

            // Active Task Component — runs the user's selected game for this stage of the ladder
            AnimatedContent(
                targetState = currentGame to ladderRound,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "task_transition"
            ) { (_, roundKey) ->
                GameRouter(
                    gameName = soloLadder[currentGame - 1],
                    round = roundKey,
                    onSuccess = { onStageSuccess() },
                    titleFamily = titleFamily,
                    interFamily = interFamily
                )
            }
        }
    }
}

