package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KickVoteBottomSheet(
    onDismiss: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    initiatorName: String = "5amclub_dani",
    endsInHours: String = "12h",
    targetUsername: String = "nocturnaleve",
    targetAvatar: String = "🐱",
    targetReason: String = "Missed 3 alarms · 0 punishments completed",
    initialYesVotes: Int = 3,
    initialNoVotes: Int = 0,
    neededVotes: String = "3/4"
) {
    var userVotedChoice by remember { mutableStateOf<String?>(null) } // null, "KEEP", "KICK"
    var yesCount by remember { mutableStateOf(initialYesVotes) }
    var noCount by remember { mutableStateOf(initialNoVotes) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.Surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header Title
            Text(
                text = "Kick Vote",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle info
            Text(
                text = "Initiated by $initiatorName · Ends in $endsInHours",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W400
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Member Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColorPalette.LossRed.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, AppColorPalette.LossRed.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppColorPalette.DeepSurface)
                            .border(2.dp, AppColorPalette.LossRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = targetAvatar,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Target Details Column
                    Column {
                        Text(
                            text = targetUsername,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W700,
                            fontFamily = interFamily
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = targetReason,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vote Count Tally Boxes (Yes, kick / No, keep)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Yes, kick Box
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.WinGreen.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, AppColorPalette.WinGreen.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = yesCount.toString(),
                            color = AppColorPalette.WinGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.W900,
                            fontFamily = titleFamily
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Yes, kick",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W500
                        )
                    }
                }

                // No, keep Box
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.LossRed.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, AppColorPalette.LossRed.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = noCount.toString(),
                            color = AppColorPalette.LossRed,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.W900,
                            fontFamily = titleFamily
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "No, keep",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtext status
            val statusMessage = when (userVotedChoice) {
                "KICK" -> "Need $neededVotes votes to kick · You voted to Kick 💀"
                "KEEP" -> "Need $neededVotes votes to kick · You voted to Keep 🤝"
                else -> "Need $neededVotes votes to kick · You haven't voted yet"
            }

            Text(
                text = statusMessage,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 13.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Keep 🤝 / Kick 💀)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Keep Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (userVotedChoice == "KEEP") AppColorPalette.WinGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f)
                        )
                        .border(
                            1.dp,
                            if (userVotedChoice == "KEEP") AppColorPalette.WinGreen else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (userVotedChoice == null) {
                                userVotedChoice = "KEEP"
                                noCount++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keep 🤝",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = interFamily
                    )
                }

                // Kick Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (userVotedChoice == "KICK") AppColorPalette.LossRed.copy(alpha = 0.3f) else AppColorPalette.LossRed.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            AppColorPalette.LossRed.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (userVotedChoice == null) {
                                userVotedChoice = "KICK"
                                yesCount++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kick 💀",
                        color = AppColorPalette.LossRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = interFamily
                    )
                }
            }
        }
    }
}
