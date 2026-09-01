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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

data class GroupMemberSetting(
    val id: String,
    val name: String,
    val avatar: String,
    val streak: Int,
    val isAdmin: Boolean = false,
    val isInactive: Boolean = false,
    val isCurrentUser: Boolean = false
)

@Composable
fun GroupSettingsScreen(
    onBack: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    groupName: String = "Morning Crew",
    groupAvatar: String = "🌅",
    createdInfo: String = "Created by 5amclub_dani · Jan 12, 2025",
    onLeaveGroupClick: () -> Unit = {},
    members: List<GroupMemberSetting> = remember {
        listOf(
            GroupMemberSetting("1", "5amclub_dani", "🐺", 89, isAdmin = true),
            GroupMemberSetting("2", "maya.rises", "🦁", 41),
            GroupMemberSetting("3", "YOU", "🥱", 23, isCurrentUser = true),
            GroupMemberSetting("4", "grind.rio", "🐻", 15),
            GroupMemberSetting("5", "nocturnaleve", "🐱", 3, isInactive = true)
        )
    }
) {
    var showKickVoteSheet by remember { mutableStateOf(false) }

    if (showKickVoteSheet) {
        KickVoteBottomSheet(
            onDismiss = { showKickVoteSheet = false },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
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
                text = "Group Settings",
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
            // ── Group Header & Avatar Banner ──────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(AppColorPalette.Surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = groupAvatar,
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = groupName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = createdInfo,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontFamily = interFamily
                    )
                }
            }

            // ── GROUP RULES Card ─────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.CyanCta.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "GROUP RULES",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W800,
                            fontFamily = interFamily,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val rules = listOf(
                            "6AM alarm every weekday",
                            "No snooze — or you're out",
                            "Punishment compliance is mandatory",
                            "2 skips in a row = kick vote"
                        )

                        rules.forEachIndexed { index, rule ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.  ",
                                    color = AppColorPalette.CyanCta,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W700,
                                    fontFamily = interFamily
                                )
                                Text(
                                    text = rule,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.sp,
                                    fontFamily = interFamily,
                                    fontWeight = FontWeight.W400
                                )
                            }
                        }
                    }
                }
            }

            // ── MEMBERS (5) List Section ─────────────────────────────────────
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MEMBERS (${members.size})",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = interFamily,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            members.forEachIndexed { index, member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar with colored border
                                    val borderColor = when {
                                        member.isAdmin -> AppColorPalette.GoldPremium
                                        member.isInactive -> AppColorPalette.LossRed
                                        member.isCurrentUser -> AppColorPalette.CyanCta
                                        else -> AppColorPalette.CyanCta
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(AppColorPalette.DeepSurface)
                                            .border(2.dp, borderColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.avatar,
                                            fontSize = 20.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Name & Streak details
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = member.name,
                                                color = if (member.isCurrentUser) AppColorPalette.CyanCta else Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.W700,
                                                fontFamily = interFamily
                                            )

                                            if (member.isAdmin) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(Color(0xFF3B2F08))
                                                        .border(1.dp, AppColorPalette.GoldPremium.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "ADMIN",
                                                        color = AppColorPalette.GoldPremium,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.W800,
                                                        fontFamily = titleFamily
                                                    )
                                                }
                                            }

                                            if (member.isInactive) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(Color(0xFF3B121C))
                                                        .border(1.dp, AppColorPalette.LossRed.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "INACTIVE",
                                                        color = AppColorPalette.LossRed,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.W800,
                                                        fontFamily = titleFamily
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "🔥 ${member.streak} streak",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 12.sp,
                                            fontFamily = interFamily
                                        )
                                    }

                                    // Action Button for INACTIVE members (Vote Kick)
                                    if (member.isInactive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(AppColorPalette.LossRed.copy(alpha = 0.15f))
                                                .border(1.dp, AppColorPalette.LossRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) { showKickVoteSheet = true }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Vote Kick",
                                                color = AppColorPalette.LossRed,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.W700,
                                                fontFamily = interFamily
                                            )
                                        }
                                    }
                                }

                                if (index < members.size - 1) {
                                    HorizontalDivider(
                                        color = Color.White.copy(alpha = 0.05f),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Leave Group Action Button ─────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColorPalette.LossRed.copy(alpha = 0.12f))
                        .border(1.dp, AppColorPalette.LossRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onLeaveGroupClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Leave Group",
                        color = AppColorPalette.LossRed,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = titleFamily
                    )
                }
            }
        }
    }
}
