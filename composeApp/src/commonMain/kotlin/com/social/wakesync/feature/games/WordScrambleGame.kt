package com.social.wakesync.feature.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay

// Tracks the previous word so consecutive rounds never repeat.
private var lastWord: String = ""

@Composable
fun WordScrambleGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    round: Int = 0
) {
    val wordList = listOf("WAKE", "RISE", "FOCUS", "ALERT", "POWER", "SHINE", "SMASH")
    // Re-seeded per round, and never repeats the immediately previous word.
    val originalWord = remember(round) {
        val candidates = wordList.filter { it != lastWord }
        candidates.random().also { lastWord = it }
    }
    // Guarantee the scramble is never accidentally identical to the word.
    val scrambled = remember(originalWord, round) {
        var s = originalWord
        while (s == originalWord && originalWord.toSet().size > 1) {
            s = originalWord.toList().shuffled().joinToString("")
        }
        s
    }
    val selectedIndices = remember(originalWord, round) { mutableStateListOf<Int>() }
    val haptic = LocalHapticFeedback.current

    val currentInput = selectedIndices.map { scrambled[it] }.joinToString("")

    LaunchedEffect(currentInput) {
        if (currentInput.equals(originalWord, ignoreCase = true)) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(150)
            onSuccess()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("🔤 Unscramble the wake-up word:", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scrambled.forEach { char ->
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColorPalette.DeepSurface)
                        .border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(char.toString(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, fontFamily = titleFamily)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AppColorPalette.Surface)
                .border(1.5.dp, AppColorPalette.CyanCta, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentInput.ifEmpty { "Tap letter bank..." },
                color = if (currentInput.isEmpty()) Color.White.copy(alpha = 0.3f) else AppColorPalette.CyanCta,
                fontSize = 24.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily
            )

            if (selectedIndices.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndices.removeAt(selectedIndices.size - 1)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("⌫ Undo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scrambled.forEachIndexed { idx, letter ->
                val isUsed = selectedIndices.contains(idx)
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val keyScale by animateFloatAsState(if (isPressed) 0.9f else 1.0f)

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .scale(keyScale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isUsed) Color.White.copy(alpha = 0.04f) else AppColorPalette.CyanCta.copy(alpha = 0.2f))
                        .border(1.5.dp, if (isUsed) Color.Transparent else AppColorPalette.CyanCta, RoundedCornerShape(14.dp))
                        .clickable(enabled = !isUsed, interactionSource = interactionSource, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndices.add(idx)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        color = if (isUsed) Color.White.copy(alpha = 0.2f) else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = interFamily
                    )
                }
            }
        }
    }
}
