package com.social.wakesync.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.feature.games.GameSounds
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AlarmPuzzleGroup(
    onDismiss: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    userName: String = "YOU",
    userAvatar: String = "🤯"
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Group participants simulation state
    var participants by remember {
        mutableStateOf(
            listOf(
                GroupParticipant("1", "5amclub_dani", "🐺", 89, GroupSolverState.Finished("0:42"), rank = 1, borderHighlight = null),
                GroupParticipant("2", "grind.rio", "🐻", 15, GroupSolverState.Finished("1:08"), rank = 2, borderHighlight = null),
                GroupParticipant("3", "$userName \u2190 you", userAvatar, 23, GroupSolverState.Solving, rank = 3, borderHighlight = AppColorPalette.CyanCta),
                GroupParticipant("4", "maya.rises", "🦊", 41, GroupSolverState.Solving, rank = 4, borderHighlight = null),
                GroupParticipant("5", "nocturnaleve", "🐱", 3, GroupSolverState.Solving, rank = 5, borderHighlight = AppColorPalette.LossRed.copy(alpha = 0.6f))
            )
        )
    }

    var isUserDone by remember { mutableStateOf(false) }

    // Group members all race through the alarm's selected 3-game ladder.
    val groupLadder = remember {
        AlarmState.activeAlarmGames.ifEmpty { listOf("Memory Chain", "Color Clash", "Speed Tap") }
    }
    var currentGame by remember { mutableIntStateOf(1) }
    val activeGame = groupLadder[(currentGame - 1).coerceAtMost(groupLadder.lastIndex)]

    // Simulation of others solving in background
    LaunchedEffect(Unit) {
        // maya.rises solves in 9 seconds
        delay(9000)
        if (!isUserDone) {
            participants = participants.map {
                if (it.id == "4") it.copy(state = GroupSolverState.Finished("1:32")) else it
            }
        }
        
        // nocturnaleve solves in 15 seconds
        delay(6000)
        if (!isUserDone) {
            participants = participants.map {
                if (it.id == "5") it.copy(state = GroupSolverState.Finished("1:48")) else it
            }
        }
    }

    fun handleGameSolved() {
        if (isUserDone) return
        GameSounds.chime()
        if (currentGame >= groupLadder.size) {
            isUserDone = true
            GameSounds.spark()
            // Update User status in list
            participants = participants.map {
                if (it.id == "3") it.copy(state = GroupSolverState.Finished("1:14")) else it
            }
            coroutineScope.launch {
                delay(1200) // Delay to show finish state
                onDismiss()
            }
        } else {
            currentGame++
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
        // Group Alarm Header
        Text(
            text = "Group Alarm",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.W900,
            fontFamily = titleFamily,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )

        // Leaderboard List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(participants) { participant ->
                ParticipantRow(participant = participant, interFamily = interFamily)
            }
        }

        // Warning Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF130E14))
                .border(width = 1.dp, color = AppColorPalette.LossRed.copy(alpha = 0.12f))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DON'T BE LAST 💀",
                color = AppColorPalette.LossRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFamily,
                letterSpacing = 0.5.sp
            )
        }

        // Game ladder section — the same games as solo, raced together
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(gameEmojiFor(activeGame), fontSize = 15.sp)
                    Text(
                        text = "Game $currentGame of ${groupLadder.size} — $activeGame",
                        color = AppColorPalette.CyanCta,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = interFamily
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                AnimatedContent(
                    targetState = currentGame,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "group_task_transition"
                ) { gameIdx ->
                    GameRouter(
                        gameName = groupLadder[(gameIdx - 1).coerceAtMost(groupLadder.lastIndex)],
                        round = gameIdx * 7,
                        onSuccess = { handleGameSolved() },
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                }
            }
        }
    }
}

@Composable
fun ParticipantRow(
    participant: GroupParticipant,
    interFamily: FontFamily
) {
    val cardBg = if (participant.borderHighlight == AppColorPalette.CyanCta) Color(0xFF0F1E2A) else Color(0xFF131829)
    val borderStroke = when (participant.borderHighlight) {
        null -> BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        else -> BorderStroke(1.5.dp, participant.borderHighlight)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank circle
            val rankColor = when (participant.rank) {
                1, 2 -> AppColorPalette.WinGreen
                3 -> AppColorPalette.CyanCta
                else -> Color.White.copy(alpha = 0.15f)
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.rank.toString(),
                    color = rankColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar with glowing border
            val avatarBorderColor = when (participant.state) {
                is GroupSolverState.Finished -> AppColorPalette.WinGreen
                GroupSolverState.Solving -> {
                    if (participant.borderHighlight == AppColorPalette.LossRed.copy(alpha = 0.6f)) AppColorPalette.LossRed
                    else AppColorPalette.CyanCta
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, avatarBorderColor, CircleShape)
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = participant.avatar, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Streak info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = participant.name,
                    color = if (participant.borderHighlight == AppColorPalette.CyanCta) AppColorPalette.CyanCta else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🔥", fontSize = 11.sp)
                    Text(
                        text = "${participant.streak} streak",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = interFamily
                    )
                }
            }

            // Status indicator badge
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        when (val state = participant.state) {
                            is GroupSolverState.Finished -> AppColorPalette.WinGreen.copy(alpha = 0.1f)
                            GroupSolverState.Solving -> Color.White.copy(alpha = 0.04f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when (val state = participant.state) {
                            is GroupSolverState.Finished -> AppColorPalette.WinGreen
                            GroupSolverState.Solving -> Color.White.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (val state = participant.state) {
                        is GroupSolverState.Finished -> "✓ ${state.time}"
                        GroupSolverState.Solving -> "solving..."
                    },
                    color = when (val state = participant.state) {
                        is GroupSolverState.Finished -> AppColorPalette.WinGreen
                        GroupSolverState.Solving -> Color.White.copy(alpha = 0.3f)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
            }
        }
    }
}

data class GroupParticipant(
    val id: String,
    val name: String,
    val avatar: String,
    val streak: Int,
    val state: GroupSolverState,
    val rank: Int,
    val borderHighlight: Color?
)

sealed interface GroupSolverState {
    data object Solving : GroupSolverState
    data class Finished(val time: String) : GroupSolverState
}
