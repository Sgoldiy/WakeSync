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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onStoryClick: (StoryItem) -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Stories mock data matching screenshot
    val stories = remember {
        listOf(
            StoryItem("1", "Your story", "+", Color.White.copy(alpha = 0.15f), isUser = true),
            StoryItem("2", "maya.rises", "🦁", Color(0xFF00FF94)),
            StoryItem("3", "5amclub_dani", "🐺", Color(0xFFFFD23D)),
            StoryItem("4", "grind.rio", "🐻", Color(0xFF00FF94)),
            StoryItem("5", "nocturnaleve", "🐱", Color(0xFFFF3D71))
        )
    }

    // Interactive feed items matching screenshot exactly
    var feedItems by remember {
        mutableStateOf(
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
                    badgeText = "loss",
                    badgeColor = Color(0xFFFF3D71),
                    content = "Slept through again. Assigned: 20 pushups.",
                    reactions = listOf("💀" to 9, "😂" to 5),
                    avatarBorderColor = Color(0xFFFF3D71)
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
        )
    }

    var replyingToItem by remember { mutableStateOf<FeedItem?>(null) }
    var replyText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var notificationsActive by remember { mutableStateOf(false) }

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
                                        // User story upload mock
                                    } else {
                                        onStoryClick(story)
                                    }
                                }
                            )
                        }
                    }
                }

                // Activity Feed Cards (Matches exact space between two cards and edges)
                items(feedItems, key = { it.id }) { item ->
                    ActivityCard(
                        item = item,
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        onReplyClick = { replyingToItem = item },
                        onReactionClick = { emoji ->
                            feedItems = feedItems.map { fit ->
                                if (fit.id == item.id) {
                                    fit.copy(
                                        reactions = fit.reactions.map { (e, c) ->
                                            if (e == emoji) e to (c + 1) else e to c
                                        }
                                    )
                                } else fit
                            }
                        },
                        onUserClick = onUserClick
                    )
                    Spacer(modifier = Modifier.height(12.dp)) // Same spacing as the screenshot
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

        // Notifications Bottom Sheet Mock
        if (notificationsActive) {
            NotificationsOverlay(
                onDismiss = { notificationsActive = false },
                interFamily = interFamily
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
                if (story.isUser) {
                    drawCircle(
                        color = story.borderColor,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 8f), 0f
                            )
                        )
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
    val borderColor: Color,
    val isUser: Boolean = false
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
    val avatarBorderColor: Color
)
