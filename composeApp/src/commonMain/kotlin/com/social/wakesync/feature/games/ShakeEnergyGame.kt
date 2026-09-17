package com.social.wakesync.feature.games

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
fun ShakeEnergyGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var shakesCount by remember { mutableIntStateOf(0) }
    val requiredShakes = 15
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(shakesCount) {
        if (shakesCount >= requiredShakes) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSuccess()
        }
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(450, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )

    val progress = (shakesCount.toFloat() / requiredShakes.toFloat()).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(140.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(AppColorPalette.CyanCta.copy(alpha = 0.15f))
                .border(3.dp, AppColorPalette.CyanCta, CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    shakesCount++
                }
        ) {
            Text("📱", fontSize = 58.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("⚡ Tap/Shake rapidly to charge battery!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = interFamily)
            Text("${(progress * 100).toInt()}% Charged ($shakesCount / $requiredShakes)", color = AppColorPalette.CyanCta, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
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
