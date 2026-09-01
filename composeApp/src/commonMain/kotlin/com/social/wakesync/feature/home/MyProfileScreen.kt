package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

data class AchievementItem(
    val title: String,
    val icon: String,
    val isUnlocked: Boolean
)

@Composable
fun MyProfileScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    username: String = "nocturnaljake",
    avatarEmoji: String = "🥱",
    followersCount: Int = 138,
    streak: Int = 23,
    wins: Int = 18,
    losses: Int = 5,
    globalRank: String = "#47",
    bestStreak: Int = 34,
    totalHabits: Int = 127,
    onEditClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    achievements: List<AchievementItem> = remember {
        listOf(
            AchievementItem("Early Bird", "🌅", true),
            AchievementItem("On Fire", "🔥", true),
            AchievementItem("Speedster", "⚡", true),
            AchievementItem("Top 50", "🏆", true),
            AchievementItem("Survivor", "💀", false),
            AchievementItem("Apex", "👑", false),
            AchievementItem("Alpha", "🐺", false),
            AchievementItem("Diamond", "💎", false)
        )
    }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Bar (Edit button on far right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onEditClick() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Edit",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "✏️",
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Glowing Avatar Circle
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(AppColorPalette.DeepSurface)
                .border(2.5.dp, AppColorPalette.CyanCta, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarEmoji,
                fontSize = 44.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Username
        Text(
            text = username,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.W700,
            fontFamily = titleFamily
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Handle & Followers
        Text(
            text = "@$username · $followersCount followers",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontFamily = interFamily,
            fontWeight = FontWeight.W400
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Main Stats Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onStatsClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Big Streak
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = streak.toString(),
                                color = Color.White,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.W900,
                                fontFamily = titleFamily
                            )
                            Text(
                                text = "🔥",
                                fontSize = 28.sp
                            )
                        }
                        Text(
                            text = "Day streak",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W400
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    VerticalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .height(56.dp)
                            .width(1.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    // Right Stats
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wins
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "${wins}W",
                                    color = AppColorPalette.WinGreen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    fontFamily = titleFamily
                                )
                                Text(
                                    text = "Wins",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = interFamily
                                )
                            }

                            // Losses
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "${losses}L",
                                    color = AppColorPalette.LossRed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    fontFamily = titleFamily
                                )
                                Text(
                                    text = "Losses",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = interFamily
                                )
                            }

                            // Global Rank
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = globalRank,
                                    color = AppColorPalette.GoldPremium,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    fontFamily = titleFamily
                                )
                                Text(
                                    text = "Global",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = interFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subtext Best & Habits
                        Text(
                            text = "Best: $bestStreak days · $totalHabits total habits",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ACHIEVEMENTS Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "ACHIEVEMENTS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontWeight = FontWeight.W800,
                fontFamily = interFamily,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Achievements Grid (4 columns x 2 rows)
        val row1 = achievements.take(4)
        val row2 = achievements.drop(4).take(4)

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row1.forEach { item ->
                    AchievementTile(
                        item = item,
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row2.forEach { item ->
                    AchievementTile(
                        item = item,
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementTile(
    item: AchievementItem,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val bg = if (item.isUnlocked) AppColorPalette.Surface else AppColorPalette.DeepSurface.copy(alpha = 0.6f)
    val borderStroke = if (item.isUnlocked) {
        BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.35f))
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    }

    Card(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.icon,
                fontSize = if (item.isUnlocked) 24.sp else 20.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = item.title,
                color = if (item.isUnlocked) Color.White else Color.White.copy(alpha = 0.25f),
                fontSize = 11.sp,
                fontWeight = if (item.isUnlocked) FontWeight.W700 else FontWeight.W400,
                fontFamily = interFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
