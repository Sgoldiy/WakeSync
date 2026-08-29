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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

data class LeaderboardUser(
    val rank: Int,
    val username: String,
    val avatar: String,
    val score: Int,
    val streak: Int,
    val isCurrentUser: Boolean = false,
    val isRedLoss: Boolean = false
)

@Composable
fun LeaderboardScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    currentUsername: String = "nocturnaljake",
    currentUserAvatar: String = "🥱",
    onFindRivalsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Global") }
    var selectedMode by remember { mutableStateOf("Solo") }

    // Mock datasets for Solo
    val globalSolo = remember(currentUsername, currentUserAvatar) {
        listOf(
            LeaderboardUser(1, "zero_snooze", "🐊", 9410, 66),
            LeaderboardUser(2, "5amclub_dani", "🐺", 8820, 89),
            LeaderboardUser(3, "earlybird_rin", "🐦", 7930, 44),
            LeaderboardUser(4, "maya.rises", "🦁", 7200, 41),
            LeaderboardUser(5, "grind.rio", "🐻", 6850, 15),
            LeaderboardUser(6, "morning_kai", "🐯", 6200, 18),
            LeaderboardUser(7, "riseup.mia", "🦊", 5900, 12),
            LeaderboardUser(47, "$currentUsername <- you", currentUserAvatar, 4200, 23, isCurrentUser = true)
        )
    }
    val friendsSolo = remember(currentUserAvatar) {
        listOf(
            LeaderboardUser(1, "5amclub_dani", "🐺", 8820, 89),
            LeaderboardUser(2, "maya.rises", "🦁", 7200, 41),
            LeaderboardUser(3, "grind.rio", "🐻", 6200, 15),
            LeaderboardUser(4, "YOU", currentUserAvatar, 4200, 23, isCurrentUser = true),
            LeaderboardUser(5, "nocturnaleve", "🐱", 1800, 3, isRedLoss = true)
        )
    }

    // Mock datasets for Duo
    val globalDuo = remember(currentUsername, currentUserAvatar) {
        listOf(
            LeaderboardUser(1, "earlybird_rin", "🐦", 8400, 40),
            LeaderboardUser(2, "maya.rises", "🦁", 7900, 38),
            LeaderboardUser(3, "zero_snooze", "🐊", 7100, 35),
            LeaderboardUser(4, "5amclub_dani", "🐺", 6500, 30),
            LeaderboardUser(5, "grind.rio", "🐻", 5800, 10),
            LeaderboardUser(6, "morning_kai", "🐯", 5100, 14),
            LeaderboardUser(7, "riseup.mia", "🦊", 4800, 9),
            LeaderboardUser(47, "$currentUsername <- you", currentUserAvatar, 3800, 18, isCurrentUser = true)
        )
    }
    val friendsDuo = remember(currentUserAvatar) {
        listOf(
            LeaderboardUser(1, "maya.rises", "🦁", 7900, 38),
            LeaderboardUser(2, "5amclub_dani", "🐺", 6500, 30),
            LeaderboardUser(3, "YOU", currentUserAvatar, 3800, 18, isCurrentUser = true),
            LeaderboardUser(4, "grind.rio", "🐻", 2100, 8),
            LeaderboardUser(5, "nocturnaleve", "🐱", 900, 1, isRedLoss = true)
        )
    }

    // Mock datasets for Group
    val globalGroup = remember(currentUsername, currentUserAvatar) {
        listOf(
            LeaderboardUser(1, "grind.rio", "🐻", 9100, 50),
            LeaderboardUser(2, "morning_kai", "🐯", 8600, 45),
            LeaderboardUser(3, "riseup.mia", "🦊", 8000, 42),
            LeaderboardUser(4, "zero_snooze", "🐊", 7500, 39),
            LeaderboardUser(5, "5amclub_dani", "🐺", 7100, 33),
            LeaderboardUser(6, "earlybird_rin", "🐦", 6600, 28),
            LeaderboardUser(7, "maya.rises", "🦁", 6000, 22),
            LeaderboardUser(47, "$currentUsername <- you", currentUserAvatar, 4900, 25, isCurrentUser = true)
        )
    }
    val friendsGroup = remember(currentUserAvatar) {
        listOf(
            LeaderboardUser(1, "grind.rio", "🐻", 6850, 15),
            LeaderboardUser(2, "morning_kai", "🐯", 6200, 18),
            LeaderboardUser(3, "YOU", currentUserAvatar, 4900, 25, isCurrentUser = true),
            LeaderboardUser(4, "maya.rises", "🦁", 3900, 10),
            LeaderboardUser(5, "nocturnaleve", "🐱", 1200, 2, isRedLoss = true)
        )
    }

    // Resolve list based on tab + mode
    val activeList = remember(selectedTab, selectedMode, globalSolo, friendsSolo, globalDuo, friendsDuo, globalGroup, friendsGroup) {
        when (selectedMode) {
            "Solo" -> if (selectedTab == "Global") globalSolo else friendsSolo
            "Duo" -> if (selectedTab == "Global") globalDuo else friendsDuo
            else -> if (selectedTab == "Global") globalGroup else friendsGroup
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedTab, // Title matches current tab selection ("Global" / "Friends")
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily
            )

            // Find Rivals Icon Button
            androidx.compose.material3.IconButton(
                onClick = onFindRivalsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Text("🔍", fontSize = 20.sp)
            }

            // Switcher Tabs
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(99.dp))
                    .padding(4.dp)
            ) {
                listOf("Global", "Friends").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(99.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFamily
                        )
                    }
                }
            }
        }

        // Horizontal Mode Selector (Solo, Duo, Group)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf("Solo", "Duo", "Group").forEach { mode ->
                val isSelected = selectedMode == mode
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMode = mode },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = mode,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = interFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) AppColorPalette.CyanCta else Color.Transparent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == "Global") {
            // Global Layout: Top 3 Podium
            val top1 = activeList.firstOrNull { it.rank == 1 }
            val top2 = activeList.firstOrNull { it.rank == 2 }
            val top3 = activeList.firstOrNull { it.rank == 3 }
            val remainingList = activeList.filter { it.rank > 3 || it.isCurrentUser }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Left)
                if (top2 != null) {
                    PodiumColumn(
                        user = top2,
                        height = 110.dp,
                        color = Color(0xFFC0C0C0), // Silver
                        podiumNumber = "2",
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rank 1 (Center)
                if (top1 != null) {
                    PodiumColumn(
                        user = top1,
                        height = 140.dp,
                        color = Color(0xFFFFD23D), // Gold
                        podiumNumber = "1",
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        modifier = Modifier.weight(1.1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1.1f))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Rank 3 (Right)
                if (top3 != null) {
                    PodiumColumn(
                        user = top3,
                        height = 95.dp,
                        color = Color(0xFFCD7F32), // Bronze
                        podiumNumber = "3",
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(remainingList) { rival ->
                    LeaderboardRow(
                        rival = rival,
                        interFamily = interFamily
                    )
                }
            }
        } else {
            // Friends Layout: Weekly Standings Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131829)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📅", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Weekly Standings",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFamily
                        )
                        Text(
                            text = "Resets Sunday · ${activeList.size} friends competing",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontFamily = interFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(activeList) { rival ->
                    LeaderboardRow(
                        rival = rival,
                        interFamily = interFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    user: LeaderboardUser,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    podiumNumber: String,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F1524))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = user.avatar, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = user.username,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = interFamily,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131829)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = podiumNumber,
                    color = color,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily
                )
                Text(
                    text = String.format("%,d", user.score),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontFamily = interFamily
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rival: LeaderboardUser,
    interFamily: FontFamily
) {
    val rankColor = when (rival.rank) {
        1 -> Color(0xFFFFD23D) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze/Orange
        else -> if (rival.isCurrentUser) AppColorPalette.CyanCta else if (rival.isRedLoss) Color(0xFFFF3D71) else Color.White.copy(alpha = 0.15f)
    }

    val containerBg = when {
        rival.isCurrentUser -> Color(0xFF0F1E2A)
        rival.isRedLoss -> Color(0xFF220F16)
        else -> Color(0xFF131829)
    }

    val borderStroke = when {
        rival.isCurrentUser -> BorderStroke(1.5.dp, AppColorPalette.CyanCta)
        rival.isRedLoss -> BorderStroke(1.5.dp, Color(0xFFFF3D71))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = borderStroke
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
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rival.rank.toString(),
                        color = rankColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.5.dp, rankColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = rival.avatar, fontSize = 18.sp)
                }

                // Username & Streak stack
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = rival.username,
                        color = if (rival.isCurrentUser) AppColorPalette.CyanCta else if (rival.isRedLoss) Color(0xFFFF3D71) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "🔥", fontSize = 10.sp)
                        Text(
                            text = rival.streak.toString(),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = interFamily
                        )
                    }
                }
            }

            // Score with rank highlight color
            Text(
                text = String.format("%,d", rival.score),
                color = rankColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily
            )
        }
    }
}
