package com.social.wakesync.feature.home

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

    var mathProblem by remember { mutableStateOf(generateGroupMathProblem()) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isUserDone by remember { mutableStateOf(false) }

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

    fun handleOptionSelected(option: Int) {
        if (isUserDone) return
        selectedOption = option
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        if (option == mathProblem.answer) {
            isUserDone = true
            // Update User status in list
            participants = participants.map {
                if (it.id == "3") it.copy(state = GroupSolverState.Finished("1:14")) else it
            }
            coroutineScope.launch {
                delay(1200) // Delay to show finish state
                onDismiss()
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

        // Math Puzzle Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Math Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131829)),
                border = BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mathProblem.question,
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = titleFamily
                    )
                }
            }

            // Options grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val row1 = mathProblem.options.take(2)
                val row2 = mathProblem.options.drop(2)

                listOf(row1, row2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { option ->
                            val isCorrect = option == mathProblem.answer
                            val isSelectedValue = selectedOption == option

                            val cardBorderColor = when {
                                isSelectedValue && isCorrect -> AppColorPalette.WinGreen
                                isSelectedValue && !isCorrect -> AppColorPalette.LossRed
                                else -> Color.White.copy(alpha = 0.08f)
                            }
                            
                            val cardBgColor = when {
                                isSelectedValue && isCorrect -> AppColorPalette.WinGreen.copy(alpha = 0.08f)
                                isSelectedValue && !isCorrect -> AppColorPalette.LossRed.copy(alpha = 0.08f)
                                else -> Color(0xFF131829)
                            }

                            val textColor = when {
                                isSelectedValue && isCorrect -> AppColorPalette.WinGreen
                                isSelectedValue && !isCorrect -> AppColorPalette.LossRed
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBgColor)
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                                    .clickable(enabled = selectedOption == null) {
                                        handleOptionSelected(option)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.toString(),
                                    color = textColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = titleFamily
                                )
                            }
                        }
                    }
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

data class GroupMathProblem(
    val question: String,
    val answer: Int,
    val options: List<Int>
)

fun generateGroupMathProblem(): GroupMathProblem {
    // Generate subtraction problem e.g. 91 - 47
    val num1 = (50..99).random()
    val num2 = (11..49).random()
    val answer = num1 - num2
    
    // Generate 3 incorrect options around the answer
    val options = mutableSetOf(answer)
    while (options.size < 4) {
        val diff = listOf(-10, -5, -2, -1, 1, 2, 5, 10, 20).random()
        val option = answer + diff
        if (option > 0) {
            options.add(option)
        }
    }
    
    return GroupMathProblem(
        question = "$num1 - $num2",
        answer = answer,
        options = options.toList().shuffled()
    )
}
