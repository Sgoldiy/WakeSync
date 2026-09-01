package com.social.wakesync.feature.splash

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font

@Composable
fun WakeSyncSplashScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "splash_transition")
    var startProgress by remember { mutableStateOf(false) }

    val outerRingScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "outer_ring_scale",
    )
    val innerRingScale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(360),
        ),
        label = "inner_ring_scale",
    )
    val flameScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flame_scale",
    )
    val flameTilt by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flame_tilt",
    )
    val progress by animateFloatAsState(
        targetValue = if (startProgress) 1f else 0f,
        animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
        label = "loading_progress",
    )

    val spaceGroteskFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    LaunchedEffect(Unit) {
        startProgress = true
        delay(3000)
        onFinished()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColorPalette.VoidBg,
                        Color(0xFF040916),
                    ),
                ),
            )
            .safeContentPadding(),
    ) {
        val minSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val outerOrbitSize = minSide * 0.72f
        val innerOrbitSize = minSide * 0.52f
        val flameSize = minSide * 0.19f
        val centerYOffset = -(minSide * 0.06f)
        val titleSize = when {
            minSide < 340.dp -> 34.sp
            minSide < 410.dp -> 38.sp
            else -> 42.sp
        }
        val subtitleSize = when {
            minSide < 340.dp -> 13.sp
            minSide < 410.dp -> 14.sp
            else -> 15.sp
        }
        val captionSize = when {
            minSide < 340.dp -> 11.sp
            minSide < 410.dp -> 14.sp
            else -> 13.sp
        }

        Box(modifier = Modifier.fillMaxSize()) {
            PulsingRing(
                size = outerOrbitSize,
                scale = outerRingScale,
                color = AppColorPalette.CyanCta.copy(alpha = 0.10f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = centerYOffset),
            )
            PulsingRing(
                size = innerOrbitSize,
                scale = innerRingScale,
                color = AppColorPalette.CyanCta.copy(alpha = 0.16f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = centerYOffset),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = centerYOffset),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                FlameIcon(
                    modifier = Modifier
                        .size(flameSize)
                        .scale(flameScale)
                        .rotate(flameTilt),
                )
                Spacer(modifier = Modifier.height(minSide * 0.03f))
                Text(
                    text = "WakeSync",
                    color = Color.White,
                    fontSize = titleSize,
                    fontWeight = FontWeight.W700,
                    fontFamily = spaceGroteskFamily,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.34f),
                            offset = Offset(0f, 3f),
                            blurRadius = 8f,
                        ),
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(minSide * 0.14f)
                        .height(2.dp)
                        .background(AppColorPalette.CyanCta, RoundedCornerShape(999.dp)),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Wake up.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.W400,
                    fontFamily = interFamily,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Or get left behind.",
                    color = AppColorPalette.CyanCta,
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.W600,
                    fontFamily = interFamily,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = minSide * 0.10f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(2.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(999.dp),
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(
                                color = AppColorPalette.CyanCta,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading...",
                    color = Color.White.copy(alpha = 0.36f),
                    fontSize = captionSize,
                    fontWeight = FontWeight.W400,
                    fontFamily = interFamily,
                )
            }
        }
    }
}

@Composable
private fun PulsingRing(
    size: Dp,
    scale: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .border(width = 0.8.dp, color = color, shape = CircleShape),
    )
}

@Composable
private fun FlameIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val outerFlame = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.06f)
            cubicTo(
                size.width * 0.20f, size.height * 0.30f,
                size.width * 0.08f, size.height * 0.58f,
                size.width * 0.29f, size.height * 0.84f,
            )
            cubicTo(
                size.width * 0.39f, size.height * 0.96f,
                size.width * 0.61f, size.height * 0.96f,
                size.width * 0.71f, size.height * 0.84f,
            )
            cubicTo(
                size.width * 0.93f, size.height * 0.58f,
                size.width * 0.79f, size.height * 0.30f,
                size.width * 0.50f, size.height * 0.06f,
            )
            close()
        }

        val innerFlame = Path().apply {
            moveTo(size.width * 0.52f, size.height * 0.25f)
            cubicTo(
                size.width * 0.36f, size.height * 0.40f,
                size.width * 0.30f, size.height * 0.62f,
                size.width * 0.40f, size.height * 0.80f,
            )
            cubicTo(
                size.width * 0.46f, size.height * 0.90f,
                size.width * 0.60f, size.height * 0.90f,
                size.width * 0.67f, size.height * 0.80f,
            )
            cubicTo(
                size.width * 0.76f, size.height * 0.66f,
                size.width * 0.69f, size.height * 0.44f,
                size.width * 0.52f, size.height * 0.25f,
            )
            close()
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AppColorPalette.StreakFireStart.copy(alpha = 0.44f),
                    Color.Transparent,
                ),
                center = center,
                radius = size.minDimension * 0.62f,
            ),
            radius = size.minDimension * 0.62f,
            center = center,
        )

        drawPath(
            path = outerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(
                    AppColorPalette.StreakFireStart,
                    AppColorPalette.LossRed,
                ),
                startY = size.height * 0.08f,
                endY = size.height * 0.95f,
            ),
            style = Fill,
        )

        drawPath(
            path = innerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(
                    AppColorPalette.StreakFireEnd,
                    AppColorPalette.StreakFireStart,
                ),
                startY = size.height * 0.18f,
                endY = size.height * 0.95f,
            ),
            style = Fill,
        )

        drawCircle(
            color = AppColorPalette.StreakFireEnd.copy(alpha = 0.40f),
            radius = size.minDimension * 0.20f,
            center = Offset(size.width * 0.52f, size.height * 0.66f),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}
