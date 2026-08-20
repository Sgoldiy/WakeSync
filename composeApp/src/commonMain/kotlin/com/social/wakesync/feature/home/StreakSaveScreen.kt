package com.social.wakesync.feature.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlin.math.sin

// Data class representing a single confetti particle
private data class ConfettiParticle(
    val xFraction: Float,    // Horizontal position as fraction of width (0..1)
    val speed: Float,        // Fall speed multiplier
    val size: Float,         // Size of the paper
    val rotation: Float,     // Base rotation offset
    val color: Color,        // Color of the confetti
    val swayAmount: Float,   // How much it sways left/right
    val swaySpeed: Float,    // Speed of sway oscillation
)

@Composable
fun StreakSaveScreen(
    streakDays: Int = 24,
    finishPosition: Int = 1,
    totalParticipants: Int = 5,
    sleepingFriends: List<String> = listOf("🦁", "🐻", "🐱"),
    friendsLostCount: Int = 3,
    onShareClick: () -> Unit = {},
    onBackToHome: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_save")

    // Confetti animation progress (loops 0..1 continuously)
    val confettiProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti"
    )

    // Trophy pulse animation (gentle scale)
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_pulse"
    )

    // Trophy glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Pre-generate confetti particles
    val confettiParticles = remember {
        val colors = listOf(
            Color(0xFF00FF94),  // Green (CyanCta / WinGreen)
            Color(0xFF00E0FF),  // Cyan
            Color(0xFFFFD23D),  // Gold
            Color(0xFFFF8A3D),  // Orange
            Color(0xFFFF3D71),  // Red/Pink
            Color(0xFF7B61FF),  // Purple
            Color(0xFF4ECDC4),  // Teal
        )
        List(35) { i ->
            ConfettiParticle(
                xFraction = (i * 0.0317f + (i * 7 % 13) * 0.07f) % 1f,
                speed = 0.6f + (i % 5) * 0.12f,
                size = 4f + (i % 4) * 2f,
                rotation = (i * 47f) % 360f,
                color = colors[i % colors.size],
                swayAmount = 0.02f + (i % 3) * 0.015f,
                swaySpeed = 1.5f + (i % 4) * 0.5f,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
    ) {
        // Confetti Canvas (falls from top)
        Canvas(modifier = Modifier.fillMaxSize()) {
            confettiParticles.forEach { particle ->
                val fallProgress = (confettiProgress * particle.speed + particle.xFraction) % 1f
                val y = -particle.size + (size.height + particle.size * 2) * fallProgress
                val sway = sin(confettiProgress * particle.swaySpeed * 2 * kotlin.math.PI.toFloat()) * particle.swayAmount * size.width
                val x = particle.xFraction * size.width + sway

                // Fade in at top, fade out at bottom
                val alpha = when {
                    fallProgress < 0.05f -> fallProgress / 0.05f
                    fallProgress > 0.85f -> (1f - fallProgress) / 0.15f
                    else -> 1f
                }.coerceIn(0f, 0.85f)

                val rotation = particle.rotation + confettiProgress * 360f * particle.speed

                rotate(degrees = rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Trophy with glow + pulse animation (slightly left)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                // Glow behind trophy
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            color = AppColorPalette.WinGreen.copy(alpha = glowAlpha * 0.15f),
                            shape = CircleShape
                        )
                )
                Text(
                    text = "🏆",
                    fontSize = 72.sp,
                    modifier = Modifier.scale(trophyScale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "STREAK SAVED" title
            Text(
                text = "STREAK SAVED",
                color = AppColorPalette.WinGreen,
                fontSize = 42.sp,
                fontWeight = FontWeight.W800,
                fontFamily = titleFamily,
                letterSpacing = (-1.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Streak day count badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColorPalette.WinGreen.copy(alpha = 0.1f))
                    .border(1.dp, AppColorPalette.WinGreen.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = streakDays.toString(),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = titleFamily,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "🔥",
                    fontSize = 36.sp
                )
                Text(
                    text = "days",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Finish position text
            Text(
                text = "You finished ${finishPosition}st out of $totalParticipants.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = "⚡ You vs. yesterday: Won.",
                color = AppColorPalette.GoldPremium,
                fontSize = 15.sp,
                fontFamily = interFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Still sleeping card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "STILL SLEEPING 💀",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = interFamily,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Overlapping friend avatars
                    sleepingFriends.forEachIndexed { index, emoji ->
                        Box(
                            modifier = Modifier
                                .offset(x = (-(index * 8)).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppColorPalette.Surface)
                                .border(2.dp, AppColorPalette.LossRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                        }
                    }

                    Text(
                        text = "$friendsLostCount friends lost",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = interFamily,
                        modifier = Modifier.padding(start = (12 - (sleepingFriends.size - 1) * 8).coerceAtLeast(4).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Share the W button
            Button(
                onClick = onShareClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = AppColorPalette.WinGreen.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.WinGreen
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "🔥 Share the W",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Back to Home button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBackToHome() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to Home",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
