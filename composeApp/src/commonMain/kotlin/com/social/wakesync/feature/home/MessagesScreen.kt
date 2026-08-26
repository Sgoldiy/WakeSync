package com.social.wakesync.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun MessagesScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onChatClick: (ChatItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val chats = remember {
        listOf(
            ChatItem("1", "Morning Crew 🌅", "5amclub_dani: Don't be last today 💀", "🌅"),
            ChatItem("2", "maya.rises", "You: Rematch tomorrow. 6AM.", "🦁"),
            ChatItem("3", "5AM Club", "Dani finished 1st again 🏆", "⚡"),
            ChatItem("4", "5amclub_dani", "Don't sleep on me again lol", "🐺"),
            ChatItem("5", "grind.rio", "Proof submitted. Judge me.", "🐻"),
            ChatItem("6", "Work Grinders", "nocturnaleve joined the group", "💼")
        )
    }

    val filteredChats = chats.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header (Omitted the right-side plus icon button as requested)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Styled Search Bar matching the mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColorPalette.Surface)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search chats...",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 15.sp,
                            fontFamily = interFamily
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
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

        Spacer(modifier = Modifier.height(24.dp))

        // Chat List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredChats) { chat ->
                ChatRowItem(
                    chat = chat,
                    titleFamily = titleFamily,
                    interFamily = interFamily,
                    onClick = { onChatClick(chat) }
                )
            }
        }
    }
}

data class ChatItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val avatar: String
)

@Composable
fun ChatRowItem(
    chat: ChatItem,
    titleFamily: FontFamily,
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Box (Clean square-rounded container, no badge icon on the right side)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColorPalette.Surface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.avatar,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content: Title & Message (No time labels or right-side badges/icons)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = chat.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                fontFamily = interFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
