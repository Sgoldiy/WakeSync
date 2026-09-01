package com.social.wakesync.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun StatsDeepDiveScreen(
    onBack: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    overallWinRate: Int = 78,
    soloWinRate: Int = 91,
    duoWinRate: Int = 72,
    groupWinRate: Int = 68,
    currentStreak: Int = 23,
    longestStreak: Int = 34,
    averageStreak: Int = 11,
    consistencyImprovement: String = "34% more consistent"
) {
    // 14-day consistency bar chart heights
    val barHeights = remember {
        listOf(45, 60, 35, 70, 40, 80, 50, 65, 55, 85, 90, 80, 90, 85)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "My Stats",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // ── Card 1: WAKE TIME CONSISTENCY ───────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "WAKE TIME CONSISTENCY",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = interFamily,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 14-day vertical bar chart
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            barHeights.forEachIndexed { index, heightPercent ->
                                val isRecent = index >= 9
                                val barColor = if (isRecent) {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            AppColorPalette.CyanCta,
                                            AppColorPalette.CyanCta.copy(alpha = 0.6f)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.15f),
                                            Color.White.copy(alpha = 0.08f)
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(13.dp)
                                        .fillMaxHeight(heightPercent / 100f)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(barColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Footer text row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2 weeks ago",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 12.sp,
                                fontFamily = interFamily
                            )

                            Text(
                                text = "↑ $consistencyImprovement",
                                color = AppColorPalette.WinGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W700,
                                fontFamily = interFamily
                            )

                            Text(
                                text = "Today",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 12.sp,
                                fontFamily = interFamily
                            )
                        }
                    }
                }
            }

            // ── Card 2: WIN RATE ─────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "WIN RATE",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = interFamily,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Donut Chart Progress Ring
                            Box(
                                modifier = Modifier.size(92.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 10.dp.toPx()
                                    // Track circle
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.08f),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    // Green Progress Arc
                                    drawArc(
                                        color = AppColorPalette.WinGreen,
                                        startAngle = -90f,
                                        sweepAngle = 360f * (overallWinRate / 100f),
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                Text(
                                    text = "$overallWinRate%",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.W900,
                                    fontFamily = titleFamily
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Right Mode Breakdown Progress Rows
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Solo alarms
                                ModeProgressRow(
                                    label = "Solo alarms",
                                    percent = soloWinRate,
                                    color = AppColorPalette.WinGreen,
                                    interFamily = interFamily
                                )

                                // Duo alarms
                                ModeProgressRow(
                                    label = "Duo alarms",
                                    percent = duoWinRate,
                                    color = AppColorPalette.CyanCta,
                                    interFamily = interFamily
                                )

                                // Group alarms
                                ModeProgressRow(
                                    label = "Group alarms",
                                    percent = groupWinRate,
                                    color = AppColorPalette.GoldPremium,
                                    interFamily = interFamily
                                )
                            }
                        }
                    }
                }
            }

            // ── Card 3: STREAK HISTORY ───────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "STREAK HISTORY",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = interFamily,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Current streak
                            StreakHistoryRow(
                                label = "Current streak",
                                valueText = "$currentStreak days",
                                valueColor = AppColorPalette.StreakFireStart,
                                fillPercent = (currentStreak / longestStreak.toFloat()).coerceIn(0f, 1f),
                                barBrush = Brush.horizontalGradient(
                                    listOf(
                                        AppColorPalette.StreakFireStart,
                                        Color(0xFFFF3D71)
                                    )
                                ),
                                interFamily = interFamily
                            )

                            // Longest streak
                            StreakHistoryRow(
                                label = "Longest streak",
                                valueText = "$longestStreak days",
                                valueColor = AppColorPalette.GoldPremium,
                                fillPercent = 1f,
                                barBrush = Brush.horizontalGradient(
                                    listOf(
                                        AppColorPalette.GoldPremium,
                                        AppColorPalette.GoldPremium.copy(alpha = 0.8f)
                                    )
                                ),
                                interFamily = interFamily
                            )

                            // Average streak
                            StreakHistoryRow(
                                label = "Average streak",
                                valueText = "$averageStreak days",
                                valueColor = Color.White.copy(alpha = 0.4f),
                                fillPercent = (averageStreak / longestStreak.toFloat()).coerceIn(0f, 1f),
                                barBrush = Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.15f)
                                    )
                                ),
                                interFamily = interFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeProgressRow(
    label: String,
    percent: Int,
    color: Color,
    interFamily: FontFamily
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W500
            )
            Text(
                text = "$percent%",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent / 100f)
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun StreakHistoryRow(
    label: String,
    valueText: String,
    valueColor: Color,
    fillPercent: Float,
    barBrush: Brush,
    interFamily: FontFamily
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W500
            )
            Text(
                text = valueText,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillPercent.coerceIn(0.05f, 1f))
                    .clip(RoundedCornerShape(99.dp))
                    .background(barBrush)
            )
        }
    }
}
