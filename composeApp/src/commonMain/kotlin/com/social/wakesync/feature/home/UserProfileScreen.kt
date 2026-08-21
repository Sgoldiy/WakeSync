package com.social.wakesync.feature.home

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

// Data class for user profile details
data class ProfileDetails(
    val username: String,
    val avatar: String,
    val joinedDate: String,
    val streak: Int,
    val winLossText: String,
    val rankText: String,
    val rankBadgeText: String,
    val headToHeadFriend: Int,
    val headToHeadYou: Int,
    val badgePills: List<Pair<String, String>>,
    val themeColor: Color
)

@Composable
fun UserProfileScreen(
    username: String,
    onBack: () -> Unit,
    onChallengeClick: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    // Generate data-driven profile details based on the username
    val details = remember(username) {
        when (username) {
            "5amclub_dani" -> ProfileDetails(
                username = "5amclub_dani",
                avatar = "🐺",
                joinedDate = "Joined Mar 2025",
                streak = 89,
                winLossText = "74-12",
                rankText = "#3",
                rankBadgeText = "Top 5",
                headToHeadFriend = 12,
                headToHeadYou = 5,
                badgePills = listOf("🐺 Apex" to "Pack Leader", "🌅 Early Bird" to "5:00 AM", "🔥 On Fire" to "Streak", "⚡ Speedster" to "Fast Dismiss"),
                themeColor = Color(0xFFFFD23D)
            )
            "nocturnaleve" -> ProfileDetails(
                username = "nocturnaleve",
                avatar = "🐱",
                joinedDate = "Joined Jun 2025",
                streak = 0,
                winLossText = "14-38",
                rankText = "#42",
                rankBadgeText = "Tier 3",
                headToHeadFriend = 2,
                headToHeadYou = 9,
                badgePills = listOf("🐱 Night Owl" to "Late Sleep", "💀 Slacker" to "Slept In"),
                themeColor = Color(0xFFFF3D71)
            )
            "grind.rio" -> ProfileDetails(
                username = "grind.rio",
                avatar = "🐻",
                joinedDate = "Joined Dec 2024",
                streak = 15,
                winLossText = "33-19",
                rankText = "#18",
                rankBadgeText = "Top 20",
                headToHeadFriend = 4,
                headToHeadYou = 4,
                badgePills = listOf("🐻 Beast" to "Grind Mode", "🌅 Early Bird" to "Early Rise", "💪 Tank" to "Never Fail"),
                themeColor = Color(0xFF00E0FF)
            )
            else -> ProfileDetails(
                username = "maya.rises",
                avatar = "🦁",
                joinedDate = "Joined Jan 2025",
                streak = 41,
                winLossText = "52-8",
                rankText = "#7",
                rankBadgeText = "Top 10",
                headToHeadFriend = 7,
                headToHeadYou = 3,
                badgePills = listOf("🦁 Apex" to "Apex Predator", "🌅 Early Bird" to "Sunrise Club", "🔥 On Fire" to "Super Hot", "⚡ Speedster" to "Quick Solves"),
                themeColor = Color(0xFFFFD23D)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 10.dp) // Aligns perfectly with Home margins
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row (Circle Back Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Avatar Section with glowing ring border
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Outer glow representation
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    details.themeColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Colored border ring
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .border(2.dp, details.themeColor, CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.02f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = details.avatar, fontSize = 42.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Username
            Text(
                text = details.username,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = titleFamily
            )

            // Handle & Joined Date
            Text(
                text = "@${details.username} · ${details.joinedDate}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontFamily = interFamily
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-badges row (Top 10 / Streak info)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFD23D).copy(alpha = 0.06f))
                        .border(1.dp, Color(0xFFFFD23D).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "👑 ${details.rankBadgeText}",
                        color = Color(0xFFFFD23D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }

                // Streak Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF22C55E).copy(alpha = 0.06f))
                        .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🔥 ${details.streak} streak",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Row of 3 stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    value = details.streak.toString(),
                    valueColor = Color(0xFFFF8A3D),
                    label = "Streak",
                    modifier = Modifier.weight(1f),
                    interFamily = interFamily
                )
                StatCard(
                    value = details.winLossText,
                    valueColor = Color(0xFF22C55E),
                    label = "W-L",
                    modifier = Modifier.weight(1f),
                    interFamily = interFamily
                )
                StatCard(
                    value = details.rankText,
                    valueColor = Color(0xFFFFD23D),
                    label = "Rank",
                    modifier = Modifier.weight(1f),
                    interFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // HEAD TO HEAD VS YOU section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111420)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "HEAD TO HEAD VS YOU",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Player Wins
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = details.headToHeadFriend.toString(),
                                color = Color(0xFFFF3D71), // Pink/Red matching mockup
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                            Text(
                                text = "${details.username} won",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontFamily = interFamily
                            )
                        }

                        // Right Player Wins (You)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = details.headToHeadYou.toString(),
                                color = AppColorPalette.CyanCta, // Cyan matching mockup
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                            Text(
                                text = "you won",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontFamily = interFamily
                            )
                        }
                    }

                    // Ratio slider bar representation
                    val totalWins = (details.headToHeadFriend + details.headToHeadYou).coerceAtLeast(1)
                    val friendRatio = details.headToHeadFriend.toFloat() / totalWins
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        // Friend Share (Pink/Red)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(friendRatio.coerceAtLeast(0.05f))
                                .background(Color(0xFFFF3D71))
                        )
                        // You Share (Cyan)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight((1f - friendRatio).coerceAtLeast(0.05f))
                                .background(AppColorPalette.CyanCta)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BADGES section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "BADGES",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(details.badgePills) { (pillText, _) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pillText,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Challenge to Duo Alarm CTA Button
            Button(
                onClick = onChallengeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.CyanCta,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚡ Challenge to Duo Alarm",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    value: String,
    valueColor: Color,
    label: String,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111420)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontFamily = interFamily
            )
        }
    }
}
