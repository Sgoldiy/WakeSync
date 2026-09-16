package com.social.wakesync.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Photo Filter Presets (ColorMatrix-based, KMP-safe) ─────────────────────
enum class StoryPhotoFilter(
    val displayName: String,
    val icon: String,
    val matrix: ColorMatrix?
) {
    NONE("Original", "🚫", null),
    GRAYSCALE("Grayscale", "🖤", ColorMatrix().apply { setToSaturation(0f) }),
    SEPIA("Sepia", "🟤", ColorMatrix(floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))),
    COOL("Cool", "🧊", ColorMatrix(floatArrayOf(
        0.9f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.15f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))),
    WARM("Warm", "🔥", ColorMatrix(floatArrayOf(
        1.12f, 0f, 0f, 0f, 0f,
        0f, 1.02f, 0f, 0f, 0f,
        0f, 0f, 0.85f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))),
    NOIR("Noir", "🎩", ColorMatrix(floatArrayOf(
        0.45f, 0.5f, 0.35f, 0f, -0.05f,
        0.45f, 0.5f, 0.35f, 0f, -0.05f,
        0.45f, 0.5f, 0.35f, 0f, -0.05f,
        0f, 0f, 0f, 1f, 0f
    ))),
    VIVID("Vivid", "✨", ColorMatrix().apply { setToSaturation(1.6f) }),
    FADE("Faded", "🌫️", ColorMatrix(floatArrayOf(
        0.85f, 0f, 0f, 0f, 0.09f,
        0f, 0.85f, 0f, 0f, 0.09f,
        0f, 0f, 0.85f, 0f, 0.09f,
        0f, 0f, 0f, 1f, 0f
    )))
}

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
    val defaultCaption: String,
    val category: String = "Popular"
)

enum class StoryBadgeShape(val displayName: String, val icon: String) {
    CAPSULE("Capsule Pill", "💊"),
    ROUNDED("Rounded Glass", "⬛"),
    CYBER_TAG("Cyber Ticket", "🎟️"),
    BANNER("Edge Banner", "📜"),
    POLYGON("Polygon Tag", "⚡"),
    DOUBLE_FRAME("Double Glow Frame", "🖼️"),
    SPEECH_BUBBLE("Speech Bubble", "💬"),
    MINIMAL("Floating Text", "✨"),
    HEART("Heart Shape", "❤️"),
    STAR("Star Burst", "⭐"),
    RIBBON("Winner Ribbon", "🎀"),
    CLOUD("Soft Cloud", "☁️"),
    DIAMOND("Diamond Gem", "💎")
}

enum class TextHighlightMode(val displayName: String) {
    CLEAN("Clean Void"),
    DARK_GLASS("Obsidian Glass"),
    ACCENT_SOLID("Solid Neon"),
    NEON_OUTLINE("Glow Outline")
}

// ── Custom Decorative Story Shapes ──────────────────────────────────────────
val HeartBadgeShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, h * 0.35f)
    cubicTo(w * 0.4f, h * 0.05f, w * 0.1f, h * 0.05f, w * 0.1f, h * 0.3f)
    cubicTo(w * 0.1f, h * 0.55f, w * 0.35f, h * 0.72f, w * 0.5f, h * 0.92f)
    cubicTo(w * 0.65f, h * 0.72f, w * 0.9f, h * 0.55f, w * 0.9f, h * 0.3f)
    cubicTo(w * 0.9f, h * 0.05f, w * 0.6f, h * 0.05f, w * 0.5f, h * 0.35f)
    close()
}

val StarBadgeShape: Shape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outer = size.width / 2f
    val inner = outer * 0.45f
    var first = true
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val angle = -kotlin.math.PI / 2 + i * kotlin.math.PI / 5
        val x = cx + (r * kotlin.math.cos(angle)).toFloat()
        val y = cy + (r * kotlin.math.sin(angle)).toFloat()
        if (first) { moveTo(x, y); first = false } else lineTo(x, y)
    }
    close()
}

val CloudBadgeShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.2f, h * 0.75f)
    lineTo(w * 0.8f, h * 0.75f)
    cubicTo(w * 1.02f, h * 0.75f, w * 1.0f, h * 0.45f, w * 0.85f, h * 0.42f)
    cubicTo(w * 0.9f, h * 0.16f, w * 0.58f, h * 0.08f, w * 0.55f, h * 0.28f)
    cubicTo(w * 0.4f, h * 0.0f, w * 0.08f, h * 0.18f, w * 0.18f, h * 0.42f)
    cubicTo(w * 0.0f, h * 0.46f, w * 0.0f, h * 0.75f, w * 0.2f, h * 0.75f)
    close()
}

val DiamondBadgeShape: Shape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)
    lineTo(size.width, size.height / 2f)
    lineTo(size.width / 2f, size.height)
    lineTo(0f, size.height / 2f)
    close()
}

enum class StoryEditorTab(val title: String, val icon: String) {
    NONE("Canvas View", "👁️"),
    TEMPLATES("Templates", "🎨"),
    PHOTO("Photo BG", "📷"),
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
            StorySticker("🏋️ GYM CRUSHED", "#2FBF71", "Habits"),
            StorySticker("📚 BOOK DONE", "#FFD23D", "Habits"),
            StorySticker("💻 DEEP WORK", "#00E5FF", "Habits"),

            // Fails
            StorySticker("📖 FAIL STORY", "#FF3D71", "Fails"),
            StorySticker("🛌 OVERSLEPT", "#EC4899", "Fails"),
            StorySticker("☕ COFFEE FIRST", "#FF8A3D", "Fails"),
            StorySticker("🧟 ZOMBIE MODE", "#A855F7", "Fails"),
            StorySticker("💀 BEAT BY ALARM", "#FF3D71", "Fails"),
            StorySticker("🥶 SLEPT THROUGH", "#B388FF", "Fails"),

            // Motivation
            StorySticker("🎯 GOAL CRUSHED", "#00FF94", "Motivation"),
            StorySticker("🌟 SHINE BRIGHT", "#FFD23D", "Motivation"),
            StorySticker("💃 VICTORY LAP", "#FF8A3D", "Motivation"),
            StorySticker("🙏 GRATEFUL", "#00E5FF", "Motivation"),
            StorySticker("👑 KING ENERGY", "#FFD23D", "Motivation"),

