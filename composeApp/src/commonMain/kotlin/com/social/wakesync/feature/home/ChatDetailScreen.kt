package com.social.wakesync.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

data class MessageItem(
    val id: String,
    val sender: String,
    val avatar: String,
    val message: String,
    val timestamp: String,
    val isIncoming: Boolean,
    val hasProofPhoto: Boolean = false
)

@Composable
fun ChatDetailScreen(
    chatId: String,
    onBack: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    // Determine info based on chatId (supporting the screenshot mock or general chat items)
    val chatTitle = when (chatId) {
        "1" -> "Morning Crew 🌅"
        "3" -> "5AM Club"
        "6" -> "Work Grinders"
        "2" -> "maya.rises"
        "4" -> "5amclub_dani"
        "5" -> "grind.rio"
        else -> "Morning Crew 🌅"
    }

    val chatAvatar = when (chatId) {
        "1" -> "🌅"
        "2" -> "🦁"
        "3" -> "⚡"
        "4" -> "🐺"
        "5" -> "🐻"
        "6" -> "💼"
        else -> "🌅"
    }

    var messageText by remember { mutableStateOf("") }
    
    // Hardcoded message list mimicking the conversation from the screenshot exactly
    val messages = remember {
        mutableStateListOf(
            MessageItem("1", "5amclub_dani", "🐺", "Don't be last today 💀", "6:32", isIncoming = true),
            MessageItem("2", "maya.rises", "🦁", "Already done. 0:58. Beat that.", "6:34", isIncoming = true),
            MessageItem("3", "You", "", "On it. Give me 10 seconds", "6:35", isIncoming = false),
            MessageItem("4", "grind.rio", "🐻", "20 pushups. Done. 💪", "6:36", isIncoming = true, hasProofPhoto = true)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chatAvatar,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatTitle,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily
                )
                Text(
                    text = "5 members · 3 active now",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W400
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

        // Pinned Punishment Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.01f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.PushPin,
                contentDescription = "Pin",
                tint = Color(0xFFFF5757),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Punishment deadline: 20 pushups · Due 8:30 AM",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W500,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "View",
                color = Color(0xFFFF8A3D),
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily,
                modifier = Modifier.clickable { /* View details action */ }
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

        // Chat conversation area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today, 6:31 AM header
                item {
                    Text(
                        text = "Today, 6:31 AM",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontFamily = interFamily,
                        fontWeight = FontWeight.W400,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // 5amclub_dani finished 1st status badge
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColorPalette.WinGreen.copy(alpha = 0.08f))
                                .border(1.dp, AppColorPalette.WinGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "5amclub_dani finished 1st 🏆",
                                color = AppColorPalette.WinGreen,
                                fontSize = 12.sp,
                                fontFamily = interFamily,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }
                }

                // Render messages list
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        interFamily = interFamily
                    )
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Message Input field pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "Message...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 15.sp,
                                fontFamily = interFamily
                            )
                        }
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = interFamily
                            ),
                            cursorBrush = SolidColor(AppColorPalette.CyanCta),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Camera/Media Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { /* Attach image proof */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = "Camera",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Send Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.CyanCta)
                    .clickable {
                        if (messageText.trim().isNotEmpty()) {
                            messages.add(
                                MessageItem(
                                    id = (messages.size + 1).toString(),
                                    sender = "You",
                                    avatar = "",
                                    content = messageText,
                                    time = "6:37",
                                    isIncoming = false
                                )
                            )
                            messageText = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    tint = AppColorPalette.VoidBg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

data class MessageItem(
    val id: String,
    val sender: String,
    val avatar: String,
    val content: String,
    val time: String,
    val isIncoming: Boolean,
    val hasProofPhoto: Boolean = false
)

@Composable
fun MessageBubble(
    message: MessageItem,
    interFamily: FontFamily
) {
    if (message.isIncoming) {
        // Left Aligned Message
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = message.sender,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontFamily = interFamily,
                modifier = Modifier.padding(start = 52.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // Small round avatar on left of the message
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColorPalette.Surface)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message.avatar,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    // Bubble Box
                    Box(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppColorPalette.Surface)
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (message.hasProofPhoto) {
                                // Custom canvas diagonal stripes image placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Darker blue background
                                        drawRect(color = Color(0xFF14192B))
                                        
                                        // Diagonal stripes matching the mockup
                                        val stripeWidth = 15.dp.toPx()
                                        val stripeSpacing = 20.dp.toPx()
                                        val totalWidth = size.width
                                        val totalHeight = size.height
                                        val paintColor = Color.White.copy(alpha = 0.04f)
                                        
                                        var x = -totalHeight
                                        while (x < totalWidth) {
                                            val path = Path().apply {
                                                moveTo(x, 0f)
                                                lineTo(x + stripeWidth, 0f)
                                                lineTo(x + stripeWidth + totalHeight, totalHeight)
                                                lineTo(x + totalHeight, totalHeight)
                                                close()
                                            }
                                            drawPath(path, paintColor)
                                            x += stripeWidth + stripeSpacing
                                        }
                                    }
                                    
                                    // Centered Icon & Subtitle inside diagonal stripes container
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "🏋️",
                                            fontSize = 24.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "proof photo",
                                            color = Color.White.copy(alpha = 0.3f),
                                            fontSize = 11.sp,
                                            fontFamily = interFamily,
                                            fontWeight = FontWeight.W500
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = message.content,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontFamily = interFamily,
                                fontWeight = FontWeight.W400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.time,
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 11.sp,
                        fontFamily = interFamily,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    } else {
        // Right Aligned Message (Outgoing)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColorPalette.VoidBg)
                    .border(1.5.dp, AppColorPalette.CyanCta.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W400
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.time,
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 11.sp,
                fontFamily = interFamily,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}
