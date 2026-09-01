package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

enum class NotificationCategory(val headerTitle: String) {
    ALARMS("⏰ ALARMS"),
    SOCIAL("🏆 SOCIAL"),
    ACHIEVEMENTS("🏅 ACHIEVEMENTS"),
    GROUPS("👥 GROUPS")
}

data class NotificationItem(
    val id: String,
    val category: NotificationCategory,
    val icon: String,
    val title: String,
    val subtext: String,
    val timeAgo: String,
    val isUnread: Boolean = false
)

@Composable
fun NotificationsScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                // Alarms
                NotificationItem("1", NotificationCategory.ALARMS, "🔥", "Your 6:30 AM alarm fires in 9 hours", "Make sure your phone stays on", "now", isUnread = true),
                NotificationItem("2", NotificationCategory.ALARMS, "⚡", "Group alarm tomorrow: Morning Crew", "5 members confirmed", "5m", isUnread = true),
                
                // Social
                NotificationItem("3", NotificationCategory.SOCIAL, "🦁", "maya.rises challenged you to a Duo", "Accept before midnight", "12m", isUnread = true),
                NotificationItem("4", NotificationCategory.SOCIAL, "🐺", "5amclub_dani hit a 90-day streak", "They're leaving everyone behind", "1h", isUnread = false),
                
                // Achievements
                NotificationItem("5", NotificationCategory.ACHIEVEMENTS, "⚡", "You earned: Speedster badge", "0:38 completion — fastest this week", "3h", isUnread = false),
                
                // Groups
                NotificationItem("6", NotificationCategory.GROUPS, "💀", "Kick vote started: nocturnaleve", "Morning Crew · 3/4 votes needed", "30m", isUnread = true),
                NotificationItem("7", NotificationCategory.GROUPS, "🌅", "Morning Crew resets in 12 days", "Current leader: 5amclub_dani", "1d", isUnread = false)
            )
        )
    }

    val categories = NotificationCategory.values()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Row (Title + Mark all read + Back)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Notifications",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = titleFamily
                )
            }

            Text(
                text = "Mark all read",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                fontFamily = interFamily,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        notifications = notifications.map { it.copy(isUnread = false) }
                    }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        var showKickVoteSheet by remember { mutableStateOf(false) }

        if (showKickVoteSheet) {
            KickVoteBottomSheet(
                onDismiss = { showKickVoteSheet = false },
                titleFamily = titleFamily,
                interFamily = interFamily
            )
        }

        // Grouped Notifications List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            categories.forEach { category ->
                val categoryItems = notifications.filter { it.category == category }
                if (categoryItems.isNotEmpty()) {
                    item(key = category.name) {
                        NotificationCategoryGroup(
                            category = category,
                            items = categoryItems,
                            interFamily = interFamily,
                            onItemClick = { clickedItem ->
                                notifications = notifications.map {
                                    if (it.id == clickedItem.id) it.copy(isUnread = false) else it
                                }
                                if (clickedItem.icon == "💀" || clickedItem.title.contains("Kick vote", ignoreCase = true)) {
                                    showKickVoteSheet = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCategoryGroup(
    category: NotificationCategory,
    items: List<NotificationItem>,
    interFamily: FontFamily,
    onItemClick: (NotificationItem) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header Label
        Text(
            text = category.headerTitle,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            fontWeight = FontWeight.W800,
            fontFamily = interFamily,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Category Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, item ->
                    NotificationRowItem(
                        item = item,
                        interFamily = interFamily,
                        onClick = { onItemClick(item) }
                    )
                    if (index < items.size - 1) {
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

@Composable
fun NotificationRowItem(
    item: NotificationItem,
    interFamily: FontFamily,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Cyan Unread Indicator Pill/Dot
        Box(
            modifier = Modifier.width(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.isUnread) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AppColorPalette.CyanCta)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Square-rounded Icon/Avatar container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColorPalette.DeepSurface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.icon,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtext
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtext,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Time Ago on far right
        Text(
            text = item.timeAgo,
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 12.sp,
            fontFamily = interFamily,
            fontWeight = FontWeight.W400
        )
    }
}
