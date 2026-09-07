package com.social.wakesync.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

/**
 * Reusable animated empty state for any screen that has no data yet.
 *
 * @param emoji The big emoji icon to show
 * @param title The main heading text
 * @param subtitle The description text
 * @param titleFamily Font family for the title
 * @param interFamily Font family for the subtitle
 * @param modifier Modifier for the outer container
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for the emoji
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    // Floating shimmer lines
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji with glow background
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glow ring
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppColorPalette.CyanCta.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Emoji
            Text(
                text = emoji,
                fontSize = 52.sp,
                modifier = Modifier.scale(pulseScale)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.W800,
            fontFamily = titleFamily,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 14.sp,
            fontFamily = interFamily,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        // Animated shimmer lines below
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(3) { index ->
                val width = when (index) {
                    0 -> 180.dp
                    1 -> 140.dp
                    else -> 100.dp
                }
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(8.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = shimmerAlpha))
                )
            }
        }
    }
}
