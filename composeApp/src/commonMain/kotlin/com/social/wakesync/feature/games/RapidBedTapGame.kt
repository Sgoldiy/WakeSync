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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun RapidBedTapGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var tapCount by remember { mutableIntStateOf(0) }
    val requiredTaps = 12
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(tapCount) {
        if (tapCount >= requiredTaps) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSuccess()
        }
    }

    val progress = (tapCount.toFloat() / requiredTaps.toFloat()).coerceIn(0f, 1f)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val padScale by animateFloatAsState(if (isPressed) 0.88f else 1.0f)

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("👆 Rapidly tap the bed-pad to wake up!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)

        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(padScale)
                .clip(CircleShape)
                .background(AppColorPalette.WinGreen.copy(alpha = 0.18f))
                .border(3.5.dp, AppColorPalette.WinGreen, CircleShape)
                .clickable(interactionSource = interactionSource, indication = null) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    tapCount++
                },
            contentAlignment = Alignment.Center
        ) {
            Text("👆", fontSize = 58.sp)
        }

        Text("Progress: $tapCount / $requiredTaps Taps", color = AppColorPalette.WinGreen, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(18.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(AppColorPalette.CyanCta, AppColorPalette.WinGreen)))
            )
        }
    }
}
