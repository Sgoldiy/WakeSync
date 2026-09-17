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

// Tracks the previous set so consecutive rounds never repeat.
private var lastOddSet: Pair<List<String>, Int>? = null

@Composable
fun OddOneOutGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    round: Int = 0
) {
    val sets = listOf(
        Pair(listOf("🔥", "🔥", "🔥", "🔥", "💥", "🔥", "🔥", "🔥", "🔥"), 4),
        Pair(listOf("🐱", "🐱", "🐱", "🐶", "🐱", "🐱", "🐱", "🐱", "🐱"), 3),
        Pair(listOf("⭐", "⭐", "⭐", "⭐", "⭐", "🌟", "⭐", "⭐", "⭐"), 5),
        Pair(listOf("😴", "😴", "🥱", "😴", "😴", "😴", "😴", "😴", "😴"), 2)
    )

    // Fresh random set per round, never repeating the immediately previous set.
    val currentSet = remember(round) {
        val candidates = sets.indices.filter { sets[it] != lastOddSet }.ifEmpty { sets.indices.toList() }
        val chosen = candidates.random()
        lastOddSet = sets[chosen]
        sets[chosen]
    }
    var isError by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = if (isError) "❌ Try again! Find intruder emoji!" else "🔍 Find the intruder emoji!",
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
            items(currentSet.first.size) { idx ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppColorPalette.Surface)
                        .border(1.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                        .clickable(interactionSource = interactionSource, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (idx == currentSet.second) {
                                isError = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSuccess()
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isError = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentSet.first[idx], fontSize = 34.sp)
                }
            }
        }
    }
}
