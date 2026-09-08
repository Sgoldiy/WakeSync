package com.social.wakesync.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StorySticker(
    val label: String,
    val colorHex: String,
    val category: String = "Popular"
)

data class GradientPreset(
    val name: String,
    val startHex: String,
    val endHex: String,
    val middleHex: String? = null
)

data class StoryFontStyle(
    val id: String,
    val name: String,
    val family: FontFamily,
    val isUppercase: Boolean = false
)

data class StoryTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val sticker: StorySticker,
    val gradient: GradientPreset,
    val shape: StoryBadgeShape,
    val highlightMode: TextHighlightMode,
    val accentHex: String,
    val defaultCaption: String
)

enum class StoryBadgeShape(val displayName: String, val icon: String) {
    CAPSULE("Capsule Pill", "💊"),
    ROUNDED("Rounded Glass", "⬛"),
    CYBER_TAG("Cyber Ticket", "🎟️"),
    BANNER("Edge Banner", "📜"),
    POLYGON("Polygon Tag", "⚡"),
    DOUBLE_FRAME("Double Glow Frame", "🖼️"),
    SPEECH_BUBBLE("Speech Bubble", "💬"),
    MINIMAL("Floating Text", "✨")
}

enum class TextHighlightMode(val displayName: String) {
    CLEAN("Clean Void"),
    DARK_GLASS("Obsidian Glass"),
    ACCENT_SOLID("Solid Neon"),
    NEON_OUTLINE("Glow Outline")
}

