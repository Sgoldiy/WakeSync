package com.social.wakesync.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.components.EmptyState
import com.social.wakesync.ui.theme.AppColorPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onStoryClick: (StoryItem) -> Unit,
    onUserClick: (String) -> Unit,
    onCreateStoryClick: () -> Unit = {},
    feedPosts: List<FeedPost> = emptyList(),
    userStories: List<StoryItem> = emptyList(),
    modifier: Modifier = Modifier
) {
    val activeUserStory = remember(userStories) {
        userStories.firstOrNull { it.isUser || it.hasActiveStory }
    }

    val stories = remember(activeUserStory, userStories) {
        val list = mutableListOf<StoryItem>()
        if (activeUserStory != null) {
            list.add(
                activeUserStory.copy(
                    id = activeUserStory.id.ifEmpty { "1" },
                    username = "Your story",
                    avatar = activeUserStory.avatar.ifEmpty { "👤" },
                    isUser = true,
                    hasActiveStory = true
                )
            )
        } else {
            list.add(
                StoryItem("1", "Your story", "+", Color.White.copy(alpha = 0.15f), isUser = true, hasActiveStory = false)
            )
        }
        
        // Add other stories if available, otherwise fall back to mock stories
        val otherStories = userStories.filter { !it.isUser && !it.hasActiveStory }
        if (otherStories.isNotEmpty()) {
            list.addAll(otherStories)
        } else {
            list.addAll(
                listOf(
                    StoryItem("2", "maya.rises", "🦁", Color(0xFF00FF94), caption = "5 AM Grind Complete! 💪", badgeText = "🏆 New Record", badgeColorHex = "#00FF94", gradientStartHex = "#002B1D", gradientEndHex = "#005C3E"),
                    StoryItem("3", "5amclub_dani", "🐺", Color(0xFFFFD23D), caption = "14-Day Streak Unlocked 🔥", badgeText = "⚡ 14-Day Streak", badgeColorHex = "#FFD23D", gradientStartHex = "#2D0B00", gradientEndHex = "#6A1B00"),
                    StoryItem("4", "grind.rio", "🐻", Color(0xFF00FF94), caption = "Orb Focus Mastered 🌟", badgeText = "🌅 5:00 AM Wake Up", badgeColorHex = "#00E5FF", gradientStartHex = "#050811", gradientEndHex = "#1A102F"),
                    StoryItem("5", "nocturnaleve", "🐱", Color(0xFFFF3D71), caption = "Failed alarm punishment incoming 😅", badgeText = "📖 Fail Story", badgeColorHex = "#FF3D71", gradientStartHex = "#210B3B", gradientEndHex = "#4A154B")
                )
            )
        }
        list
    }

    // Map real FeedPost data to UI FeedItems, fall back to mock if empty
    val mappedFeedItems = remember(feedPosts) {
        if (feedPosts.isNotEmpty()) {
            feedPosts.map { post ->
                val badgeColor = try {
                    val hex = post.badgeColorHex.removePrefix("#")
                    val r = hex.substring(0, 2).toInt(16) / 255f
                    val g = hex.substring(2, 4).toInt(16) / 255f
                    val b = hex.substring(4, 6).toInt(16) / 255f
                    Color(r, g, b)
                } catch (_: Exception) { Color(0xFF22C55E) }
                val timeAgo = formatTimeAgo(post.createdAt)
                FeedItem(
                    id = post.id,
                    username = post.username,
                    avatar = post.avatar,
                    timeAgo = timeAgo,
                    streak = post.streak,
                    badgeText = post.badgeText,
                    badgeColor = badgeColor,
                    content = post.content,
                    reactions = post.reactions.map { it.key to it.value },
                    avatarBorderColor = badgeColor,
                    isShameReceipt = post.isShameReceipt || post.badgeText == "shame" || post.badgeText == "loss",
                    missedTime = post.missedTime,
                    challengeName = post.challengeName,
                    punishmentText = post.punishmentText,
                    shameCaption = post.shameCaption.ifEmpty { post.content }
                )
            }
        } else {
            // Fallback mock data when no real posts exist yet
            listOf(
                FeedItem(
                    id = "1",
                    username = "maya.rises",
                    avatar = "🦁",
                    timeAgo = "2m ago",
                    streak = 41,
                    badgeText = "win",
                    badgeColor = Color(0xFF22C55E),
                    content = "Finished 1st 🏆 Group alarm crushed.",
                    reactions = listOf("🔥" to 14, "⚡" to 8),
                    avatarBorderColor = Color(0xFF22C55E)
                ),
                FeedItem(
                    id = "2",
                    username = "5amclub_dani",
                    avatar = "🐺",
                    timeAgo = "18m ago",
                    streak = 89,
                    badgeText = "streak",
                    badgeColor = Color(0xFFFFD23D),
                    content = "89 day streak. Still undefeated. 🐺",
                    reactions = listOf("👑" to 22, "🔥" to 17),
                    avatarBorderColor = Color(0xFFFFD23D)
                ),
                FeedItem(
                    id = "3",
                    username = "nocturnaleve",
                    avatar = "🐱",
                    timeAgo = "1h ago",
                    streak = 0,
                    badgeText = "shame",
                    badgeColor = Color(0xFFFF3D71),
                    content = "Slept through 07:30 AM alarm 😴",
                    reactions = listOf("💀" to 9, "😂" to 5, "🤡" to 3, "🚨" to 4),
                    avatarBorderColor = Color(0xFFFF3D71),
                    isShameReceipt = true,
                    missedTime = "07:30 AM",
                    challengeName = "Speed Math",
                    punishmentText = "20 pushups 💪",
                    shameCaption = "@nocturnaleve slept through 07:30 AM alarm 😴 (Caught in 4K 💀)"
                ),
                FeedItem(
                    id = "4",
                    username = "grind.rio",
                    avatar = "🐻",
                    timeAgo = "3h ago",
                    streak = 15,
                    badgeText = "proof",
                    badgeColor = Color(0xFF00E0FF),
                    content = "Proof submitted. Pushups done. Never again. 💪",
                    reactions = listOf("👍" to 11, "🤔" to 2),
                    avatarBorderColor = Color(0xFF00E0FF)
                )
            )
        }
    }

    var feedItems by remember(mappedFeedItems) { mutableStateOf(mappedFeedItems) }

    var replyingToItem by remember { mutableStateOf<FeedItem?>(null) }
    var replyText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var notificationsActive by remember { mutableStateOf(false) }
    var reactedEmojis by remember { mutableStateOf(setOf<String>()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Row (Matches HomeHeader height and alignment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Activity",
                    color = Color.White,
                    fontSize = 24.sp, // Match same top header prominence size in home screen (20-24sp)
                    fontWeight = FontWeight.Bold,
                    fontFamily = titleFamily
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                            .clickable { searchActive = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = AppColorPalette.CyanCta,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Notification Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f))
                            .clickable { notificationsActive = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFFFFD23D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Horizontal Stories
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(stories) { story ->
                            StoryCircle(
                                story = story,
                                interFamily = interFamily,
                                onClick = {
                                    if (story.isUser) {
                                        if (story.hasActiveStory) {
                                            onStoryClick(story)
                                        } else {
                                            onCreateStoryClick()
                                        }
                                    } else {
                                        onStoryClick(story)
                                    }
                                }
                            )
                        }
                    }
                }

                // Activity Feed Cards (Matches exact space between two cards and edges)
                if (feedItems.isEmpty()) {
                    item {
                        EmptyState(
                            emoji = "📡",
                            title = "No Feed Posts Yet",
                            subtitle = "Be the first to share your morning wake-up win or streak milestone with the community!",
                            titleFamily = titleFamily,
                            interFamily = interFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                        )
                    }
                } else {
                    items(feedItems, key = { it.id }) { item ->
                        if (item.isShameReceipt || item.badgeText == "shame" || item.badgeText == "loss") {
                            ShameReceiptCard(
                                item = item,
                                titleFamily = titleFamily,
                                interFamily = interFamily,
                                onReactionClick = { emoji ->
                                    val reactionKey = "${item.id}_$emoji"
                                    if (reactionKey !in reactedEmojis) {
                                        reactedEmojis = reactedEmojis + reactionKey
                                        feedItems = feedItems.map { fit ->
                                            if (fit.id == item.id) {
                                                val existingReactions = fit.reactions.toMap()
                                                val updatedMap = existingReactions.toMutableMap()
                                                updatedMap[emoji] = (updatedMap[emoji] ?: 0) + 1
                                                fit.copy(reactions = updatedMap.toList())
                                            } else fit
                                        }
                                    }
                                },
                                onUserClick = onUserClick
                            )
                        } else {
                            ActivityCard(
                                item = item,
                                titleFamily = titleFamily,
                                interFamily = interFamily,
                                onReplyClick = { replyingToItem = item },
                                onReactionClick = { emoji ->
                                    val reactionKey = "${item.id}_$emoji"
                                    if (reactionKey !in reactedEmojis) {
                                        reactedEmojis = reactedEmojis + reactionKey
                                        feedItems = feedItems.map { fit ->
                                            if (fit.id == item.id) {
                                                fit.copy(
                                                    reactions = fit.reactions.map { (e, c) ->
                                                        if (e == emoji) e to (c + 1) else e to c
                                                    }
                                                )
                                            } else fit
                                        }
                                    }
                                },
                                onUserClick = onUserClick
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }



        // Search Overlay Mock
        if (searchActive) {
            SearchOverlay(
                onDismiss = { searchActive = false },
                interFamily = interFamily
            )
        }

        // Notifications Full Screen
        if (notificationsActive) {
            NotificationsScreen(
                titleFamily = titleFamily,
                interFamily = interFamily,
                onBack = { notificationsActive = false }
            )
        }

        // Reply Bottom Sheet
        replyingToItem?.let { item ->
            ModalBottomSheet(
                onDismissRequest = {
                    replyingToItem = null
                    replyText = ""
                },
                containerColor = AppColorPalette.Surface,
                contentColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Reply to @${item.username}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = titleFamily
                    )
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Write something supportive or funny...", color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = AppColorPalette.CyanCta,
                            focusedBorderColor = AppColorPalette.CyanCta,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Button(
                        onClick = {
                            replyingToItem = null
                            replyText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorPalette.CyanCta),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCircle(
    story: StoryItem,
    interFamily: FontFamily,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(62.dp) // Updated size to match screenshot proportions
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (story.isUser && !story.hasActiveStory) {
                    drawCircle(
                        color = story.borderColor,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 8f), 0f
                            )
                        )
                    )
                } else if (story.hasActiveStory) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF833AB4),
                                Color(0xFFFD1D1D),
                                Color(0xFFFFD23D),
                                Color(0xFF833AB4)
                            )
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = story.borderColor,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f)),
                contentAlignment = Alignment.Center
            ) {
                if (story.isUser) {
                    Text(
                        text = story.avatar,
                        color = AppColorPalette.CyanCta,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                } else {
                    Text(
                        text = story.avatar,
                        fontSize = 24.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = story.username,
            color = Color.White.copy(alpha = if (story.isUser) 0.4f else 0.7f),
            fontSize = 10.sp,
            fontFamily = interFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(62.dp)
        )
    }
}

@Composable
fun ActivityCard(
    item: FeedItem,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onReplyClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp), // Set same edge margin as home screen cards
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111420)), // Same dark tint as mockup cards
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp) // Perfect interior spacing matching screenshot
        ) {
            // Profile & Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.2.dp, item.avatarBorderColor, CircleShape)
                            .padding(1.5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable { onUserClick(item.username) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.avatar, fontSize = 18.sp)
                    }
                    
                    Column {
                        Text(
                            text = item.username,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFamily,
                            modifier = Modifier.clickable { onUserClick(item.username) }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.timeAgo,
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 11.sp,
                                fontFamily = interFamily
                            )
                            if (item.streak > 0) {
                                Text(
                                    text = " · 🔥 ${item.streak}",
                                    color = Color(0xFFFF8A3D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    }
                }

                // Status Badge pill matching screenshot colors
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(item.badgeColor.copy(alpha = 0.08f))
                        .border(1.dp, item.badgeColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.badgeText,
                        color = item.badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feed Content Text
            Text(
                text = item.content,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontFamily = interFamily
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Reactions & Reply alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.reactions.forEach { (emoji, count) ->
                        ReactionPill(
                            emoji = emoji,
                            count = count,
                            onClick = { onReactionClick(emoji) }
                        )
                    }
                }

                Text(
                    text = "Reply",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onReplyClick
                        )
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ShameReceiptCard(
    item: FeedItem,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onReactionClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    val crimson = Color(0xFFFF3D71)
    val missedTimeStr = if (item.missedTime.isEmpty()) "07:30 AM" else item.missedTime
    val challengeStr = if (item.challengeName.isEmpty()) "Speed Math" else item.challengeName
    val streakLostStr = if (item.streak > 0) "${item.streak} -> 0" else "Broken 📉"
    val punishmentStr = if (item.punishmentText.isEmpty()) "20 pushups" else item.punishmentText
    val captionStr = if (item.shameCaption.isNotEmpty()) item.shameCaption else item.content

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF190D18)),
        border = BorderStroke(1.5.dp, crimson.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Receipt Header Stamp & Barcode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(crimson.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🚨", fontSize = 16.sp)
                    Text(
                        text = "MISSED ALARM RECEIPT",
                        color = crimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = titleFamily,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "|||| | ||||| ||",
                    color = crimson.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Offender Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .border(1.5.dp, crimson, CircleShape)
                            .padding(1.5.dp)
                            .clip(CircleShape)
                            .background(crimson.copy(alpha = 0.15f))
                            .clickable { onUserClick(item.username) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.avatar, fontSize = 20.sp)
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.username,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily,
                                modifier = Modifier.clickable { onUserClick(item.username) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFENDER 💀",
                                color = crimson,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFamily,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(crimson.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = item.timeAgo,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = interFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Perforated Details Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, crimson.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏰ Missed: $missedTimeStr",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "⚡ Challenge: $challengeStr",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = interFamily
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔥 Streak Lost: $streakLostStr",
                        color = Color(0xFFFF8A3D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "💪 Penalty: $punishmentStr",
                        color = crimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlighted Quote / Caption Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(crimson.copy(alpha = 0.15f), Color(0xFF26101D))
                        )
                    )
                    .border(1.dp, crimson.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "“$captionStr”",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reaction Pills Bar (Gen Z Roasts & Nudges)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reactionsList = if (item.reactions.isEmpty()) {
                        listOf("💀" to 12, "😂" to 7, "🤡" to 4, "🚨" to 8)
                    } else {
                        item.reactions
                    }
                    reactionsList.forEach { (emoji, count) ->
                        ReactionPill(
                            emoji = emoji,
                            count = count,
                            onClick = { onReactionClick(emoji) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionPill(
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        finishedListener = { scale = 1f }
    )

    Row(
        modifier = Modifier
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .clickable {
                scale = 1.3f
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 13.sp)
        Text(
            text = "x$count",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
fun SearchOverlay(
    onDismiss: () -> Unit,
    interFamily: FontFamily
) {
    var searchQuery by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg.copy(alpha = 0.98f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Search Friends",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
                Text(
                    text = "Close",
                    color = AppColorPalette.CyanCta,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username...", color = Color.White.copy(alpha = 0.3f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AppColorPalette.CyanCta,
                    focusedBorderColor = AppColorPalette.CyanCta,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Try searching for \"maya\" or \"5amclub\"",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontFamily = interFamily
            )
        }
    }
}

@Composable
fun NotificationsOverlay(
    onDismiss: () -> Unit,
    interFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg.copy(alpha = 0.98f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recent Notifications",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
                Text(
                    text = "Close",
                    color = AppColorPalette.CyanCta,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NotificationRow("🦁", "maya.rises completed her workout alarm 5m ago", "5m ago")
                NotificationRow("🐺", "5amclub_dani invited you to a new 7-day challenge", "1h ago")
                NotificationRow("🐻", "grind.rio nudged you to upload habit proof", "3h ago")
            }
        }
    }
}

@Composable
fun NotificationRow(avatar: String, message: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColorPalette.Surface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = avatar, fontSize = 24.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = message, color = Color.White, fontSize = 13.sp)
            Text(text = time, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

data class StoryItem(
    val id: String,
    val username: String,
    val avatar: String,
    val borderColor: Color = Color(0xFF00FF94),
    val isUser: Boolean = false,
    val userId: String = "",
    val caption: String = "",
    val badgeText: String = "",
    val badgeColorHex: String = "#00FF94",
    val gradientStartHex: String = "#050811",
    val gradientEndHex: String = "#1A102F",
    val timestamp: Long = 0L,
    val hasActiveStory: Boolean = false
)

data class FeedItem(
    val id: String,
    val username: String,
    val avatar: String,
    val timeAgo: String,
    val streak: Int,
    val badgeText: String,
    val badgeColor: Color,
    val content: String,
    val reactions: List<Pair<String, Int>>,
    val avatarBorderColor: Color,
    val isShameReceipt: Boolean = false,
    val missedTime: String = "",
    val challengeName: String = "",
    val punishmentText: String = "",
    val shameCaption: String = ""
)

private fun formatTimeAgo(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}
