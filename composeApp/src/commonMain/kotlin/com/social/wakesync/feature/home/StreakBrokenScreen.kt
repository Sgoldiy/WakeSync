package com.social.wakesync.feature.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun StreakBrokenScreen(
    previousStreak: Int = 23,
    currentStreak: Int = 0,
    punishmentText: String = "20 pushups 💪",
    punishmentDetail: String = "Photo proof required · Due 8:30 AM",
    onCompletePunishment: () -> Unit = {},
    onUseInsurance: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_broken")

    // Broken heart pulse — slow, heavy beat
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_pulse"
    )

    // Red glow breathing
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_breathe"
    )

    // Subtle top/bottom vignette animation
    val vignetteAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vignette"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
    ) {
        // Top red vignette glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColorPalette.LossRed.copy(alpha = vignetteAlpha * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom red vignette glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AppColorPalette.LossRed.copy(alpha = vignetteAlpha * 0.25f)
                        )
                    )
                )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Broken heart with red glow
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Glow behind heart
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = AppColorPalette.LossRed.copy(alpha = glowAlpha * 0.2f),
                            shape = CircleShape
                        )
                )
                Text(
                    text = "💔",
                    fontSize = 72.sp,
                    modifier = Modifier.scale(heartScale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "STREAK BROKEN" title
            Text(
                text = "STREAK\nBROKEN",
                color = AppColorPalette.LossRed,
                fontSize = 42.sp,
                fontWeight = FontWeight.W800,
                fontFamily = titleFamily,
                letterSpacing = (-1.5).sp,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Streak counter badge: "23 was your streak 🔥 0 now"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                // Old streak (strikethrough)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = previousStreak.toString(),
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = titleFamily,
                        letterSpacing = (-1.5).sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text(
                        text = "was your streak",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = interFamily
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Dimmed fire emoji
                Text(
                    text = "🔥",
                    fontSize = 28.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                // New streak = 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStreak.toString(),
                        color = AppColorPalette.LossRed,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        letterSpacing = (-1.5).sp
                    )
                    Text(
                        text = "now",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Punishment Assigned Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppColorPalette.LossRed.copy(alpha = 0.08f),
                                AppColorPalette.LossRed.copy(alpha = 0.03f)
                            )
                        )
                    )
                    .border(1.dp, AppColorPalette.LossRed.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "PUNISHMENT ASSIGNED",
                    color = AppColorPalette.LossRed.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = punishmentText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = titleFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = punishmentDetail,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Last place again?" message
            Text(
                text = "Last place again? 😐",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Redeem yourself tomorrow.",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 15.sp,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Complete Punishment button (Red)
            Button(
                onClick = onCompletePunishment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = AppColorPalette.LossRed.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.LossRed
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Complete Punishment 💪",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Use Streak Insurance (Premium) button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1028),
                                Color(0xFF1C1230)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
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
                    text = "💎 Use Streak Insurance (Premium)",
                    color = AppColorPalette.GoldPremium.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
