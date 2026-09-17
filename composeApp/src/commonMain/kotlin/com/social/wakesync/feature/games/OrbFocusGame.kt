package com.social.wakesync.feature.games

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun OrbFocusGame(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var tapsLeft by remember { mutableIntStateOf(4) }
    var orbXOffset by remember { mutableIntStateOf(0) }
    var orbYOffset by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val animatedX by animateDpAsState(targetValue = orbXOffset.dp, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
    val animatedY by animateDpAsState(targetValue = orbYOffset.dp, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))

    LaunchedEffect(tapsLeft) {
        if (tapsLeft <= 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSuccess()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("🎯 Focus & tap glowing orb as it jumps!", color = Color.White.copy(alpha = 0.75f), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)

        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(AppColorPalette.Surface)
                .border(1.5.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(x = animatedX, y = animatedY)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.CyanCta)
                    .border(2.5.dp, Color.White, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        tapsLeft--
                        orbXOffset = (-65..65).random()
                        orbYOffset = (-65..65).random()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("🎯", fontSize = 26.sp)
            }
        }

        Text("$tapsLeft target taps remaining", color = AppColorPalette.CyanCta, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = interFamily)
    }
}
