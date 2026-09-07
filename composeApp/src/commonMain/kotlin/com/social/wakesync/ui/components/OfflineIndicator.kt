package com.social.wakesync.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A thin status bar that shows:
 * - "Offline — X changes pending" when offline with pending writes
 * - "Syncing..." when coming back online
 * - Hidden when online with no pending writes
 */
@Composable
fun OfflineStatusBar(
    isOnline: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val showOffline = !isOnline && pendingCount > 0
    val showSyncing = isOnline && pendingCount > 0

    AnimatedVisibility(
        visible = showOffline || showSyncing,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        val bgColor = if (isOnline) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFFF6B35).copy(alpha = 0.15f)
        val textColor = if (isOnline) Color(0xFF22C55E) else Color(0xFFFF6B35)
        val text = if (isOnline) {
            "Syncing $pendingCount change${if (pendingCount > 1) "s" else ""}..."
        } else {
            "Offline — $pendingCount change${if (pendingCount > 1) "s" else ""} pending"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing dot
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotAlpha"
            )

            Text(
                text = "●",
                fontSize = 8.sp,
                color = textColor,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .alpha(alpha)
            )

            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Inline badge showing pending sync count (e.g., on the home screen header).
 */
@Composable
fun SyncBadge(
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    if (pendingCount <= 0) return

    Text(
        text = "$pendingCount",
        color = Color(0xFFFF6B35),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(
                color = Color(0xFFFF6B35).copy(alpha = 0.15f),
                shape = androidx.compose.foundation.shape.CircleShape
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
