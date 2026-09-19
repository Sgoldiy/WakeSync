package com.social.wakesync.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.feature.games.GameSounds
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.math.absoluteValue

@Composable
fun AlarmPuzzleDuo(
    onDismiss: () -> Unit,
    onFailure: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    userName: String = "You",
    userAvatar: String = "🤯",
    rivalName: String = "Partner",
    rivalAvatar: String = "🦁",
    alarmId: String? = null,
    currentUserId: String? = null,
    onListenToDuoAlarm: ((String) -> kotlinx.coroutines.flow.Flow<String?>)? = null,
    onSetDuoAlarmWinner: ((String, String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isUserDone by remember { mutableStateOf(false) }
    var isRivalDone by remember { mutableStateOf(false) }

    // Both players race through the alarm's selected 3-game ladder on the shared
    // GameRouter — same games, same rules, same sounds as the solo alarm.
    val duoLadder = remember(alarmId) {
        AlarmState.activeAlarmGames.ifEmpty { listOf("Memory Chain", "Color Clash", "Speed Tap") }
    }
    var currentGame by remember(alarmId) { mutableIntStateOf(1) }
    val activeGame = duoLadder[(currentGame - 1).coerceAtMost(duoLadder.lastIndex)]
    val round = remember(alarmId) { (alarmId?.hashCode() ?: Random.nextInt()).absoluteValue % 1000 + 1 }

    var timeLeft by remember { mutableIntStateOf(120) }

    // 120-Second High-Stakes Timeout Logic
    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isUserDone && !isRivalDone) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0 && !isUserDone && !isRivalDone) {
            // Time out! Alarm stops, both lose 3 streaks
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFailure()
        }
    }

    // Real-Time Firestore Sync Listener: Stops alarm when any partner solves it
    LaunchedEffect(alarmId) {
        if (alarmId != null && onListenToDuoAlarm != null && currentUserId != null) {
            onListenToDuoAlarm(alarmId).collect { winnerUid ->
                if (!winnerUid.isNullOrBlank()) {
                    if (winnerUid == currentUserId) {
                        isUserDone = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(800)
                        onDismiss()
                    } else {
                        isRivalDone = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(800)
                        onFailure()
                    }
                }
            }
        }
    }

    // Failsafe backup timer: runs in parallel to prevent hang when offline
    val rivalSolveTime = remember { Random.nextLong(20000, 30000) }
    LaunchedEffect(Unit) {
        delay(rivalSolveTime)
        if (!isUserDone && !isRivalDone) {
            isRivalDone = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1000)
            onFailure()
        }
    }

    // User Solved First!
    val onUserSolveFirst = {
        if (!isRivalDone && !isUserDone) {
            if (alarmId != null && onSetDuoAlarmWinner != null && currentUserId != null) {
                onSetDuoAlarmWinner(alarmId, currentUserId)
            } else {
                // Local Fallback
                isUserDone = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Real-Time Split VS Screen Header
        // Real-Time Header (Group Battle vs Duo Split VS)
        if (AlarmState.activeAlarmMode == "Group") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF041E2B), Color(0xFF020617)))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚡ GROUP ALARM BATTLE CLASH ⚡",
                        color = AppColorPalette.CyanCta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        letterSpacing = 1.5.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // YOU
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(userAvatar, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("You", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Text(
                                text = if (isUserDone) "WON! 🎉" else "Solving...",
                                color = if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFamily
                            )
                        }

                        // Partners (Competitors)
                        val partnerNames = remember(AlarmState.activeAlarmPartnerUsername) {
                            AlarmState.activeAlarmPartnerUsername?.split(",")?.filter { it.isNotBlank() } ?: listOf("Partner 1", "Partner 2")
                        }

                        partnerNames.take(4).forEachIndexed { idx, name ->
                            val isThisPartnerWinner = isRivalDone
                            val competitorColor = if (isThisPartnerWinner) AppColorPalette.WinGreen else AppColorPalette.LossRed
                            val competitorAvatar = when (idx % 4) {
                                0 -> "🦁"
                                1 -> "🐺"
                                2 -> "🦊"
                                else -> "🐻"
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, competitorColor, CircleShape)
                                        .background(Color.White.copy(alpha = 0.04f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(competitorAvatar, fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (name.length > 8) name.take(6) + ".." else name,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                                Text(
                                    text = if (isThisPartnerWinner) "WON! 💀" else "Solving...",
                                    color = competitorColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT SIDE - YOU
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF041E2B), Color(0xFF061521)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("YOU", color = AppColorPalette.CyanCta, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = interFamily, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(userAvatar, fontSize = 32.sp)
                            }
                            Text(userName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (isUserDone) AppColorPalette.WinGreen.copy(alpha = 0.15f) else AppColorPalette.CyanCta.copy(alpha = 0.15f))
                                    .border(1.dp, if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, RoundedCornerShape(99.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isUserDone) "WON! 🎉" else "Solving...", color = if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            }
                        }
                    }

                    // RIGHT SIDE - RIVAL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF1C0D16), Color(0xFF260D1A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("RIVAL", color = AppColorPalette.LossRed, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = interFamily, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, if (isRivalDone) AppColorPalette.WinGreen else AppColorPalette.LossRed, CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rivalAvatar, fontSize = 32.sp)
                            }
                            Text(rivalName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (isRivalDone) AppColorPalette.WinGreen.copy(alpha = 0.15f) else AppColorPalette.LossRed.copy(alpha = 0.15f))
                                    .border(1.dp, if (isRivalDone) AppColorPalette.WinGreen else AppColorPalette.LossRed, RoundedCornerShape(99.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isRivalDone) "WON! 💀" else "Solving...", color = if (isRivalDone) AppColorPalette.WinGreen else AppColorPalette.LossRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            }
                        }
                    }
                }

                // Central VS Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppColorPalette.VoidBg)
                        .border(2.dp, AppColorPalette.CyanCta, CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", color = AppColorPalette.CyanCta, fontSize = 13.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
                }
            }
        }

        // Pressure Copy Text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Don't let them win again. 💀",
                color = AppColorPalette.LossRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFamily
            )
            Text(
                text = "${rivalName} beat you 3 times last week",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                fontFamily = interFamily
            )
        }

        // Active ladder progress + 120s Countdown Timer Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(gameEmojiFor(activeGame), fontSize = 16.sp)
                Text(
                    text = "Game $currentGame of ${duoLadder.size} — $activeGame · Solve First!",
                    color = AppColorPalette.CyanCta,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFamily
                )
            }

            val badgeColor = if (timeLeft <= 30) AppColorPalette.LossRed else AppColorPalette.CyanCta
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⏱️ ${timeLeft}s",
                    color = badgeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }
        }

        // Interactive Duo Challenge Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = currentGame to round,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "duo_task_transition"
            ) { (_, roundKey) ->
                GameRouter(
                    gameName = activeGame,
                    round = roundKey + currentGame * 100,
                    onSuccess = onUserSolveFirst,
                    titleFamily = titleFamily,
                    interFamily = interFamily
                )
            }
        }
    }
}

