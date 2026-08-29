package com.social.wakesync.feature.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler
import com.social.wakesync.ui.utils.LongArrowBackIcon

@Composable
fun AlarmsScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    BackHandler { onBack() }

    var showSetAlarmScreen by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    if (showSetAlarmScreen) {
        SetAlarmScreen(
            onBack = { showSetAlarmScreen = false },
            onSave = { hour, minute, isAm, days, mode, challenge, partnerUsername, bondName ->
                viewModel.addAlarm(hour, minute, isAm, days, mode, challenge, partnerUsername, bondName)
            },
            titleFamily = titleFamily,
            interFamily = interFamily,
            sounds = uiState.sounds,
            selectedSound = uiState.selectedSound,
            onSoundSelected = { viewModel.selectSound(it) }
        )
    } else {
        Scaffold(
            containerColor = AppColorPalette.VoidBg,
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 8.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AppColorPalette.CyanCta,
                                    AppColorPalette.CyanCta.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .clickable { showSetAlarmScreen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add Alarm",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Sticky Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColorPalette.VoidBg)
                        .padding(start = 16.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        LongArrowBackIcon(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Alarms",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Sort",
                        color = AppColorPalette.CyanCta,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = interFamily,
                        modifier = Modifier.clickable { viewModel.toggleSortOrder() }
                    )
                }

                if (uiState.alarms.isEmpty()) {
                    val transition = rememberInfiniteTransition(label = "clock_float_transition")
                    val floatingOffset by transition.animateFloat(
                        initialValue = -7f,
                        targetValue = 7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1750, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "clock_floating_offset",
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .offset(y = floatingOffset.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF061325))
                                .border(
                                    width = 1.dp,
                                    color = AppColorPalette.CyanCta.copy(alpha = 0.42f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                AppColorPalette.CyanCta.copy(alpha = 0.16f),
                                                Color.Transparent,
                                            ),
                                        ),
                                    ),
                            )
                            Text(
                                text = "⏰",
                                fontSize = 48.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "No Alarms Set",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            fontFamily = titleFamily,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap the '+' button below to add your first\nalarm. Get ready to wake up.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.W400,
                            fontFamily = interFamily,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.alarms) { alarm ->
                            AlarmListCard(
                                alarm = alarm,
                                onToggle = { isEnabled ->
                                    viewModel.toggleAlarm(alarm.id, isEnabled)
                                },
                                onDelete = {
                                    viewModel.deleteAlarm(alarm.id)
                                },
                                titleFamily = titleFamily,
                                interFamily = interFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmListCard(
    alarm: AlarmData,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val alpha = if (alarm.isEnabled) 1f else 0.35f

    // Format time for display
    val displayTime = remember(alarm.time) {
        try {
            val parts = alarm.time.split(":")
            var h = parts[0].toInt()
            val m = parts[1]
            val ampm = if (h >= 12) "PM" else "AM"
            if (h > 12) h -= 12
            if (h == 0) h = 12
            "$h:$m $ampm"
        } catch (e: Exception) {
            alarm.time
        }
    }

    // Format days for display
    val displayDays = remember(alarm.days) {
        if (alarm.days.size == 7) "Daily"
        else if (alarm.days.isEmpty()) "Once"
        else {
            val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
            alarm.days.sorted().joinToString(" ") { dayNames[it] }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) AppColorPalette.Surface else AppColorPalette.Surface.copy(
                alpha = 0.4f
            )
        ),
        border = BorderStroke(
            1.dp,
            if (alarm.isEnabled) AppColorPalette.CyanCta.copy(alpha = 0.15f)
            else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayTime,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        letterSpacing = (-1.5).sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Delete Button
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete Alarm",
                            tint = AppColorPalette.LossRed.copy(alpha = if (alarm.isEnabled) 0.6f else 0.35f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onDelete() }
                        )

                        // Custom Switch
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (alarm.isEnabled) AppColorPalette.CyanCta else Color.White.copy(
                                        alpha = 0.1f
                                    )
                                )
                                .clickable { onToggle(!alarm.isEnabled) }
                                .padding(3.dp),
                            contentAlignment = if (alarm.isEnabled) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Text(
                    text = "${alarm.label} · $displayDays",
                    color = Color.White.copy(alpha = 0.4f * alpha),
                    fontSize = 12.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlarmTag(
                        text = alarm.mode,
                        icon = Icons.Rounded.FlashOn,
                        iconColor = Color(0xFFFFD600), // Emoji yellow thunder
                        textColor = AppColorPalette.CyanCta,
                        borderColor = AppColorPalette.CyanCta.copy(alpha = 0.25f),
                        isEnabled = alarm.isEnabled,
                        interFamily = interFamily
                    )
                    AlarmTag(
                        text = alarm.challenge,
                        icon = Icons.Rounded.Extension,
                        iconColor = Color.White.copy(alpha = 0.4f),
                        textColor = Color.White.copy(alpha = 0.5f),
                        borderColor = Color.White.copy(alpha = 0.12f),
                        isEnabled = alarm.isEnabled,
                        interFamily = interFamily
                    )
                }

                val timeLeft = remember(alarm.timestamp) {
                    try {
                        val now: Long = getCurrentTimeMillis()
                        val diff: Long = alarm.timestamp - now
                        if (diff > 0L) {
                            val h = diff / 3600000L
                            val m = (diff / 60000L) % 60L
                            "${h}h ${m}m"
                        } else ""
                    } catch (_: Exception) {
                        ""
                    }
                }

                if (alarm.isEnabled && timeLeft.isNotEmpty()) {
                    Text(
                        text = "in $timeLeft",
                        color = Color.White.copy(alpha = 0.15f),
                        fontSize = 11.sp,
                        fontFamily = interFamily,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun AlarmTag(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    textColor: Color,
    borderColor: Color,
    isEnabled: Boolean,
    interFamily: FontFamily,
) {
    val alpha = if (isEnabled) 1f else 0.4f
    Row(
        modifier = Modifier
            .border(1.dp, borderColor.copy(alpha = borderColor.alpha * alpha), CircleShape)
            .background(Color.White.copy(alpha = 0.04f * alpha), CircleShape)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor.copy(alpha = alpha),
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = text,
            color = textColor.copy(alpha = alpha),
            fontSize = 10.sp,
            fontWeight = FontWeight.W800,
            fontFamily = interFamily
        )
    }
}
