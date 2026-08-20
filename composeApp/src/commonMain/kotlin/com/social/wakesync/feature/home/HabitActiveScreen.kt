package com.social.wakesync.feature.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitActiveScreen(
    habit: Habit,
    onBack: () -> Unit,
    onToggleDone: (String) -> Unit,
    onViewStats: (Habit) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isDone = habit.isDone
    val emoji = when (habit.iconType) {
        HabitIconType.RUN -> "🏃"
        HabitIconType.SHOWER -> "🚿"
        HabitIconType.NO_PHONE -> "📵"
        HabitIconType.READING -> "📚"
        HabitIconType.STRETCH -> "🧘"
    }

    // Glowing animation transitions
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        containerColor = AppColorPalette.VoidBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColorPalette.VoidBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                    ) {
                        Text(
                            text = "←",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // View Stats Button
                    IconButton(
                        onClick = { onViewStats(habit) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Habit Title
            Text(
                text = habit.title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic Completion State Content
            Crossfade(
                targetState = isDone,
                animationSpec = tween(600),
                modifier = Modifier.fillMaxWidth()
            ) { doneState ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (doneState) {
                        // STATE 1: COMPLETED (Image 1)
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .scale(glowScale)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
                                    color = AppColorPalette.WinGreen.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            AppColorPalette.WinGreen.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .clickable { onToggleDone(habit.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Green Square Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(AppColorPalette.WinGreen)
                                        .shadow(12.dp, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.Black,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.W900
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // +1 Streak Text inside glow
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "+1 STREAK",
                                        color = AppColorPalette.WinGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFamily,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text("🔥", fontSize = 16.sp)
                                }
                            }
                        }

                        // Completed Streak Counter (e.g. 23+1)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${habit.streak}+1",
                                color = AppColorPalette.WinGreen,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = titleFamily
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🔥", fontSize = 13.sp)
                                Text(
                                    text = "day streak",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    } else {
                        // STATE 2: IN PROGRESS (Image 2)
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .scale(glowScale)
                                .clip(CircleShape)
                                .border(
                                    width = 3.dp,
                                    color = AppColorPalette.CyanCta.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            AppColorPalette.CyanCta.copy(alpha = 0.12f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .clickable { onToggleDone(habit.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Tap to complete",
                                    color = AppColorPalette.CyanCta,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                            }
                        }

                        // In-progress Streak Counter (e.g. 23)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = habit.streak.toString(),
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = titleFamily
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🔥", fontSize = 13.sp)
                                Text(
                                    text = "day streak",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Add Photo Proof section (only if not completed)
            if (!isDone) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { onToggleDone(habit.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("📷", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Add photo proof",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                                Text(
                                    text = "Optional — but earn +10 credibility pts",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp,
                                    fontFamily = interFamily
                                )
                            }
                        }

                        Text(
                            text = "Add →",
                            color = AppColorPalette.CyanCta,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily
                        )
                    }
                }
            } else {
                // Placeholder to balance the layout height when completed
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}
