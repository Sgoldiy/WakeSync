package com.social.wakesync.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

val DefaultGenZSoundCatalog = listOf(
    SoundMetadata("phonk_1", "Phonk Overdrive 808", "https://actions.google.com/sounds/v1/science_fiction/scifi_alarm.ogg", "Phonk", 15, "🏎️", 1.0f, 1.0f),
    SoundMetadata("phonk_2", "Tokyo Night Bass Drift", "https://actions.google.com/sounds/v1/alarms/digital_watch_alarm.ogg", "Phonk", 12, "🔊", 1.0f, 1.0f),
    SoundMetadata("hyper_1", "Hyper-Glitch Siren 2099", "https://actions.google.com/sounds/v1/alarms/beeping_disaster_alarm.ogg", "HyperPop", 10, "⚡", 1.0f, 1.0f),
    SoundMetadata("hyper_2", "Neon Pulse Energy", "https://actions.google.com/sounds/v1/emergency/emergency_siren_short.ogg", "HyperPop", 14, "✨", 1.0f, 1.0f),
    SoundMetadata("chip_1", "Retro Boss Fight Arcade", "https://actions.google.com/sounds/v1/weapons/retro_laser_shot.ogg", "8Bit", 12, "👾", 1.0f, 1.0f),
    SoundMetadata("chip_2", "Pixel Arcade Surge", "https://actions.google.com/sounds/v1/alarms/spaceship_alarm.ogg", "8Bit", 15, "🕹️", 1.0f, 1.0f),
    SoundMetadata("meme_1", "Emotional Damage 📢", "https://actions.google.com/sounds/v1/cartoon/boing.ogg", "Meme", 8, "🤡", 1.0f, 1.0f),
    SoundMetadata("meme_2", "Brukuh Alert 💀", "https://actions.google.com/sounds/v1/cartoon/clang_and_drip.ogg", "Meme", 6, "💀", 1.0f, 1.0f),
    SoundMetadata("meme_3", "Final Boss Alarm 🚨", "https://actions.google.com/sounds/v1/alarms/bugle_tune.ogg", "Meme", 18, "🚨", 1.0f, 1.0f)
)

@Composable
fun CyberAudioLibraryModal(
    onDismiss: () -> Unit,
    onSelectSound: (SoundMetadata) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    initialSound: SoundMetadata? = null
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var currentSelectedSound by remember { mutableStateOf(initialSound ?: DefaultGenZSoundCatalog.first()) }
    var pitch by remember { mutableFloatStateOf(currentSelectedSound.pitch) }
    var speed by remember { mutableFloatStateOf(currentSelectedSound.speed) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    val soundPlayer = remember {
        try {
            getSoundPlayer()
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer?.stopPreview()
        }
    }

    val categories = listOf("ALL", "🏎️ PHONK", "⚡ HYPERPOP", "👾 8-BIT", "🤡 MEME")

    val filteredCatalog = remember(selectedCategory) {
        if (selectedCategory == "ALL") {
            DefaultGenZSoundCatalog
        } else {
            val key = selectedCategory.split(" ").last()
            DefaultGenZSoundCatalog.filter { it.category.equals(key, ignoreCase = true) || selectedCategory.contains(it.category, ignoreCase = true) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable {
                soundPlayer?.stopPreview()
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121420)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(AppColorPalette.CyanCta, Color(0xFFFF007A))))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "🔊", fontSize = 20.sp)
                            Text(
                                text = "CYBER AUDIO LIBRARY",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFamily
                            )
                        }
                        Text(
                            text = "Gen Z Soundscapes & Audio Pitch Shifter",
                            color = AppColorPalette.CyanCta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFamily
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable {
                                soundPlayer?.stopPreview()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Filter Pills
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Track Catalog List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCatalog) { item ->
                        val isSelected = currentSelectedSound.id == item.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF1E2538) else Color.White.copy(alpha = 0.03f))
                                .border(1.dp, if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                                .clickable {
                                    currentSelectedSound = item.copy(pitch = pitch, speed = speed)
                                    if (isPlayingPreview) {
                                        soundPlayer?.playPreview(item.url, pitch, speed)
                                    }
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = item.emoji, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = item.category,
                                            color = AppColorPalette.CyanCta,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = interFamily
                                        )
                                        Text(
                                            text = "· ${item.durationSeconds}s",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 10.sp,
                                            fontFamily = interFamily
                                        )
                                    }
                                }
                            }

                            // Play / Pause Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected && isPlayingPreview) Color(0xFFFF007A) else Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        if (isSelected && isPlayingPreview) {
                                            soundPlayer?.stopPreview()
                                            isPlayingPreview = false
                                        } else {
                                            currentSelectedSound = item.copy(pitch = pitch, speed = speed)
                                            soundPlayer?.playPreview(item.url, pitch, speed)
                                            isPlayingPreview = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSelected && isPlayingPreview) "⏸️" else "▶️",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pitch & Speed Audio Controls Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "🎛️ AUDIO ENGINE CONTROLS",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pitch Shift",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFamily
                        )
                        Text(
                            text = "${(pitch * 100).toInt() / 100.0}x",
                            color = AppColorPalette.CyanCta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFamily
                        )
                    }
                    Slider(
                        value = pitch,
                        onValueChange = {
                            pitch = it
                            soundPlayer?.updatePlaybackParams(pitch, speed)
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AppColorPalette.CyanCta,
                            activeTrackColor = AppColorPalette.CyanCta
                        )
                    )

                    // Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speed / Tempo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFamily
                        )
                        Text(
                            text = "${(speed * 100).toInt() / 100.0}x",
                            color = Color(0xFFFF007A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = interFamily
                        )
                    }
                    Slider(
                        value = speed,
                        onValueChange = {
                            speed = it
                            soundPlayer?.updatePlaybackParams(pitch, speed)
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF007A),
                            activeTrackColor = Color(0xFFFF007A)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickable {
                                    pitch = 0.85f
                                    speed = 0.85f
                                    soundPlayer?.updatePlaybackParams(pitch, speed)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Slowed + Reverb 🌌", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickable {
                                    pitch = 1.35f
                                    speed = 1.35f
                                    soundPlayer?.updatePlaybackParams(pitch, speed)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nightcore Chaos ⚡", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .clickable {
                                    pitch = 1.0f
                                    speed = 1.0f
                                    soundPlayer?.updatePlaybackParams(pitch, speed)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Default 🎯", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply CTA Button
                Button(
                    onClick = {
                        soundPlayer?.stopPreview()
                        val finalSound = currentSelectedSound.copy(pitch = pitch, speed = speed)
                        onSelectSound(finalSound)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColorPalette.CyanCta)
                ) {
                    Text(
                        text = "Apply Sound & Audio Settings 🎧",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }
        }
    }
}
