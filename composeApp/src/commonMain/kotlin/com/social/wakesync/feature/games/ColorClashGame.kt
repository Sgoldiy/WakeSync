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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun ColorClashGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val colors = listOf(
        Pair("RED", AppColorPalette.LossRed),
        Pair("CYAN", AppColorPalette.CyanCta),
        Pair("GREEN", AppColorPalette.WinGreen),
        Pair("GOLD", AppColorPalette.GoldPremium)
    )
    val haptic = LocalHapticFeedback.current

    var round by remember { mutableIntStateOf(1) }
    val targetPair = remember(round) { colors.random() }
    val displayColor = remember(round) { colors.filter { it != targetPair }.random().second }
    var isError by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = if (isError) "❌ Wrong! Tap the FONT COLOR, not word!" else "Tap the button matching the FONT COLOR below:",
            color = if (isError) AppColorPalette.LossRed else Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFamily,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(AppColorPalette.Surface)
                .border(2.dp, if (isError) AppColorPalette.LossRed else displayColor, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = targetPair.first,
                color = displayColor,
                fontSize = 42.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                letterSpacing = 2.sp
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colors.size) { idx ->
                val colorOption = colors[idx]
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val btnScale by animateFloatAsState(if (isPressed) 0.94f else 1.0f)

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .scale(btnScale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorOption.second.copy(alpha = 0.18f))
                        .border(2.dp, colorOption.second, RoundedCornerShape(16.dp))
                        .clickable(interactionSource = interactionSource, indication = null) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (colorOption.second == displayColor) {
                                isError = false
                                onSuccess()
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isError = true
                                round++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = colorOption.first,
                        color = colorOption.second,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = interFamily
                    )
                }
            }
        }
    }
}
