package com.social.wakesync.feature.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.delay

// Tracks the previous pattern so consecutive rounds never repeat.
private var lastPattern: List<Int> = emptyList()

@Composable
fun PatternMemoryGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    round: Int = 0
) {
    val gridSize = 9 // 3x3
    var patternVersion by remember(round) { mutableIntStateOf(1) }
    // Fresh 4-tile sequence per round, never identical to the previous one.
    val targetSequence = remember(patternVersion) {
        var seq: List<Int>
        do {
            seq = (0 until gridSize).shuffled().take(4)
        } while (seq == lastPattern)
        lastPattern = seq
        seq
    }
    val userSequence = remember(patternVersion) { mutableStateListOf<Int>() }
    var isShowingPattern by remember { mutableStateOf(true) }
    var activeFlashTile by remember { mutableIntStateOf(-1) }
    var isErrorFlash by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(patternVersion) {
        isShowingPattern = true
        isErrorFlash = false
        userSequence.clear()
        delay(400)
        for (tile in targetSequence) {
            activeFlashTile = tile
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(450)
            activeFlashTile = -1
            delay(200)
        }
        isShowingPattern = false
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = when {
                isErrorFlash -> "❌ Missed sequence! Replaying pattern..."
                isShowingPattern -> "👀 Watch & Memorize 4-Step Pattern..."
                else -> "Tap the 4 tiles in order (${userSequence.size}/4)"
            },
            color = when {
                isErrorFlash -> AppColorPalette.LossRed
                isShowingPattern -> Color.White.copy(alpha = 0.7f)
                else -> AppColorPalette.CyanCta
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFamily
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(240.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(gridSize) { index ->
                val isHighlighted = activeFlashTile == index || userSequence.contains(index)
                val color by animateColorAsState(
                    targetValue = when {
                        isErrorFlash -> AppColorPalette.LossRed.copy(alpha = 0.5f)
                        isHighlighted -> AppColorPalette.CyanCta
                        else -> AppColorPalette.Surface
                    },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                val scale by animateFloatAsState(
                    targetValue = if (isHighlighted) 1.06f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .border(1.5.dp, if (isHighlighted) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isShowingPattern && !isErrorFlash) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            userSequence.add(index)
                            val step = userSequence.size - 1
                            if (userSequence[step] != targetSequence[step]) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isErrorFlash = true
                                patternVersion++
                            } else if (userSequence.size == targetSequence.size) {
                                onSuccess()
                            }
                        }
                )
            }
        }
    }
}
