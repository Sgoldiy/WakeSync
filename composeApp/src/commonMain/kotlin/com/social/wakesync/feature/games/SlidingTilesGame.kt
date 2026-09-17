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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay

// Tracks the previous starting layout so consecutive rounds never repeat.
private var lastTileLayout: List<Int> = emptyList()

@Composable
fun SlidingTilesGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    round: Int = 0
) {
    // Start from a fresh random shuffle (solvable by swaps) each round — never the same layout twice in a row.
    val currentTiles = remember(round) {
        var start: List<Int>
        do {
            start = listOf(1, 2, 3, 4).shuffled()
        } while (start == listOf(1, 2, 3, 4) || start == lastTileLayout)
        lastTileLayout = start
        mutableStateListOf(start[0], start[1], start[2], start[3])
    }
    var selectedIndex by remember(round) { mutableIntStateOf(-1) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(currentTiles.toList()) {
        if (currentTiles.toList() == listOf(1, 2, 3, 4)) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(150)
            onSuccess()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = if (selectedIndex == -1) "🧱 Tap tile to select, then tap target tile to swap!" else "🔄 Tap target tile to swap into place!",
            color = AppColorPalette.CyanCta,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.size(210.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { idx ->
                val num = currentTiles[idx]
                val isSelected = selectedIndex == idx
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isSelected) 1.05f else if (isPressed) 0.95f else 1.0f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.35f) else AppColorPalette.Surface)
                        .border(2.dp, if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                        .clickable(interactionSource = interactionSource, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (selectedIndex == -1) {
                                selectedIndex = idx
                            } else if (selectedIndex == idx) {
                                selectedIndex = -1
                            } else {
                                val temp = currentTiles[selectedIndex]
                                currentTiles[selectedIndex] = currentTiles[idx]
                                currentTiles[idx] = temp
                                selectedIndex = -1
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(num.toString(), color = if (isSelected) AppColorPalette.CyanCta else Color.White, fontSize = 34.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
                }
            }
        }
    }
}
