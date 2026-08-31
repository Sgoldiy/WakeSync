package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
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

data class GroupMember(
    val rank: Int,
    val username: String,
    val avatar: String,
    val points: Int,
    val wins: Int,
    val isCurrentUser: Boolean = false,
    val isRedHighlight: Boolean = false
)

@Composable
fun GroupLeaderboardScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    groupTitle: String = "Morning Crew 🌅",
    memberCount: Int = 5,
    resetDays: Int = 12,
    onBack: (() -> Unit)? = null,
    members: List<GroupMember> = remember {
        listOf(
            GroupMember(1, "5amclub_dani", "🐺", 42, 14),
            GroupMember(2, "maya.rises", "🦁", 38, 12),
            GroupMember(3, "YOU", "🥱", 31, 9, isCurrentUser = true),
            GroupMember(4, "grind.rio", "🐻", 28, 8),
            GroupMember(5, "nocturnaleve", "🐱", 11, 3, isRedHighlight = true)
        )
    }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top bar with back button if provided
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }

        // Header Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = groupTitle,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Header Info Badges (5 members & Resets in 12 days)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Members badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(AppColorPalette.CyanCta.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        AppColorPalette.CyanCta.copy(alpha = 0.3f),
                        RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$memberCount members",
                    color = AppColorPalette.CyanCta,
                    fontSize = 12.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W600
                )
            }

            // Resets in X days badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(AppColorPalette.StreakFireStart.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        AppColorPalette.StreakFireStart.copy(alpha = 0.3f),
                        RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Resets in $resetDays days ⌛",
                    color = AppColorPalette.StreakFireStart,
                    fontSize = 12.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W600
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top 3 Podium Card Summary
        val top1 = members.firstOrNull { it.rank == 1 }
        val top2 = members.firstOrNull { it.rank == 2 }
        val top3 = members.firstOrNull { it.rank == 3 }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 (Left)
                if (top2 != null) {
                    PodiumItem(
                        member = top2,
                        rankColor = Color.White.copy(alpha = 0.7f),
                        avatarSize = 48.dp,
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                }

                // Rank 1 (Center - Elevated)
                if (top1 != null) {
                    PodiumItem(
                        member = top1,
                        rankColor = AppColorPalette.GoldPremium,
                        avatarSize = 54.dp,
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                }

                // Rank 3 (Right)
                if (top3 != null) {
                    PodiumItem(
                        member = top3,
                        rankColor = AppColorPalette.StreakFireStart,
                        avatarSize = 48.dp,
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Members Leaderboard List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(members) { member ->
                GroupMemberRowItem(
                    member = member,
                    titleFamily = titleFamily,
                    interFamily = interFamily
                )
            }
        }
    }
}

@Composable
fun PodiumItem(
    member: GroupMember,
    rankColor: Color,
    avatarSize: androidx.compose.ui.unit.Dp,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Avatar Box
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(AppColorPalette.DeepSurface)
                .border(2.dp, rankColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.avatar,
                fontSize = if (avatarSize > 50.dp) 24.sp else 20.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Rank Number
        Text(
            text = member.rank.toString(),
            color = rankColor,
            fontSize = if (member.rank == 1) 18.sp else 16.sp,
            fontWeight = FontWeight.W900,
            fontFamily = interFamily
        )

        // Username
        Text(
            text = member.username,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontFamily = interFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Score
        Text(
            text = "${member.points}pts",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
            fontFamily = titleFamily
        )
    }
}

@Composable
fun GroupMemberRowItem(
    member: GroupMember,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val cardBg = when {
        member.isCurrentUser -> AppColorPalette.CyanCta.copy(alpha = 0.05f)
        member.isRedHighlight -> AppColorPalette.LossRed.copy(alpha = 0.05f)
        else -> AppColorPalette.Surface
    }

    val borderStroke = when {
        member.isCurrentUser -> BorderStroke(1.5.dp, AppColorPalette.CyanCta)
        member.isRedHighlight -> BorderStroke(1.5.dp, AppColorPalette.LossRed.copy(alpha = 0.6f))
        else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    }

    val rankTextColor = when (member.rank) {
        1 -> AppColorPalette.GoldPremium
        2 -> Color.White.copy(alpha = 0.7f)
        3 -> AppColorPalette.StreakFireStart
        5 -> AppColorPalette.LossRed
        else -> Color.White.copy(alpha = 0.4f)
    }

    val avatarBorderColor = when {
        member.isCurrentUser -> AppColorPalette.CyanCta
        member.isRedHighlight -> AppColorPalette.LossRed
        member.rank == 1 -> AppColorPalette.GoldPremium
        else -> Color.White.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = member.rank.toString(),
                color = rankTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.W900,
                fontFamily = interFamily,
                modifier = Modifier.width(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.DeepSurface)
                    .border(2.dp, avatarBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.avatar,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // User Info (Name + Wins)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = member.username,
                    color = if (member.isCurrentUser) AppColorPalette.CyanCta else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${member.wins} wins",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                }
            }

            // Right Points
            val pointsColor = when {
                member.isCurrentUser -> AppColorPalette.CyanCta
                member.isRedHighlight -> AppColorPalette.LossRed
                else -> Color.White
            }

            Text(
                text = "${member.points}pts",
                color = pointsColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )
        }
    }
}
