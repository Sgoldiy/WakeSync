package com.social.wakesync.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LongArrowBackIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        
        val path = Path().apply {
            // Horizontal line
            moveTo(width * 0.15f, height * 0.5f)
            lineTo(width * 0.85f, height * 0.5f)
            
            // Arrow head
            moveTo(width * 0.4f, height * 0.25f)
            lineTo(width * 0.15f, height * 0.5f)
            lineTo(width * 0.4f, height * 0.75f)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
