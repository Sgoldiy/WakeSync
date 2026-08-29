package com.social.wakesync.feature.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun AlarmLockScreen(
    alarmTime: String,           // e.g. "6:30 AM"
    alarmName: String,           // e.g. "Main Grind"
    mode: String,                // "Solo", "Duo", or "Group"
    challengeName: String,       // e.g. "Math", "Memory"
    groupAvatars: List<String>,  // emoji avatars — empty list hides the group section
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onWake: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Live clock
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val rawHour = now.hour
            val hour = if (rawHour == 0) 12 else if (rawHour > 12) rawHour - 12 else rawHour
            val min = now.minute.toString().padStart(2, '0')
            val amPm = if (now.hour < 12) "AM" else "PM"
            timeString = "$hour:$min"

            val dayName = when (now.dayOfWeek.name) {
                "MONDAY" -> "Monday"
                "TUESDAY" -> "Tuesday"
                "WEDNESDAY" -> "Wednesday"
                "THURSDAY" -> "Thursday"
                "FRIDAY" -> "Friday"
                "SATURDAY" -> "Saturday"
                else -> "Sunday"
            }
            val monthName = when (now.monthNumber) {
                1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
                5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
                9 -> "September"; 10 -> "October"; 11 -> "November"
                else -> "December"
            }
            dateString = "$dayName, $monthName ${now.dayOfMonth}"
            delay(1000)
        }
    }

    // Pulsing animation for the wake button
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val isSolo = mode == "Solo"
    val modeLabel = when (mode) {
        "Group" -> "Group Mode"
        "Duo"   -> "Duo Mode"
        else    -> "Solo Mode"
    }

    val friendsCount = groupAvatars.size
    val avatarBorderColors = listOf(
        AppColorPalette.WinGreen,
        AppColorPalette.StreakFireStart,
        AppColorPalette.GoldPremium,
        AppColorPalette.LossRed,
        AppColorPalette.CyanCta
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
    ) {
        // Subtle dark vignette radial overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // ── Large Clock ──────────────────────────────────────────────────────
            Text(
                text = timeString.ifEmpty { alarmTime.substringBefore(" ") },
                color = Color.White,
                fontSize = 88.sp,
                fontWeight = FontWeight.W300,
                fontFamily = titleFamily,
                letterSpacing = (-2).sp
            )

            // Date
            Text(
                text = dateString.ifEmpty { "Thursday, May 1" },
                color = AppColorPalette.CyanCta,
                fontSize = 16.sp,
                fontWeight = FontWeight.W500,
                fontFamily = interFamily
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Notification Card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1228))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFFF3D71).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                // Left red accent bar
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(130.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = 1.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF3D71),
                                    Color(0xFFFF8A3D)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)
                ) {
                    // App badge row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🚨", fontSize = 13.sp)
                        Text(
                            text = "WakeSync · Alarm",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 13.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W600
                        )
                    }

                    // Alarm sub-name / mode
                    Text(
                        text = "$alarmName · $modeLabel",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp,
                        fontFamily = interFamily,
                        fontWeight = FontWeight.W400
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Big alarm time
                    Text(
                        text = alarmTime,
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = titleFamily,
                        letterSpacing = (-1).sp
                    )

                    // Motivational message
                    Text(
                        text = "Wake up. Or get left behind. 💀",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        fontFamily = interFamily,
                        fontWeight = FontWeight.W400
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action pills row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Mode pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(AppColorPalette.WinGreen.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    AppColorPalette.WinGreen.copy(alpha = 0.5f),
                                    RoundedCornerShape(99.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = "⚡", fontSize = 12.sp)
                                Text(
                                    text = if (isSolo) "Solo Alarm" else "${mode} Alarm",
                                    color = AppColorPalette.WinGreen,
                                    fontSize = 12.sp,
                                    fontFamily = interFamily,
                                    fontWeight = FontWeight.W700
                                )
                            }
                        }

                        // Challenge pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(AppColorPalette.GoldPremium.copy(alpha = 0.07f))
                                .border(
                                    1.dp,
                                    AppColorPalette.GoldPremium.copy(alpha = 0.45f),
                                    RoundedCornerShape(99.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = "🧩", fontSize = 12.sp)
                                Text(
                                    text = "$challengeName Challenge",
                                    color = AppColorPalette.GoldPremium,
                                    fontSize = 12.sp,
                                    fontFamily = interFamily,
                                    fontWeight = FontWeight.W700
                                )
                            }
                        }
                    }
                }
            }

            // ── Group Friends Section (hidden for Solo) ──────────────────────────
            if (!isSolo && groupAvatars.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColorPalette.Surface)
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.06f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$friendsCount friends in this alarm",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W500
                        )

                        // Avatar circles row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-8).dp)
                        ) {
                            groupAvatars.forEachIndexed { index, avatar ->
                                val borderColor = avatarBorderColors[index % avatarBorderColors.size]
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AppColorPalette.DeepSurface)
                                        .border(2.dp, borderColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = avatar, fontSize = 20.sp)
                                }
                                // Small colored status dot below each avatar
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(borderColor)
                                        .align(Alignment.BottomCenter)
                                        .offset(y = 44.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Swipe to Wake Up bar ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(AppColorPalette.Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(99.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onWake() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Pulsing glowing wake circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppColorPalette.StreakFireStart,
                                        Color(0xFFFF3D71)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text content
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Swipe to wake up",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W700,
                            fontFamily = interFamily
                        )
                        Text(
                            text = "No snooze. No dismiss. Just win.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W400
                        )
                    }

                    // Arrow indicator
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
