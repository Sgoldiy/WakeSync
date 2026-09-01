package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

data class PremiumFeatureItem(
    val icon: String,
    val title: String,
    val subtext: String
)

@Composable
fun PremiumScreen(
    onClose: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    onUpgradeClick: (isYearly: Boolean) -> Unit = {},
    features: List<PremiumFeatureItem> = remember {
        listOf(
            PremiumFeatureItem("📊", "Advanced Stats", "Detailed wake analytics"),
            PremiumFeatureItem("🎨", "Custom Themes", "Exclusive dark palettes"),
            PremiumFeatureItem("⚡", "Priority in Duo", "Choose your challenge"),
            PremiumFeatureItem("🤫", "Ghost Mode", "Hide your losses from feed"),
            PremiumFeatureItem("🏷️", "Premium Badge", "Gold ring on your avatar")
        )
    }
) {
    var isYearlySelected by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Bar (Close button on left)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }

        // Header Branding
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👑",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "WakeSync Premium",
                color = AppColorPalette.GoldPremium,
                fontSize = 28.sp,
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "For people serious about winning.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // ── Featured Highlight Card: Streak Insurance ───────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.GoldPremium.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, AppColorPalette.GoldPremium.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shield Icon Container
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF332A15))
                                .border(1.dp, AppColorPalette.GoldPremium.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛡️",
                                fontSize = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Streak Insurance Text
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Streak Insurance",
                                color = AppColorPalette.GoldPremium,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.W700,
                                fontFamily = interFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Miss an alarm without losing your streak. Use 2× per month. Your record, protected.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontFamily = interFamily,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // ── 5 Premium Feature List Rows ────────────────────────────────
            items(features) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColorPalette.DeepSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.icon,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Subtext
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.W700,
                                fontFamily = interFamily
                            )
                            Text(
                                text = item.subtext,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                fontFamily = interFamily
                            )
                        }

                        // Green Checkmark
                        Text(
                            text = "✓",
                            color = AppColorPalette.WinGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W900
                        )
                    }
                }
            }

            // ── Selectable Pricing Cards (Monthly vs Yearly) ──────────────────
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Monthly Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isYearlySelected = false },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!isYearlySelected) AppColorPalette.GoldPremium.copy(alpha = 0.06f) else AppColorPalette.Surface
                        ),
                        border = if (!isYearlySelected) {
                            BorderStroke(1.5.dp, AppColorPalette.GoldPremium)
                        } else {
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$4.99",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.W900,
                                fontFamily = titleFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "per month",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                fontFamily = interFamily
                            )
                        }
                    }

                    // Yearly Card (with SAVE 33% badge)
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isYearlySelected = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isYearlySelected) AppColorPalette.GoldPremium.copy(alpha = 0.06f) else AppColorPalette.Surface
                            ),
                            border = if (isYearlySelected) {
                                BorderStroke(1.5.dp, AppColorPalette.GoldPremium)
                            } else {
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$39.99",
                                    color = AppColorPalette.GoldPremium,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.W900,
                                    fontFamily = titleFamily
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "per year",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 13.sp,
                                    fontFamily = interFamily
                                )
                            }
                        }

                        // Floating SAVE 33% Badge on Top Right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = (-10).dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(AppColorPalette.GoldPremium)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SAVE 33%",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.W900,
                                fontFamily = titleFamily
                            )
                        }
                    }
                }
            }

            // ── Gold Upgrade CTA & Footer Subtext ─────────────────────────────
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFD23D),
                                    Color(0xFFFF8A3D)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onUpgradeClick(isYearlySelected) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Upgrade to Premium 👑",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = titleFamily
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "7-day free trial · Cancel anytime",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontFamily = interFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
