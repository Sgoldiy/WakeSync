package com.social.wakesync.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.social.wakesync.ui.theme.AppColorPalette

enum class HomeTab(val title: String, val icon: ImageVector, val color: Color) {
    HOME("Home", Icons.Rounded.Home, AppColorPalette.CyanCta),
    LEADERBOARD("Ranking", Icons.Rounded.Leaderboard, AppColorPalette.GoldPremium),
    SOCIAL("Feed", Icons.Rounded.AutoAwesome, AppColorPalette.StreakFireStart),
    CHAT("Messages", Icons.Rounded.ChatBubble, AppColorPalette.WinGreen),
    PROFILE("Profile", Icons.Rounded.Person, AppColorPalette.LossRed)
}

@Composable
fun WakeSyncBottomBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    val tabs = HomeTab.entries
    val selectedIndex = tabs.indexOf(selectedTab)
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition()
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(65.dp)
                .fillMaxWidth(),
            shape = androidx.compose.ui.graphics.RectangleShape,
            color = AppColorPalette.VoidBg,
            tonalElevation = 0.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.TopCenter)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.0f),
                                    Color.White.copy(alpha = 0.02f),
                                    Color.White.copy(alpha = 0.0f),
                                ),
                                start = Offset(shimmerTranslate - 400f, shimmerTranslate - 400f),
                                end = Offset(shimmerTranslate, shimmerTranslate)
                            )
                        )
                )

                val leftOffset by animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = 160f)
                )
                val rightOffset by animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f)
                )

                val indicatorColor by animateColorAsState(
                    targetValue = selectedTab.color,
                    animationSpec = tween(durationMillis = 400)
                )

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val tabWidth = maxWidth / tabs.size
                    
                    val startX = tabWidth * (if (leftOffset < rightOffset) leftOffset else rightOffset)
                    val endX = tabWidth * (if (leftOffset > rightOffset) leftOffset else rightOffset) + tabWidth
                    val indicatorWidth = endX - startX
                    
                    val baseHeight = 44.dp
                    val stretchFactor = (indicatorWidth / tabWidth).coerceIn(1f, 2.5f)
                    val dynamicHeight = baseHeight * (1f - (stretchFactor - 1f) * 0.12f)
                    val verticalCenteringOffset = (65.dp - dynamicHeight) / 2
                    
                    val horizontalPadding = (tabWidth - 44.dp) / 2

                    Box(
                        modifier = Modifier
                            .offset(x = startX, y = verticalCenteringOffset)
                            .width(indicatorWidth)
                            .height(dynamicHeight)
                            .padding(horizontal = horizontalPadding.coerceAtLeast(0.dp))
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        indicatorColor.copy(alpha = 0.15f),
                                        indicatorColor.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(1.dp, indicatorColor.copy(alpha = 0.25f), CircleShape)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize().selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.2f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onTabSelected(tab)
                                        }
                                    },
                                    role = Role.Tab,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier
                                    .size(25.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                tint = if (isSelected) tab.color else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
