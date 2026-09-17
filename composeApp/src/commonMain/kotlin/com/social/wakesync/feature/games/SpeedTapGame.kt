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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

// Tracks the previous grid layout so consecutive rounds never repeat.
private var lastTapLayout: List<Int> = emptyList()

@Composable
fun SpeedTapGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    round: Int = 0
) {
    var nextExpected by remember(round) { mutableIntStateOf(1) }
    // Fresh grid layout each round, never repeating the previous arrangement.
    val numbers = remember(round) {
        var layout: List<Int>
        do {
            layout = (1..6).shuffled()
        } while (layout == lastTapLayout)
        lastTapLayout = layout
        layout
    }
    var isError by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = if (isError) "❌ Wrong sequence! Tap #$nextExpected next!" else "Tap numbers ascending: #$nextExpected ➔ 6",
            color = if (isError) AppColorPalette.LossRed else AppColorPalette.CyanCta,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFamily
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(240.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(numbers.size) { idx ->
                val num = numbers[idx]
                val isCleared = num < nextExpected
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.92f else 1.0f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCleared) AppColorPalette.WinGreen.copy(alpha = 0.25f) else AppColorPalette.Surface)
                        .border(1.5.dp, if (isCleared) AppColorPalette.WinGreen else Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isCleared, interactionSource = interactionSource, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (num == nextExpected) {
                                isError = false
                                if (nextExpected == 6) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSuccess()
                                } else nextExpected++
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isError = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCleared) "✓" else num.toString(),
                        color = if (isCleared) AppColorPalette.WinGreen else Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily
                    )
                }
            }
        }
    }
}
