package com.social.wakesync.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Reaction emoji animation item
data class FloatingEmoji(
    val id: Long,
    val emoji: String,
    val startX: Float,
    val duration: Int
)

@Composable
fun StoryViewScreen(
    story: StoryItem,
    onClose: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Story progress tracking: 3 segments mock
    val totalSegments = 3
    var currentSegment by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    // Floating emojis list
    var floatingEmojis by remember { mutableStateOf(listOf<FloatingEmoji>()) }
    var emojiCounter by remember { mutableStateOf(0L) }

    // Story timing logic
    LaunchedEffect(currentSegment, isPaused) {
        if (!isPaused) {
            val durationMs = 4000
            val intervalMs = 16
            val step = intervalMs.toFloat() / durationMs
            
            while (progress < 1f) {
                delay(intervalMs.toLong())
                if (!isPaused) {
                    progress = (progress + step).coerceAtMost(1f)
                }
            }
            
            if (currentSegment < totalSegments - 1) {
                currentSegment++
                progress = 0f
            } else {
                onClose()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050811)) // Dark void background
            .drawBehind {
                // Diagonal stripes matching the mockup background pattern
                val step = 45.dp.toPx()
                val strokeWidth = 3.dp.toPx()
                val color = Color.White.copy(alpha = 0.015f)
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = color,
                        start = Offset(x, 0f),
                        end = Offset(x + size.height, size.height),
                        strokeWidth = strokeWidth
                    )
                    x += step
                }
            }
            .pointerInput(Unit) {
                // Simple tap triggers: tap left to go back, tap right to advance, hold to pause
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 3) {
                            // Go back segment
                            if (currentSegment > 0) {
                                currentSegment--
                                progress = 0f
                            } else {
                                onClose()
                            }
                        } else {
                            // Advance segment
                            if (currentSegment < totalSegments - 1) {
                                currentSegment++
                                progress = 0f
                            } else {
                                onClose()
                            }
                        }
                    }
                )
            }
    ) {
        // Floating Emojis Render Layer
        floatingEmojis.forEach { fe ->
            key(fe.id) {
                FloatingEmojiItem(fe) {
                    floatingEmojis = floatingEmojis.filter { it.id != fe.id }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            // 1. Progress Segments (Top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until totalSegments) {
                    val segmentProgress = when {
                        i < currentSegment -> 1f
                        i == currentSegment -> progress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(CircleShape),
                        color = story.borderColor,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. User Info Header Row (Matches mockup layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.2.dp, story.borderColor, CircleShape)
                            .padding(1.5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = story.avatar, fontSize = 18.sp)
                    }

                    Column {
                        Text(
                            text = story.username,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFamily
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "18 min ago",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontFamily = interFamily
                            )
                            Text(
                                text = " · 🔥 89",
                                color = Color(0xFFFF8A3D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }
                    }
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Center Screen Area (Mockup Trophy with Placeholder "story content")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🏆", fontSize = 48.sp)
                    Text(
                        text = "story content",
                        color = Color.White.copy(alpha = 0.15f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            // 4. Overlaid Bottom Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080B13)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Badge Pills Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // New Record badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFD23D).copy(alpha = 0.06f))
                                .border(1.dp, Color(0xFFFFD23D).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🏆 New Record",
                                color = Color(0xFFFFD23D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }

                        // Streak badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00FF94).copy(alpha = 0.06f))
                                .border(1.dp, Color(0xFF00FF94).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🔥 89",
                                color = Color(0xFF00FF94),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }
                    }

                    // Main Text
                    Text(
                        text = "89 days. Still undefeated.",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )

                    // Subtext
                    Text(
                        text = "First in the group 7 days running 🐺",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontFamily = interFamily
                    )
                }
            }

            // 5. Floating Bottom Reaction Circles Overlay matching the screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("🔥", "⚡", "👑", "💪").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                                .clickable {
                                    // Trigger floating emoji animation
                                    val newEmoji = FloatingEmoji(
                                        id = emojiCounter++,
                                        emoji = emoji,
                                        startX = (300..800)
                                            .random()
                                            .toFloat(),
                                        duration = (1500..2500).random()
                                    )
                                    floatingEmojis = floatingEmojis + newEmoji
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// Float up animation helper Composable
@Composable
fun FloatingEmojiItem(
    floatingEmoji: FloatingEmoji,
    onAnimationEnd: () -> Unit
) {
    val animState = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animState.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = floatingEmoji.duration,
                easing = FastOutSlowInEasing
            )
        )
        onAnimationEnd()
    }

    val progress = animState.value
    val scale = 0.4f + (progress * 0.6f)
    val alpha = 1f - progress
    val yOffset = - (progress * 500)
    
    // Wave drift horizontal
    val xOffset = kotlin.math.sin(progress * 2 * kotlin.math.PI) * 40

    Box(
        modifier = Modifier
            .offset(
                x = floatingEmoji.startX.dp + xOffset.dp,
                y = 800.dp + yOffset.dp
            )
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
    ) {
        Text(text = floatingEmoji.emoji, fontSize = 32.sp)
    }
}
