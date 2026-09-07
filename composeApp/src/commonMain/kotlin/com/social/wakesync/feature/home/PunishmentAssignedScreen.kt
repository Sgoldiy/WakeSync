package com.social.wakesync.feature.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.utils.BackHandler
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun PunishmentAssignedScreen(
    taskName: String = "20 Pushups 💪",
    taskDescription: String = "Photo proof required — front facing, all the way down",
    timeLeft: String = "1h 47m left",
    watchingFriends: List<String> = listOf("🦁", "🐺", "🐻"),
    onCompleteProof: () -> Unit,
    onUseInsurance: () -> Unit,
    onBackToHome: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToHome() }
    val infiniteTransition = rememberInfiniteTransition(label = "punishment_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            // Warning Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFFFF8A3D).copy(alpha = 0.12f))
                    .border(2.dp, Color(0xFFFF8A3D).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Punishment\nAssigned",
                color = AppColorPalette.LossRed,
                fontSize = 36.sp,
                fontWeight = FontWeight.W800,
                fontFamily = titleFamily,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Task Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "YOUR TASK",
                    color = AppColorPalette.LossRed.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily,
                    letterSpacing = 1.sp
                )
                Text(
                    text = taskName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = titleFamily
                )
                Text(
                    text = taskDescription,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    fontFamily = interFamily,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppColorPalette.LossRed, AppColorPalette.StreakFireStart)
                                )
                            )
                    )
                }

                Text(
                    text = timeLeft,
                    color = AppColorPalette.LossRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip Warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A1018))
                    .border(1.dp, AppColorPalette.LossRed.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "💀", fontSize = 16.sp)
                Column {
                    Text(
                        text = "Skipping = ",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "streak permanently broken",
                        color = AppColorPalette.LossRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "and posted to your friends' feeds.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Watching You
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Watching you 👀",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = interFamily
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    watchingFriends.forEachIndexed { index, emoji ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = (-(index * 4)).dp)
                                .clip(CircleShape)
                                .background(AppColorPalette.DeepSurface)
                                .border(2.dp, AppColorPalette.CyanCta.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                        }
                    }
                }
                Text(
                    text = "${watchingFriends.size} friends can verify",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontFamily = interFamily,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Upload Proof Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppColorPalette.LossRed, Color(0xFFFF6B6B))
                        )
                    )
                    .clickable { onCompleteProof() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷 Upload Proof Now",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Use Streak Insurance Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1A1028), Color(0xFF1C1230))
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                AppColorPalette.GoldPremium.copy(alpha = 0.15f),
                                AppColorPalette.GoldPremium.copy(alpha = 0.08f)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onUseInsurance() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛡️ Use Streak Insurance (Premium)",
                    color = AppColorPalette.GoldPremium.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
