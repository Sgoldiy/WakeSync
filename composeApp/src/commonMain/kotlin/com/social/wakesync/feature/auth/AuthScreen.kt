package com.social.wakesync.feature.auth

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.ic_google
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
) {
    val titleFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    val transition = rememberInfiniteTransition(label = "auth_flame_transition")
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColorPalette.VoidBg,
                        Color(0xFF040918),
                    ),
                ),
            )
            .safeContentPadding()
    ) {
        // Main Center Content (Flame + Join the Arena + Tagline)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                PulsingRing(
                    size = 140.dp,
                    scale = outerRingScale,
                    color = AppColorPalette.CyanCta.copy(alpha = 0.10f),
                )
                PulsingRing(
                    size = 100.dp,
                    scale = innerRingScale,
                    color = AppColorPalette.CyanCta.copy(alpha = 0.16f),
                )
                FlameIcon(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(flameScale)
                        .rotate(flameTilt),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Join the Arena",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Your squad is already waking up.",
                color = Color.White.copy(alpha = 0.74f),
                fontSize = 17.sp,
                fontWeight = FontWeight.W400,
                fontFamily = interFamily,
                textAlign = TextAlign.Center,
            )
        }

        // Bottom Actions (Sign In Button + Terms)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GoogleSignInButton(
                text = if (isLoading) "Signing in..." else "Continue with Google",
                enabled = !isLoading,
                onClick = onGoogleSignIn,
                interFamily = interFamily,
            )

            Spacer(modifier = Modifier.height(24.dp))

            val footerText = buildAnnotatedString {
                append("By continuing you agree to the ")
                withStyle(style = SpanStyle(color = AppColorPalette.CyanCta)) {
                    append("Terms")
                }
                append("\u00A0")
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.42f))) {
                    append("&")
                }
                append("\u00A0")
                withStyle(style = SpanStyle(color = AppColorPalette.CyanCta)) {
                    append("Privacy\u00A0Policy")
                }
            }

            Text(
                text = footerText,
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 12.sp,
                fontWeight = FontWeight.W400,
                fontFamily = interFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    interFamily: FontFamily,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.White, shape)
            .border(1.dp, Color(0xFFE5E5E5), shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = "Google Icon",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = Color(0xFF111111),
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                fontFamily = interFamily,
            )
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
    Canvas(modifier = modifier) {
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

@Preview
@Composable
fun AuthScreenPreview() {
    MaterialTheme {
        AuthScreen(
            isLoading = false,
            onGoogleSignIn = {},
        )
    }
}

@Preview
@Composable
fun AuthScreenLoadingPreview() {
    MaterialTheme {
        AuthScreen(
            isLoading = true,
            onGoogleSignIn = {},
        )
    }
}
