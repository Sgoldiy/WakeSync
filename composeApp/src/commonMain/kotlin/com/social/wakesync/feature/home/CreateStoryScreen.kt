package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StorySticker(
    val label: String,
    val colorHex: String
)

data class GradientPreset(
    val name: String,
    val startHex: String,
    val endHex: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    userAvatar: String,
    username: String,
    onClose: () -> Unit,
    onPostStory: (caption: String, badgeText: String, badgeColorHex: String, bgStartHex: String, bgEndHex: String) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val stickers = remember {
        listOf(
            StorySticker("🌅 5:00 AM Wake Up", "#00FF94"),
            StorySticker("⚡ 14-Day Streak", "#FFD23D"),
            StorySticker("🏆 New Record", "#FF8A3D"),
            StorySticker("📖 Fail Story", "#FF3D71"),
            StorySticker("💪 Habit Done", "#00E5FF")
        )
    }

    val gradients = remember {
        listOf(
            GradientPreset("Cyber Dark", "#050811", "#1A102F"),
            GradientPreset("Sunset Glow", "#2D0B00", "#6A1B00"),
            GradientPreset("Emerald Flame", "#002B1D", "#005C3E"),
            GradientPreset("Royal Violet", "#210B3B", "#4A154B")
        )
    }

    var selectedSticker by remember { mutableStateOf(stickers[0]) }
    var selectedGradient by remember { mutableStateOf(gradients[0]) }
    var captionText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    fun parseColor(hex: String): Color {
        return try {
            val cleanedHex = hex.removePrefix("#")
            Color(cleanedHex.toLong(16) or 0xFF000000)
        } catch (e: Exception) {
            Color(0xFF050811)
        }
    }

    val canvasBrush = remember(selectedGradient) {
        Brush.verticalGradient(
            colors = listOf(
                parseColor(selectedGradient.startHex),
                parseColor(selectedGradient.endHex)
            )
        )
    }

    val stickerColor = remember(selectedSticker) {
        parseColor(selectedSticker.colorHex)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Top Action Bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }

                Text(
                    text = "CREATE STORY",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = titleFamily,
                    letterSpacing = 1.5.sp
                )

                Button(
                    onClick = {
                        if (!isPosting) {
                            isPosting = true
                            onPostStory(
                                captionText,
                                selectedSticker.label,
                                selectedSticker.colorHex,
                                selectedGradient.startHex,
                                selectedGradient.endHex
                            )
                        }
                    },
                    enabled = !isPosting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF94),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isPosting) "Sharing..." else "Share",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = interFamily
                        )
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Main Story Canvas Preview ─────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(canvasBrush)
                        .padding(20.dp)
                ) {
                    // Top User Header Info
                    Row(
                        modifier = Modifier.align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userAvatar.ifEmpty { "👤" }, fontSize = 18.sp)
                        }

                        Column {
                            Text(
                                text = username.ifEmpty { "You" },
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                            Text(
                                text = "Your Story",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontFamily = interFamily
                            )
                        }
                    }

                    // Center Canvas Content (Sticker Badge + Text Caption)
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Badge Sticker Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(stickerColor.copy(alpha = 0.15f))
                                .border(1.5.dp, stickerColor, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = selectedSticker.label,
                                color = stickerColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }

                        // Story Caption Text Preview
                        Text(
                            text = captionText.ifBlank { "Tap caption field below to add a message..." },
                            color = if (captionText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.4f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFamily,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // ── Controls Section (Stickers, Gradients, Caption Input) ───────
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sticker Selectors
                Text(
                    text = "Select Sticker Badge",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = interFamily
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stickers) { sticker ->
                        val color = parseColor(sticker.colorHex)
                        val isSelected = selectedSticker == sticker
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    1.dp,
                                    if (isSelected) color else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedSticker = sticker }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sticker.label,
                                color = if (isSelected) color else Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = interFamily
                            )
                        }
                    }
                }

                // Background Gradient Presets
                Text(
                    text = "Background Theme",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontFamily = interFamily
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(gradients) { preset ->
                        val isSelected = selectedGradient == preset
                        val brush = Brush.verticalGradient(
                            listOf(parseColor(preset.startHex), parseColor(preset.endHex))
                        )
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(brush)
                                .border(
                                    if (isSelected) 2.5.dp else 1.dp,
                                    if (isSelected) Color(0xFF00FF94) else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { selectedGradient = preset }
                        )
                    }
                }

                // Caption Input Field
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { if (it.length <= 140) captionText = it },
                    placeholder = {
                        Text(
                            text = "Add a caption...",
                            color = Color.White.copy(alpha = 0.3f),
                            fontFamily = interFamily
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedBorderColor = Color(0xFF00FF94),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }
        }
    }
}
