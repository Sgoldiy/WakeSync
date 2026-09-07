package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.utils.BackHandler
import com.social.wakesync.ui.theme.AppColorPalette

data class ProofVerifier(
    val avatar: String,
    val username: String,
    val vote: String? // "legit", "suspicious", or null (pending)
)

@Composable
fun ProofReviewScreen(
    submitterAvatar: String = "😤",
    submitterName: String = "nocturnaljake",
    taskName: String = "20 Pushups",
    timeAgo: String = "12 min ago",
    verifiers: List<ProofVerifier> = listOf(
        ProofVerifier("🦁", "maya.rises", "legit"),
        ProofVerifier("🐺", "5amclub_dani", null),
        ProofVerifier("🐻", "grind.rio", "legit")
    ),
    onLegit: () -> Unit,
    onSuspicious: () -> Unit,
    onBack: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var userVote by remember { mutableStateOf<String?>(null) }
    val legitCount = verifiers.count { it.vote == "legit" }
    val suspiciousCount = verifiers.count { it.vote == "suspicious" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Proof Review",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily
                )
            }

            // Submitter Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColorPalette.DeepSurface)
                        .border(2.dp, AppColorPalette.WinGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = submitterAvatar, fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = submitterName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "$taskName · $timeAgo",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Proof Photo Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏋️", fontSize = 48.sp)
                    Text(
                        text = "proof photo",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vote Counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Verified Count
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColorPalette.WinGreen.copy(alpha = 0.08f))
                        .border(1.dp, AppColorPalette.WinGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👍", fontSize = 16.sp)
                        Text(
                            text = "$legitCount",
                            color = AppColorPalette.WinGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily
                        )
                        Text(
                            text = "Verified",
                            color = AppColorPalette.WinGreen.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = interFamily
                        )
                    }
                }

                // Suspicious Count
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColorPalette.LossRed.copy(alpha = 0.08f))
                        .border(1.dp, AppColorPalette.LossRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🤔", fontSize = 16.sp)
                        Text(
                            text = "$suspiciousCount",
                            color = AppColorPalette.LossRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily
                        )
                        Text(
                            text = "Suspicious",
                            color = AppColorPalette.LossRed.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = interFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verifier List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(verifiers) { verifier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColorPalette.Surface)
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppColorPalette.DeepSurface)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = verifier.avatar, fontSize = 16.sp)
                            }
                            Text(
                                text = verifier.username,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }

                        Text(
                            text = when (verifier.vote) {
                                "legit" -> "👍"
                                "suspicious" -> "🤔"
                                else -> "⏳"
                            },
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        userVote = "legit"
                        onLegit()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userVote == "legit") AppColorPalette.WinGreen else AppColorPalette.WinGreen.copy(alpha = 0.12f),
                        contentColor = if (userVote == "legit") Color.Black else AppColorPalette.WinGreen
                    ),
                    border = BorderStroke(1.dp, AppColorPalette.WinGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "👍 Legit",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                }

                Button(
                    onClick = {
                        userVote = "suspicious"
                        onSuspicious()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userVote == "suspicious") AppColorPalette.LossRed else AppColorPalette.LossRed.copy(alpha = 0.12f),
                        contentColor = if (userVote == "suspicious") Color.White else AppColorPalette.LossRed
                    ),
                    border = BorderStroke(1.dp, AppColorPalette.LossRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "🤔 Suspicious",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
