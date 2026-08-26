package com.social.wakesync.feature.onboarding

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font

@Composable
fun OnboardingScreen1(
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "clock_float_transition")
    val floatingOffset by transition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1750, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "clock_floating_offset",
    )

    val titleFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    BoxWithConstraints(
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
            .safeContentPadding(),
    ) {
        val minSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val ringSize = minSide * 0.31f
        val iconSize = minSide * 0.15f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.92f))

            Box(
                modifier = Modifier
                    .size(ringSize)
                    .offset(y = floatingOffset.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF061325))
                    .border(
                        width = 1.dp,
                        color = AppColorPalette.CyanCta.copy(alpha = 0.42f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AppColorPalette.CyanCta.copy(alpha = 0.16f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
                Text(
                    text = "\u23F0",
                    fontSize = (iconSize.value * 0.80f).sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Set an Alarm",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pick your wake time, your challenge,\nand your stakes. No snooze. No\nmercy.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W400,
                fontFamily = interFamily,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            RowDots()
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF18D9F8),
                                    Color(0xFF12C7E3),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Next",
                        color = Color(0xFF02121B),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = titleFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Skip",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                fontFamily = interFamily,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RowDots() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 6.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(AppColorPalette.CyanCta),
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
        )
    }
}