            // Weekend
            StorySticker("🎉 WEEKEND MODE", "#FF8A3D", "Weekend"),
            StorySticker("🦉 NIGHT OWL", "#B388FF", "Weekend"),
            StorySticker("🏋️ REST DAY", "#2FBF71", "Weekend"),
            StorySticker("🧘 CALM MIND", "#A855F7", "Weekend"),
            StorySticker("📵 PHONE DETOX", "#EC4899", "Weekend")
        )
    }

    val quickEmojis = remember {
        listOf(
            "🔥", "⚡", "🌅", "🏆", "⏰", "💪", "🚀", "💯", "☕", "👑",
            "🎯", "🌟", "💥", "🎉", "😴", "🛌", "💀", "😂",
            "❤️", "🧠", "📚", "🧘", "🏋️", "🙏", "💧", "🦉",
            "📵", "💃", "🎵", "🌈", "✨", "😎", "🍀", "🎂",
            "🐐", "💜"
        )
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
            GradientPreset("Blood Moon", "#1A0005", "#3B000B"),
            GradientPreset("Aurora Teal", "#00131A", "#007A6E", middleHex = "#003D4D"),
            GradientPreset("Sunset Peach", "#2A0500", "#E2543A", middleHex = "#7A1E00"),
            GradientPreset("Ocean Royal", "#020617", "#1E4FA8", middleHex = "#0F2A5C"),
            GradientPreset("Forest Glow", "#041A10", "#2FBF71", middleHex = "#0B4D2C"),
            GradientPreset("Royal Violet", "#0B0214", "#6D28D9", middleHex = "#2A0A4A"),
            GradientPreset("Cherry Punch", "#1A0000", "#B91C1C", middleHex = "#4A000D")
        )
    }

    // ── 17 Pre-Designed Story Templates (Categorized) ────────────────────────
    val templates = remember {
        listOf(
            // ── WAKE ──
            StoryTemplate(
                id = "5am_club",
                name = "5:00 AM Club",
                icon = "🌅",
                sticker = StorySticker("🌅 5:00 AM CLUB", "#00FF94", "Wake Up"),
                gradient = GradientPreset("Obsidian Cyber", "#050811", "#120924"),
                shape = StoryBadgeShape.CAPSULE,
                highlightMode = TextHighlightMode.DARK_GLASS,
                accentHex = "#00FF94",
                defaultCaption = "RISE & SHINE - UNLOCK YOUR POTENTIAL ⚡",
                category = "Wake"
            ),
            StoryTemplate(
                id = "early_bird",
                name = "Early Bird",
                icon = "🐦",
                sticker = StorySticker("⏰ EARLY BIRD", "#00E5FF", "Wake Up"),
                gradient = GradientPreset("Aurora Teal", "#00131A", "#007A6E", middleHex = "#003D4D"),
                shape = StoryBadgeShape.CLOUD,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#00E5FF",
                defaultCaption = "EARLY BIRD WINS THE DAY 🐦 SUNRISE MODE",
                category = "Wake"
            ),
            StoryTemplate(
                id = "alarm_smashed",
                name = "Alarm Smashed",
                icon = "⚡",
                sticker = StorySticker("⚡ ALARM SMASHED", "#FFD23D", "Wake Up"),
                gradient = GradientPreset("Sunset Peach", "#2A0500", "#E2543A", middleHex = "#7A1E00"),
                shape = StoryBadgeShape.STAR,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FFD23D",
                defaultCaption = "DESTROYED MY ALARM ⚡ ZERO HESITATION",
                category = "Wake"
            ),
            StoryTemplate(
                id = "no_snooze",
                name = "No Snooze",
                icon = "🔔",
                sticker = StorySticker("🔔 NO SNOOZE", "#FF8A3D", "Wake Up"),
                gradient = GradientPreset("Carbon Gold", "#121000", "#332B00"),
                shape = StoryBadgeShape.DOUBLE_FRAME,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FF8A3D",
                defaultCaption = "SLEPT LIKE A CHAMPION 🔔 NO SNOOZE EVER",
                category = "Wake"
            ),

            // ── STREAK ──
            StoryTemplate(
                id = "streak_14",
                name = "14-Day Streak",
                icon = "🔥",
                sticker = StorySticker("⚡ 14-DAY STREAK", "#FFD23D", "Streaks"),
                gradient = GradientPreset("Dark Sunset Flame", "#1A0500", "#3D0F00"),
                shape = StoryBadgeShape.DOUBLE_FRAME,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FFD23D",
                defaultCaption = "UNSTOPPABLE MOMENTUM 🔥 14 DAYS STRONG",
                category = "Streak"
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
                defaultCaption = "NEW PERSONAL RECORD SET 🏆 LEVEL UP!",
                category = "Streak"
            ),
            StoryTemplate(
                id = "century",
                name = "Century 100",
                icon = "💯",
                sticker = StorySticker("💯 100% STREAK", "#00E5FF", "Streaks"),
                gradient = GradientPreset("Ocean Royal", "#020617", "#1E4FA8", middleHex = "#0F2A5C"),
                shape = StoryBadgeShape.ROUNDED,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#00E5FF",
                defaultCaption = "CENTURY CLUB 💯 100 DAYS UNBROKEN",
                category = "Streak"
            ),

            // ── HABIT ──
            StoryTemplate(
                id = "routine",
                name = "Morning Routine",
                icon = "💪",
                sticker = StorySticker("💪 HABIT DONE", "#00E5FF", "Habits"),
                gradient = GradientPreset("Deep Space Cyan", "#00141D", "#002B3D"),
                shape = StoryBadgeShape.CYBER_TAG,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#00E5FF",
                defaultCaption = "MORNING ROUTINE SMASHED 💪 READY TO CONQUER",
                category = "Habit"
            ),
            StoryTemplate(
                id = "hydrated",
                name = "Hydration Goal",
                icon = "💧",
                sticker = StorySticker("💧 HYDRATED GOAL", "#00E5FF", "Habits"),
                gradient = GradientPreset("Ocean Royal", "#020617", "#1E4FA8", middleHex = "#0F2A5C"),
                shape = StoryBadgeShape.ROUNDED,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#00E5FF",
                defaultCaption = "8 GLASSES DONE 💧 STAY HYDRATED, STAY SHARP",
                category = "Habit"
            ),
            StoryTemplate(
                id = "workout",
                name = "Beast Mode",
                icon = "🏋️",
                sticker = StorySticker("🏋️ GYM CRUSHED", "#2FBF71", "Habits"),
                gradient = GradientPreset("Forest Glow", "#041A10", "#2FBF71", middleHex = "#0B4D2C"),
                shape = StoryBadgeShape.POLYGON,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#2FBF71",
                defaultCaption = "BEAST MODE ACTIVATED 🏋️ 100% PUSH TODAY",
                category = "Habit"
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
                defaultCaption = "IN THE ZONE 🧠 100% FOCUS MODE",
                category = "Habit"
            ),
            StoryTemplate(
                id = "reading",
                name = "Book Worm",
                icon = "📚",
                sticker = StorySticker("📚 BOOK DONE", "#FFD23D", "Habits"),
                gradient = GradientPreset("Carbon Gold", "#121000", "#332B00"),
                shape = StoryBadgeShape.BANNER,
                highlightMode = TextHighlightMode.CLEAN,
                accentHex = "#FFD23D",
                defaultCaption = "CHAPTER COMPLETED 📚 KNOWLEDGE LEVEL UP",
                category = "Habit"
            ),

            // ── MOTIVATION ──
            StoryTemplate(
                id = "meditation",
                name = "Zen Calm",
                icon = "🧘",
                sticker = StorySticker("🧘 CALM MIND", "#A855F7", "Weekend"),
                gradient = GradientPreset("Royal Violet", "#0B0214", "#6D28D9", middleHex = "#2A0A4A"),
                shape = StoryBadgeShape.CLOUD,
                highlightMode = TextHighlightMode.DARK_GLASS,
                accentHex = "#A855F7",
                defaultCaption = "BREATHE IN. BREATHE OUT 🧘 PEACE OVERLOAD",
                category = "Motivation"
            ),
            StoryTemplate(
                id = "gratitude",
                name = "Gratitude Mode",
                icon = "🙏",
                sticker = StorySticker("🙏 GRATEFUL", "#00E5FF", "Motivation"),
                gradient = GradientPreset("Aurora Teal", "#00131A", "#007A6E", middleHex = "#003D4D"),
                shape = StoryBadgeShape.SPEECH_BUBBLE,
                highlightMode = TextHighlightMode.CLEAN,
                accentHex = "#00FF94",
                defaultCaption = "THANKFUL FOR TODAY 🙏 GROWING EVERY DAY",
                category = "Motivation"
            ),
            StoryTemplate(
                id = "nightowl",
                name = "Night Owl",
                icon = "🦉",
                sticker = StorySticker("🦉 NIGHT OWL", "#B388FF", "Weekend"),
                gradient = GradientPreset("Midnight Purple", "#11001C", "#2D0036"),
                shape = StoryBadgeShape.STAR,
                highlightMode = TextHighlightMode.NEON_OUTLINE,
                accentHex = "#B388FF",
                defaultCaption = "PRODUCTIVE AFTER DARK 🦉 NO SLEEP TIL DONE",
                category = "Motivation"
            ),

            // ── WEEKEND ──
            StoryTemplate(
                id = "weekend",
                name = "Weekend Vibes",
                icon = "🎉",
                sticker = StorySticker("🎉 WEEKEND MODE", "#FF8A3D", "Weekend"),
                gradient = GradientPreset("Sunset Peach", "#2A0500", "#E2543A", middleHex = "#7A1E00"),
                shape = StoryBadgeShape.DOUBLE_FRAME,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FF8A3D",
                defaultCaption = "WEEKEND ENERGY LOADED 🎉 LET'S GO!",
                category = "Weekend"
            ),
            StoryTemplate(
                id = "victory",
                name = "Victory Dance",
                icon = "💃",
                sticker = StorySticker("💃 VICTORY LAP", "#FF8A3D", "Motivation"),
                gradient = GradientPreset("Cherry Punch", "#1A0000", "#B91C1C", middleHex = "#4A000D"),
                shape = StoryBadgeShape.HEART,
                highlightMode = TextHighlightMode.ACCENT_SOLID,
                accentHex = "#FF3D71",
                defaultCaption = "MISSION COMPLETE 💃 VICTORY DANCE TIME",
                category = "Weekend"
            )
        )
    }

    // ── Template Filter Categories ──────────────────────────────────────────
    val templateCategories = listOf("All", "Wake", "Streak", "Habit", "Motivation", "Weekend")

    // ── Editor State Variables ──────────────────────────────────────────────
    var captionText by remember { mutableStateOf("") }
    var selectedSticker by remember { mutableStateOf(stickers[0]) }
    var selectedGradient by remember { mutableStateOf(gradients[0]) }
    var selectedFont by remember { mutableStateOf(fontStyles[0]) }
    var selectedShape by remember { mutableStateOf(StoryBadgeShape.CAPSULE) }
    var textHighlight by remember { mutableStateOf(TextHighlightMode.CLEAN) }
    var textAlign by remember { mutableStateOf(TextAlign.Center) }
    var activeTab by remember { mutableStateOf(StoryEditorTab.TEXT) }
    val editorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStickerCategory by remember { mutableStateOf("Wake Up") }
    var selectedTemplateCategory by remember { mutableStateOf("All") }
    var captionFontSize by remember { mutableStateOf(26f) }
    var isPosting by remember { mutableStateOf(false) }
    var selectedAccentColorHex by remember { mutableStateOf("#00FF94") }

    // ── Photo Background State ────────────────────────────────────────
    var photoBackgroundBytes by remember { mutableStateOf<ByteArray?>(null) }
    var photoOverlayAlpha by remember { mutableStateOf(0.45f) }
    var usePhotoBackground by remember { mutableStateOf(true) }
    var selectedPhotoFilter by remember { mutableStateOf(StoryPhotoFilter.NONE) }
    var photoBrightness by remember { mutableStateOf(0f) }   // -0.5f..0.5f add to RGB
    var photoContrast by remember { mutableStateOf(1f) }     // 0.5f..1.8f scale from gray mid
    var photoVignette by remember { mutableStateOf(0f) }     // 0f..0.9f edge darkening
    var photoBlur by remember { mutableStateOf(0.dp) }       // 0..24.dp gaussian blur

    // ── Drag-to-Reposition State (canvas elements) ───────────────
    var badgeDragOffset by remember { mutableStateOf(Offset.Zero) }
    var badgeScale by remember { mutableStateOf(1f) }          // 0.5x..2.5x pinch zoom
    var badgeRotation by remember { mutableStateOf(0f) }       // degrees, free rotation
    var captionDragOffset by remember { mutableStateOf(Offset.Zero) }
    var captionScale by remember { mutableStateOf(1f) }        // 0.5x..2.5x pinch zoom
    var captionRotation by remember { mutableStateOf(0f) }     // degrees, free rotation

    val photoBitmap = remember(photoBackgroundBytes) {
        photoBackgroundBytes?.decodeToImageBitmap()
    }

    val launchPhotoPicker = rememberStoryPhotoPickerLauncher { bytes ->
        if (bytes != null) {
            photoBackgroundBytes = bytes
            usePhotoBackground = true
        }
    }

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

    // Combined photo ColorFilter: preset matrix + brightness + contrast
    val photoColorFilter: ColorFilter? = remember(
        selectedPhotoFilter, photoBrightness, photoContrast
    ) {
        val presetMatrix = selectedPhotoFilter.matrix
        val hasAdjustments = photoBrightness != 0f || photoContrast != 1f
        if (presetMatrix == null && !hasAdjustments) return@remember null

        fun scaleRows(m: ColorMatrix, rgbScale: Float, offset: Float) = ColorMatrix(floatArrayOf(
            m[0, 0] * rgbScale, m[0, 1] * rgbScale, m[0, 2] * rgbScale, m[0, 3], m[0, 4] + offset,
            m[1, 0] * rgbScale, m[1, 1] * rgbScale, m[1, 2] * rgbScale, m[1, 3], m[1, 4] + offset,
            m[2, 0] * rgbScale, m[2, 1] * rgbScale, m[2, 2] * rgbScale, m[2, 3], m[2, 4] + offset,
            m[3, 0], m[3, 1], m[3, 2], m[3, 3], m[3, 4]
        ))

        // Start from identity or the preset
        val base = presetMatrix ?: ColorMatrix()

        // Contrast: scale around mid-gray (0.5). Brightness: additive offset.
        val contrastMatrix = scaleRows(base, photoContrast, (1f - photoContrast) * 0.5f)
        val brightnessMatrix = scaleRows(contrastMatrix, 1f, photoBrightness)

        ColorFilter.colorMatrix(brightnessMatrix)
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
            StoryBadgeShape.HEART -> HeartBadgeShape
            StoryBadgeShape.STAR -> StarBadgeShape
            StoryBadgeShape.RIBBON -> CutCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
            StoryBadgeShape.CLOUD -> CloudBadgeShape
            StoryBadgeShape.DIAMOND -> DiamondBadgeShape
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
            // ── Photo Background Layer (under gradient overlay) ────────────
            if (photoBitmap != null && usePhotoBackground) {
                Image(
                    bitmap = photoBitmap,
                    contentDescription = "Story photo background",
                    modifier = Modifier
                        .fillMaxSize()
                        // Scale up slightly so gaussian blur doesn't reveal soft edges
                        .scale(1f + (photoBlur.value / 90f))
                        .blur(photoBlur),
                    contentScale = ContentScale.Crop,
                    colorFilter = photoColorFilter
                )
                // Gradient scrim so text stays readable over the photo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    parseColor(selectedGradient.startHex).copy(alpha = photoOverlayAlpha),
                                    parseColor(selectedGradient.endHex).copy(alpha = photoOverlayAlpha)
                                )
                            )
                        )
                )
                // Vignette: radial darkening toward the edges for a cinematic look
                if (photoVignette > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0f),
                                        Color.Black.copy(alpha = 0f),
                                        Color.Black.copy(alpha = photoVignette)
                                    ),
                                    radius = 900f
                                )
                            )
                    )
                }
            }

            // Decorative Glow Orbs (Modern Depth Effect)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(170.dp)
                    .offset(x = 70.dp, y = (-50).dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(150.dp)
                    .offset(x = (-60).dp, y = 55.dp)
                    .clip(CircleShape)
                    .background(stickerColor.copy(alpha = 0.12f))
            )

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

            // Canvas Center Content — Badge, draggable + pinch-zoom/rotate
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(badgeDragOffset.x.roundToInt(), badgeDragOffset.y.roundToInt()) }
                    .graphicsLayer {
                        scaleX = badgeScale
                        scaleY = badgeScale
                        rotationZ = badgeRotation
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            badgeDragOffset += pan
                            badgeScale = (badgeScale * zoom).coerceIn(0.5f, 2.5f)
                            badgeRotation += rotation
                        }
                    }
            ) {
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
            }

            // Canvas Center Content — Caption, draggable + tap-to-edit + pinch-zoom/rotate
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(captionDragOffset.x.roundToInt(), captionDragOffset.y.roundToInt()) }
                    .graphicsLayer {
                        scaleX = captionScale
                        scaleY = captionScale
                        rotationZ = captionRotation
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            captionDragOffset += pan
                            captionScale = (captionScale * zoom).coerceIn(0.5f, 2.5f)
                            captionRotation += rotation
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { activeTab = StoryEditorTab.TEXT })
                    }
            ) {
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
                    modifier = textBgModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = textToDisplay,
                        color = textColor,
                        fontSize = captionFontSize.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = selectedFont.family,
                        textAlign = textAlign,
                        lineHeight = (captionFontSize + 6).sp
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
                    StoryEditorTab.PHOTO,
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

        // ── 5. Real Material 3 Modal Bottom Sheet ────────────────────────────
        if (activeTab != StoryEditorTab.NONE) {
            ModalBottomSheet(
                onDismissRequest = { activeTab = StoryEditorTab.NONE },
                sheetState = editorSheetState,
                containerColor = Color(0xFF0C101D),
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Drawer Filter Chips Bar
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(
                            StoryEditorTab.TEMPLATES,
                            StoryEditorTab.PHOTO,
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
                            StoryEditorTab.PHOTO -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PHOTO BACKGROUND",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )

                                        // One-tap reset: filter, brightness, contrast, overlay
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    selectedPhotoFilter = StoryPhotoFilter.NONE
                                                    photoBrightness = 0f
                                                    photoContrast = 1f
                                                    photoOverlayAlpha = 0.45f
                                                    photoVignette = 0f
                                                    photoBlur = 0.dp
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(text = "🔄", fontSize = 11.sp)
                                            Text(
                                                text = "Reset",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interFamily
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Pick from Gallery
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(accentColor.copy(alpha = 0.18f))
                                                .border(1.5.dp, accentColor, RoundedCornerShape(14.dp))
                                                .clickable { launchPhotoPicker() }
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = "📷", fontSize = 14.sp)
                                                Text(
                                                    text = "Pick Photo",
                                                    color = accentColor,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interFamily
                                                )
                                            }
                                        }

                                        // Remove Photo (only when one is set)
                                        if (photoBackgroundBytes != null) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Color.White.copy(alpha = 0.08f))
                                                    .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        photoBackgroundBytes = null
                                                        usePhotoBackground = false
                                                    }
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = "🗑️", fontSize = 14.sp)
                                                    Text(
                                                        text = "Remove",
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = interFamily
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    val currentPhotoBitmap = photoBitmap
                                    if (currentPhotoBitmap != null) {
                                        // Live thumbnail: current photo with filters applied
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .border(1.5.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                            ) {
                                                Image(
                                                    bitmap = currentPhotoBitmap,
                                                    contentDescription = "Selected photo thumbnail",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop,
                                                    colorFilter = photoColorFilter
                                                )
                                                if (photoVignette > 0f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.radialGradient(
                                                                    colors = listOf(
                                                                        Color.Black.copy(alpha = 0f),
                                                                        Color.Black.copy(alpha = 0f),
                                                                        Color.Black.copy(alpha = photoVignette)
                                                                    ),
                                                                    radius = 200f
                                                                )
                                                            )
                                                    )
                                                }
                                                if (!usePhotoBackground) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.6f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = "🚫", fontSize = 18.sp)
                                                    }
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Current photo",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = interFamily
                                                )
                                                Text(
                                                    text = "${selectedPhotoFilter.displayName} • Bright ${(photoBrightness * 100).toInt()}% • Contrast ${(photoContrast * 100).toInt()}%" +
                                                        if (photoBlur > 0.dp) " • Blur ${photoBlur.value.toInt()}dp" else "",
                                                    color = Color.White.copy(alpha = 0.55f),
                                                    fontSize = 10.sp,
                                                    fontFamily = interFamily
                                                )
                                            }
                                        }

                                        // Toggle photo on/off without losing the pick
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Show photo background",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 12.sp,
                                                fontFamily = interFamily
                                            )
                                            Switch(
                                                checked = usePhotoBackground,
                                                onCheckedChange = { usePhotoBackground = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color.Black,
                                                    checkedTrackColor = accentColor,
                                                    uncheckedThumbColor = Color.White,
                                                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                                )
                                            )
                                        }

                                        // Overlay darkness slider
                                        Text(
                                            text = "OVERLAY DARKNESS • ${(photoOverlayAlpha * 100).toInt()}%",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        Slider(
                                            value = photoOverlayAlpha,
                                            onValueChange = { photoOverlayAlpha = it },
                                            valueRange = 0.1f..0.9f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = accentColor,
                                                activeTrackColor = accentColor,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                            )
                                        )

                                        Text(
                                            text = "Tip: the gradient tint keeps your caption readable on any photo ✨",
                                            color = Color.White.copy(alpha = 0.45f),
                                            fontSize = 10.sp,
                                            fontFamily = interFamily
                                        )
                                    }

                                    // ── Photo Filters (only when a photo is set) ──
                                    if (photoBackgroundBytes != null) {
                                        Text(
                                            text = "FILTERS",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(StoryPhotoFilter.values().toList()) { filter ->
                                                val isSelected = selectedPhotoFilter == filter
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(46.dp)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    listOf(
                                                                        parseColor(selectedGradient.startHex),
                                                                        parseColor(selectedGradient.endHex)
                                                                    )
                                                                )
                                                            )
                                                            .border(
                                                                if (isSelected) 2.5.dp else 1.dp,
                                                                if (isSelected) accentColor else Color.White.copy(alpha = 0.25f),
                                                                RoundedCornerShape(12.dp)
                                                            )
                                                            .clickable { selectedPhotoFilter = filter },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = filter.icon, fontSize = 18.sp)
                                                    }
                                                    Text(
                                                        text = filter.displayName,
                                                        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.6f),
                                                        fontSize = 9.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontFamily = interFamily
                                                    )
                                                }
                                            }
                                        }

                                        // ── Brightness & Contrast ──
                                        Text(
                                            text = "BRIGHTNESS • ${if (photoBrightness >= 0) "+" else ""}${(photoBrightness * 100).toInt()}%",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        Slider(
                                            value = photoBrightness,
                                            onValueChange = { photoBrightness = it },
                                            valueRange = -0.5f..0.5f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = accentColor,
                                                activeTrackColor = accentColor,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                            )
                                        )

                                        Text(
                                            text = "CONTRAST • ${(photoContrast * 100).toInt()}%",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        Slider(
                                            value = photoContrast,
                                            onValueChange = { photoContrast = it },
                                            valueRange = 0.5f..1.8f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = accentColor,
                                                activeTrackColor = accentColor,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                            )
                                        )

                                        Text(
                                            text = "VIGNETTE • ${(photoVignette * 100).toInt()}%",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        Slider(
                                            value = photoVignette,
                                            onValueChange = { photoVignette = it },
                                            valueRange = 0f..0.9f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = accentColor,
                                                activeTrackColor = accentColor,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                            )
                                        )

                                        Text(
                                            text = "BLUR • ${photoBlur.value.toInt()}dp",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        Slider(
                                            value = photoBlur.value,
                                            onValueChange = { photoBlur = it.dp },
                                            valueRange = 0f..24f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = accentColor,
                                                activeTrackColor = accentColor,
                                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                            )
                                        )
                                    }
                                }
                            }

                            StoryEditorTab.TEMPLATES -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "1-TAP STORY TEMPLATES (17 STYLES)",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = interFamily
                                        )
                                        // Bonus: Surprise Me — random template shuffle
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(accentColor.copy(alpha = 0.18f))
                                                .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    applyTemplate(templates.random())
                                                    activeTab = StoryEditorTab.NONE
                                                }
                                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(text = "🎲", fontSize = 11.sp)
                                            Text(
                                                text = "Surprise Me",
                                                color = accentColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = interFamily
                                            )
                                        }
                                    }

                                    // Category Filter Chips
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(templateCategories) { cat ->
                                            val isSelected = selectedTemplateCategory == cat
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) accentColor else Color.White.copy(alpha = 0.08f))
                                                    .clickable { selectedTemplateCategory = cat }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

                                    // Template Cards with Live Gradient Preview
                                    val filteredTemplates = remember(selectedTemplateCategory) {
                                        if (selectedTemplateCategory == "All") templates
                                        else templates.filter { it.category == selectedTemplateCategory }
                                    }
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(filteredTemplates) { template ->
                                            val tAccent = parseColor(template.accentHex)
                                            val tBrush = Brush.verticalGradient(
                                                listOf(
                                                    parseColor(template.gradient.startHex),
                                                    parseColor(template.gradient.endHex)
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(120.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(tBrush)
                                                    .border(1.5.dp, tAccent, RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        applyTemplate(template)
                                                        activeTab = StoryEditorTab.NONE
                                                    }
                                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.Black.copy(alpha = 0.45f))
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = template.sticker.label.take(14),
                                                            color = tAccent,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = interFamily
                                                        )
                                                    }
                                                    Text(text = template.icon, fontSize = 22.sp)
                                                    Text(
                                                        text = template.name,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = interFamily,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
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
                                        text = "TEXT SIZE • ${captionFontSize.toInt()}sp",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    Slider(
                                        value = captionFontSize,
                                        onValueChange = { captionFontSize = it },
                                        valueRange = 18f..44f,
                                        steps = 0,
                                        colors = SliderDefaults.colors(
                                            thumbColor = accentColor,
                                            activeTrackColor = accentColor,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                        )
                                    )

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
                                        val categories = listOf("Wake Up", "Streaks", "Habits", "Fails", "Motivation", "Weekend")
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
                                        text = "SELECT CONTAINER SHAPE (13 STYLES)",
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