enum class StoryEditorTab(val title: String, val icon: String) {
    NONE("Canvas View", "👁️"),
    TEMPLATES("Templates", "🎨"),
    TEXT("Typography", "T"),
    STICKERS("Badges", "⭐"),
    SHAPES("Shapes", "💬"),
    THEMES("Swatches", "🖌️"),
    EMOJIS("Emojis", "⚡")
}

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
    // ── Font Styles Preset ──────────────────────────────────────────────────
    val fontStyles = remember(titleFamily, interFamily) {
        listOf(
            StoryFontStyle("cyber", "CYBER BOLD", titleFamily, isUppercase = true),
            StoryFontStyle("modern", "MODERN SANS", interFamily),
            StoryFontStyle("serif", "SERIF ELEGANCE", FontFamily.Serif),
            StoryFontStyle("typewriter", "TYPEWRITER CODE", FontFamily.Monospace),
            StoryFontStyle("cursive", "CURSIVE SCRIPT", FontFamily.Cursive)
        )
    }

    // ── Sticker Badges Categories ───────────────────────────────────────────
    val stickers = remember {
        listOf(
            // Wake Up
            StorySticker("🌅 5:00 AM CLUB", "#00FF94", "Wake Up"),
            StorySticker("⏰ EARLY BIRD", "#00E5FF", "Wake Up"),
            StorySticker("⚡ ALARM SMASHED", "#FFD23D", "Wake Up"),
            StorySticker("🔔 NO SNOOZE", "#FF8A3D", "Wake Up"),
            StorySticker("😴 5 MIN MORE", "#A855F7", "Wake Up"),

            // Streaks
            StorySticker("🔥 7-DAY STREAK", "#FF5722", "Streaks"),
            StorySticker("⚡ 14-DAY STREAK", "#FFD23D", "Streaks"),
            StorySticker("🏆 NEW RECORD", "#FF8A3D", "Streaks"),
            StorySticker("🚀 LEVEL UP", "#00FF94", "Streaks"),
            StorySticker("💯 100% STREAK", "#00E5FF", "Streaks"),

            // Habits
            StorySticker("💪 HABIT DONE", "#00FF94", "Habits"),
            StorySticker("🧘 MORNING ROUTINE", "#A855F7", "Habits"),
            StorySticker("💧 HYDRATED GOAL", "#00E5FF", "Habits"),
            StorySticker("🏃 MORNING RUN", "#FFD23D", "Habits"),
            StorySticker("🧠 DEEP FOCUS", "#EC4899", "Habits"),

            // Fails
            StorySticker("📖 FAIL STORY", "#FF3D71", "Fails"),
            StorySticker("🛌 OVERSLEPT", "#EC4899", "Fails"),
            StorySticker("☕ COFFEE FIRST", "#FF8A3D", "Fails"),
            StorySticker("🧟 ZOMBIE MODE", "#A855F7", "Fails"),
            StorySticker("💀 BEAT BY ALARM", "#FF3D71", "Fails")
        )
    }

    val quickEmojis = remember {
        listOf("🔥", "⚡", "🌅", "🏆", "⏰", "💪", "🚀", "💯", "☕", "👑", "🎯", "🌟", "💥", "🎉", "😴", "🛌", "💀", "😂")
    }

    // ── 16 Vibrant Color Swatches ──────────────────────────────────────────
    val colorSwatches = remember {
        listOf(
            "#00FF94", "#00E5FF", "#FFD23D", "#A855F7", "#EC4899",
            "#FF5722", "#FF3D71", "#2563EB", "#10B981", "#84CC16",
            "#F59E0B", "#7C3AED", "#F43F5E", "#06B6D4", "#E11D48", "#FFFFFF"
        )
    }

    // ── 8 Deep Dark Cyber Void Gradients ─────────────────────────────────────
    val gradients = remember {
        listOf(
            GradientPreset("Obsidian Cyber", "#050811", "#120924"),
            GradientPreset("Neon Matrix", "#00120B", "#002B1D"),
            GradientPreset("Midnight Purple", "#11001C", "#2D0036"),
            GradientPreset("Deep Space Cyan", "#00141D", "#002B3D"),
            GradientPreset("Dark Sunset Flame", "#1A0500", "#3D0F00"),
            GradientPreset("Vaporwave Dark", "#180018", "#3B003B"),
            GradientPreset("Carbon Gold", "#121000", "#332B00"),
            GradientPreset("Blood Moon", "#1A0005", "#3B000B")
        )
    }

    // ── 6 Pre-Designed Story Templates ──────────────────────────────────────
    val templates = remember {
        listOf(
            StoryTemplate(
                id = "5am_club",
                name = "5:00 AM Club",
                icon = "🌅",
                sticker = StorySticker("🌅 5:00 AM CLUB", "#00FF94", "Wake Up"),
                gradient = GradientPreset("Obsidian Cyber", "#050811", "#120924"),
                shape = StoryBadgeShape.CAPSULE,
                highlightMode = TextHighlightMode.DARK_GLASS,
                accentHex = "#00FF94",
                defaultCaption = "RISE & SHINE - UNLOCK YOUR POTENTIAL ⚡"
            ),
            StoryTemplate(
                id = "streak_14",
                name = "14-Day Streak",
                icon = "🔥",
                sticker = StorySticker("⚡ 14-DAY STREAK", "#FFD23D", "Streaks"),
                gradient = GradientPreset("Dark Sunset Flame", "#1A0500", "#3D0F00"),
                shape = StoryBadgeShape.DOUBLE_FRAME,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FFD23D",
                defaultCaption = "UNSTOPPABLE MOMENTUM 🔥 14 DAYS STRONG"
            ),
            StoryTemplate(
                id = "routine",
                name = "Morning Routine",
                icon = "💪",
                sticker = StorySticker("💪 HABIT DONE", "#00E5FF", "Habits"),
                gradient = GradientPreset("Deep Space Cyan", "#00141D", "#002B3D"),
                shape = StoryBadgeShape.CYBER_TAG,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#00E5FF",
                defaultCaption = "MORNING ROUTINE SMASHED 💪 READY TO CONQUER"
            ),
            StoryTemplate(
                id = "record",
                name = "Personal Record",
                icon = "🏆",
                sticker = StorySticker("🏆 NEW RECORD", "#FF8A3D", "Streaks"),
                gradient = GradientPreset("Carbon Gold", "#121000", "#332B00"),
                shape = StoryBadgeShape.POLYGON,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FF8A3D",
                defaultCaption = "NEW PERSONAL RECORD SET 🏆 LEVEL UP!"
            ),
            StoryTemplate(
                id = "fail",
                name = "Fail Story",
                icon = "📖",
                sticker = StorySticker("📖 FAIL STORY", "#FF3D71", "Fails"),
                gradient = GradientPreset("Blood Moon", "#1A0005", "#3B000B"),
                shape = StoryBadgeShape.SPEECH_BUBBLE,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#FF3D71",
                defaultCaption = "OVERSLEPT TODAY 🛌 BOUNCING BACK TOMORROW"
            ),
            StoryTemplate(
                id = "focus",
                name = "Deep Focus",
                icon = "🧠",
                sticker = StorySticker("🧠 DEEP FOCUS", "#A855F7", "Habits"),
                gradient = GradientPreset("Midnight Purple", "#11001C", "#2D0036"),
                shape = StoryBadgeShape.BANNER,
                highlightMode = TextHighlightMode.DARK_GLASS,
                accentHex = "#A855F7",
                defaultCaption = "IN THE ZONE 🧠 100% FOCUS MODE"
            )
        )
    }

    // ── Editor State Variables ──────────────────────────────────────────────
    var captionText by remember { mutableStateOf("") }
    var selectedSticker by remember { mutableStateOf(stickers[0]) }
    var selectedGradient by remember { mutableStateOf(gradients[0]) }
    var selectedFont by remember { mutableStateOf(fontStyles[0]) }
    var selectedShape by remember { mutableStateOf(StoryBadgeShape.CAPSULE) }
    var textHighlight by remember { mutableStateOf(TextHighlightMode.CLEAN) }
    var textAlign by remember { mutableStateOf(TextAlign.Center) }
    var activeTab by remember { mutableStateOf(StoryEditorTab.TEXT) }
    var selectedStickerCategory by remember { mutableStateOf("Wake Up") }
    var isPosting by remember { mutableStateOf(false) }
    var selectedAccentColorHex by remember { mutableStateOf("#00FF94") }

    fun parseColor(hex: String): Color {
        return try {
            val cleanedHex = hex.removePrefix("#")
            Color(cleanedHex.toLong(16) or 0xFF000000)
        } catch (e: Exception) {
            Color(0xFF050811)
        }
    }

    fun applyTemplate(template: StoryTemplate) {
        selectedSticker = template.sticker
        selectedGradient = template.gradient
        selectedShape = template.shape
        textHighlight = template.highlightMode
        selectedAccentColorHex = template.accentHex
        captionText = template.defaultCaption
    }

    val canvasBrush = remember(selectedGradient) {
        val start = parseColor(selectedGradient.startHex)
        val end = parseColor(selectedGradient.endHex)
        val midHex = selectedGradient.middleHex
        if (midHex != null) {
            val mid = parseColor(midHex)
            Brush.verticalGradient(listOf(start, mid, end))
        } else {
            Brush.verticalGradient(listOf(start, end))
        }
    }

    val stickerColor = remember(selectedSticker) {
        parseColor(selectedSticker.colorHex)
    }

    val accentColor = remember(selectedAccentColorHex) {
        parseColor(selectedAccentColorHex)
    }

    fun getBadgeShape(badgeShape: StoryBadgeShape): Shape {
        return when (badgeShape) {
            StoryBadgeShape.CAPSULE -> CircleShape
            StoryBadgeShape.ROUNDED -> RoundedCornerShape(18.dp)
            StoryBadgeShape.CYBER_TAG -> CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp)
            StoryBadgeShape.BANNER -> RoundedCornerShape(4.dp)
            StoryBadgeShape.POLYGON -> CutCornerShape(12.dp)
            StoryBadgeShape.DOUBLE_FRAME -> RoundedCornerShape(16.dp)
            StoryBadgeShape.SPEECH_BUBBLE -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
            StoryBadgeShape.MINIMAL -> RoundedCornerShape(0.dp)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
    ) {
        // ── 1. Full-Screen Story Canvas Background ──────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(canvasBrush)
                .clickable {
                    activeTab = if (activeTab == StoryEditorTab.NONE) StoryEditorTab.TEXT else StoryEditorTab.NONE
                }
                .padding(20.dp)
        ) {
            // Story Header (Top Left User Avatar & Username)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.5.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = userAvatar.ifEmpty { "👤" }, fontSize = 20.sp)
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
                        color = accentColor.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontFamily = interFamily
                    )
                }
            }

            // Canvas Center Content (Badge + Dynamic Styled Caption)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Sticker Badge Container
                if (selectedShape == StoryBadgeShape.MINIMAL) {
                    Text(
                        text = selectedSticker.label,
                        color = stickerColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = interFamily
                    )
                } else {
                    val shape = getBadgeShape(selectedShape)
                    val isDouble = selectedShape == StoryBadgeShape.DOUBLE_FRAME
                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(if (isDouble) 3.dp else 2.dp, stickerColor, shape)
                            .padding(horizontal = 22.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = selectedSticker.label,
                            color = stickerColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFamily
                        )
                    }
                }

                // Styled Text Caption
                val textToDisplay = if (captionText.isBlank()) {
                    "NO SNOOZE, NO RETREAT 🔥"
                } else if (selectedFont.isUppercase) {
                    captionText.uppercase()
                } else {
                    captionText
                }

                val textColor = when (textHighlight) {
                    TextHighlightMode.CLEAN -> if (captionText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.65f)
                    TextHighlightMode.DARK_GLASS -> Color.White
                    TextHighlightMode.ACCENT_SOLID -> Color.Black
                    TextHighlightMode.NEON_OUTLINE -> accentColor
                }

                val textBgModifier = when (textHighlight) {
                    TextHighlightMode.CLEAN -> Modifier
                    TextHighlightMode.DARK_GLASS -> Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                    TextHighlightMode.ACCENT_SOLID -> Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(accentColor)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                    TextHighlightMode.NEON_OUTLINE -> Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(2.dp, accentColor, RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                }

                Box(
                    modifier = textBgModifier.clickable { activeTab = StoryEditorTab.TEXT },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = textToDisplay,
                        color = textColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = selectedFont.family,
                        textAlign = textAlign,
                        lineHeight = 32.sp
                    )
                }
            }
        }

        // ── 2. Top-Left Close (X) & Top-Center Templates Pill ────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Top-Center Floating Templates Pill Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color(0xFF00FF94).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { activeTab = StoryEditorTab.TEMPLATES }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Templates", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                Text(text = "🎨", fontSize = 14.sp)
            }

            // Spacer for top right dock alignment offset
            Spacer(modifier = Modifier.width(42.dp))
        }

        // ── 3. Top-Right Vertical Floating Tool Dock (Starting from Top) ─────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 16.dp, top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(26.dp))
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                listOf(
                    StoryEditorTab.TEMPLATES,
                    StoryEditorTab.TEXT,
                    StoryEditorTab.STICKERS,
                    StoryEditorTab.SHAPES,
                    StoryEditorTab.THEMES,
                    StoryEditorTab.EMOJIS
                ).forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) accentColor.copy(alpha = 0.35f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) accentColor else Color.Transparent,
                                CircleShape
                            )
                            .clickable { activeTab = if (isSelected) StoryEditorTab.NONE else tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.icon,
                            fontSize = 16.sp,
                            color = if (isSelected) accentColor else Color.White
                        )
                    }
                }
            }
        }

        // ── 4. Authentic Instagram Bottom Story Footer ───────────────────────
        if (activeTab == StoryEditorTab.NONE) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Your Story Button (Profile Avatar + Green Plus)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .clickable {
                            if (!isPosting) {
                                isPosting = true
                                onPostStory(
                                    if (selectedFont.isUppercase) captionText.uppercase() else captionText,
                                    selectedSticker.label,
                                    selectedSticker.colorHex,
                                    selectedGradient.startHex,
                                    selectedGradient.endHex
                                )
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userAvatar.ifEmpty { "👤" }, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FF94)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Your Story",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }

                // Center: Close Friends Star Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF00FF94).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00FF94), RoundedCornerShape(24.dp))
                        .clickable {
                            if (!isPosting) {
                                isPosting = true
                                onPostStory(
                                    if (selectedFont.isUppercase) captionText.uppercase() else captionText,
                                    selectedSticker.label,
                                    selectedSticker.colorHex,
                                    selectedGradient.startHex,
                                    selectedGradient.endHex
                                )
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "⭐", fontSize = 12.sp)
                    Text(
                        text = "Close Friends",
                        color = Color(0xFF00FF94),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }

                // Right: Send To Pill Button
                Button(
                    onClick = {
                        if (!isPosting) {
                            isPosting = true
                            onPostStory(
                                if (selectedFont.isUppercase) captionText.uppercase() else captionText,
                                selectedSticker.label,
                                selectedSticker.colorHex,
                                selectedGradient.startHex,
                                selectedGradient.endHex
                            )
                        }
                    },
                    enabled = !isPosting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isPosting) "Posting..." else "Send To",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            fontFamily = interFamily
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Share",
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // ── 5. High-Tech Dark Control Drawer Sheet ───────────────────────────
        AnimatedVisibility(
            visible = activeTab != StoryEditorTab.NONE,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color(0xFF0C101D).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .align(Alignment.CenterHorizontally)
                            .clickable { activeTab = StoryEditorTab.NONE }
                    )

                    // Drawer Filter Chips Bar
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(
                            StoryEditorTab.TEMPLATES,
                            StoryEditorTab.TEXT,
                            StoryEditorTab.STICKERS,
                            StoryEditorTab.SHAPES,
                            StoryEditorTab.THEMES,
                            StoryEditorTab.EMOJIS
                        )) { tab ->
                            val isSelected = activeTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { activeTab = tab },
                                label = {
                                    Text(
                                        text = "${tab.icon} ${tab.title}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = interFamily
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    labelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha = 0.15f),
                                    selectedBorderColor = accentColor
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }

                    // Drawer Dynamic Tool Panel
                    Crossfade(targetState = activeTab) { tab ->
                        when (tab) {
                            StoryEditorTab.TEMPLATES -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "1-TAP PRESET STORY TEMPLATES (6 STYLES)",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(templates) { template ->
                                            val tAccent = parseColor(template.accentHex)
                                            Box(
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color.Black.copy(alpha = 0.7f))
                                                    .border(1.5.dp, tAccent, RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        applyTemplate(template)
                                                        activeTab = StoryEditorTab.NONE
                                                    }
                                                    .padding(12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = template.icon, fontSize = 24.sp)
                                                    Text(
                                                        text = template.name,
                                                        color = tAccent,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = interFamily,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            StoryEditorTab.TEXT -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = captionText,
                                        onValueChange = { if (it.length <= 140) captionText = it },
                                        placeholder = {
                                            Text(
                                                text = "Type story caption...",
                                                color = Color.White.copy(alpha = 0.35f),
                                                fontFamily = interFamily
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White.copy(alpha = 0.06f),
                                            unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                            focusedBorderColor = accentColor,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "FONT TYPOGRAPHY",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf(TextAlign.Left, TextAlign.Center, TextAlign.Right).forEach { align ->
                                                val isSelected = textAlign == align
                                                val icon = when (align) {
                                                    TextAlign.Left -> "⬅️"
                                                    TextAlign.Center -> "↔️"
                                                    else -> "➡️"
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) accentColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f))
                                                        .clickable { textAlign = align }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(text = icon, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(fontStyles) { font ->
                                            val isSelected = selectedFont == font
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) accentColor else Color.White.copy(alpha = 0.08f))
                                                    .clickable { selectedFont = font }
                                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = font.name,
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = font.family
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "HIGHLIGHT MODE",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(TextHighlightMode.values().toList()) { mode ->
                                            val isSelected = textHighlight == mode
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { textHighlight = mode }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = mode.displayName,
                                                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f),
                                                    fontSize = 11.sp,
                                                    fontFamily = interFamily
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            StoryEditorTab.STICKERS -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val categories = listOf("Wake Up", "Streaks", "Habits", "Fails")
                                        items(categories) { cat ->
                                            val isSelected = selectedStickerCategory == cat
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) Color(0xFFFFD23D) else Color.White.copy(alpha = 0.08f))
                                                    .clickable { selectedStickerCategory = cat }
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = cat,
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interFamily
                                                )
                                            }
                                        }
                                    }

                                    val filteredStickers = remember(selectedStickerCategory) {
                                        stickers.filter { it.category == selectedStickerCategory }
                                    }
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(filteredStickers) { sticker ->
                                            val color = parseColor(sticker.colorHex)
                                            val isSelected = selectedSticker == sticker
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(if (isSelected) color.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f))
                                                    .border(
                                                        1.5.dp,
                                                        if (isSelected) color else Color.White.copy(alpha = 0.15f),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { selectedSticker = sticker }
                                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = sticker.label,
                                                    color = if (isSelected) color else Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interFamily
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            StoryEditorTab.SHAPES -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "SELECT CONTAINER SHAPE (8 STYLES)",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(StoryBadgeShape.values().toList()) { shapeStyle ->
                                            val isSelected = selectedShape == shapeStyle
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                                                    .border(
                                                        1.5.dp,
                                                        if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { selectedShape = shapeStyle }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = shapeStyle.icon, fontSize = 16.sp)
                                                    Text(
                                                        text = shapeStyle.displayName,
                                                        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = interFamily
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            StoryEditorTab.THEMES -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "ACCENT COLOR SWATCHES (16 VIBRANT TONES)",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(colorSwatches) { hex ->
                                            val color = parseColor(hex)
                                            val isSelected = selectedAccentColorHex.equals(hex, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(
                                                        if (isSelected) 3.dp else 1.dp,
                                                        if (isSelected) Color.White else Color.Transparent,
                                                        CircleShape
                                                    )
                                                    .clickable { selectedAccentColorHex = hex }
                                            )
                                        }
                                    }

                                    Text(
                                        text = "DARK VOID CANVASES",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(gradients) { preset ->
                                            val isSelected = selectedGradient == preset
                                            val startColor = parseColor(preset.startHex)
                                            val endColor = parseColor(preset.endHex)
                                            val midHex = preset.middleHex
                                            val brush = if (midHex != null) {
                                                Brush.verticalGradient(listOf(startColor, parseColor(midHex), endColor))
                                            } else {
                                                Brush.verticalGradient(listOf(startColor, endColor))
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(brush)
                                                        .border(
                                                            if (isSelected) 2.5.dp else 1.dp,
                                                            if (isSelected) accentColor else Color.White.copy(alpha = 0.25f),
                                                            CircleShape
                                                        )
                                                        .clickable { selectedGradient = preset }
                                                )
                                                Text(
                                                    text = preset.name,
                                                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.5f),
                                                    fontSize = 10.sp,
                                                    fontFamily = interFamily
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            StoryEditorTab.EMOJIS -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "QUICK EMOJI TAP (APPEND TO CAPTION)",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(quickEmojis) { emoji ->
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.08f))
                                                    .clickable {
                                                        if (captionText.length + emoji.length <= 140) {
                                                            captionText = if (captionText.isBlank()) emoji else "$captionText $emoji"
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = emoji, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            StoryEditorTab.NONE -> {}
                        }
                    }
                }
            }
        }
    }
}





