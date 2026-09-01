package com.social.wakesync.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

@Composable
fun SettingsScreen(
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    currentUsername: String = "nocturnaljake",
    onBack: (() -> Unit)? = null,
    onSoundClick: (() -> Unit)? = null,
    onPremiumClick: (() -> Unit)? = null,
    onSignOutClick: (() -> Unit)? = null,
    onDeleteAccountClick: (() -> Unit)? = null
) {
    var alarmAlertsEnabled by remember { mutableStateOf(true) }
    var friendActivityEnabled by remember { mutableStateOf(true) }
    var messagesEnabled by remember { mutableStateOf(true) }
    var silentModeOverrideEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = "Settings",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Settings Category Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // ── Section 1: ACCOUNT ───────────────────────────────────────────
            item {
                SettingsSectionGroup(
                    headerTitle = "ACCOUNT",
                    interFamily = interFamily
                ) {
                    // Profile Item
                    SettingsRowItem(
                        icon = "👤",
                        title = "Profile",
                        subtext = "@$currentUsername",
                        interFamily = interFamily,
                        hasChevron = true
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Privacy Item
                    SettingsRowItem(
                        icon = "🔒",
                        title = "Privacy",
                        subtext = "Friends only",
                        interFamily = interFamily,
                        hasChevron = true
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Linked Accounts Item
                    SettingsRowItem(
                        icon = "🔗",
                        title = "Linked Accounts",
                        subtext = "Google",
                        interFamily = interFamily,
                        hasChevron = true
                    )
                }
            }

            // ── Section 2: NOTIFICATIONS ──────────────────────────────────────
            item {
                SettingsSectionGroup(
                    headerTitle = "NOTIFICATIONS",
                    interFamily = interFamily
                ) {
                    // Alarm alerts
                    SettingsSwitchRowItem(
                        icon = "🔔",
                        title = "Alarm alerts",
                        subtext = "Full screen",
                        isChecked = alarmAlertsEnabled,
                        onCheckedChange = { alarmAlertsEnabled = it },
                        interFamily = interFamily
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Friend activity
                    SettingsSwitchRowItem(
                        icon = "📲",
                        title = "Friend activity",
                        subtext = "Digest only",
                        isChecked = friendActivityEnabled,
                        onCheckedChange = { friendActivityEnabled = it },
                        interFamily = interFamily
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Messages
                    SettingsSwitchRowItem(
                        icon = "💬",
                        title = "Messages",
                        subtext = "All",
                        isChecked = messagesEnabled,
                        onCheckedChange = { messagesEnabled = it },
                        interFamily = interFamily
                    )
                }
            }

            // ── Section 3: SOUNDS ─────────────────────────────────────────────
            item {
                SettingsSectionGroup(
                    headerTitle = "SOUNDS",
                    interFamily = interFamily
                ) {
                    // Alarm sound
                    SettingsRowItem(
                        icon = "🔊",
                        title = "Alarm sound",
                        subtext = "Neon Pulse",
                        interFamily = interFamily,
                        hasChevron = true,
                        onClick = { onSoundClick?.invoke() }
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Challenge sound
                    SettingsRowItem(
                        icon = "🎵",
                        title = "Challenge sound",
                        subtext = "Arena Entry",
                        interFamily = interFamily,
                        hasChevron = true
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Silent mode override
                    SettingsSwitchRowItem(
                        icon = "🔕",
                        title = "Silent mode override",
                        subtext = "Always on",
                        isChecked = silentModeOverrideEnabled,
                        onCheckedChange = { silentModeOverrideEnabled = it },
                        interFamily = interFamily
                    )
                }
            }

            // ── Section 4: PREMIUM ───────────────────────────────────────────
            item {
                SettingsSectionGroup(
                    headerTitle = "PREMIUM",
                    interFamily = interFamily
                ) {
                    SettingsRowItem(
                        icon = "👑",
                        title = "WakeSync Premium",
                        subtext = "Upgrade now",
                        interFamily = interFamily,
                        titleColor = AppColorPalette.GoldPremium,
                        hasChevron = true,
                        onClick = { onPremiumClick?.invoke() }
                    )
                }
            }

            // ── Section 5: DANGER ZONE ────────────────────────────────────────
            item {
                SettingsSectionGroup(
                    headerTitle = "DANGER ZONE",
                    interFamily = interFamily,
                    borderColor = AppColorPalette.LossRed.copy(alpha = 0.25f)
                ) {
                    // Sign Out
                    SettingsRowItem(
                        icon = "🚪",
                        title = "Sign Out",
                        subtext = "",
                        titleColor = AppColorPalette.LossRed,
                        interFamily = interFamily,
                        hasChevron = true,
                        onClick = { onSignOutClick?.invoke() }
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.05f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Delete Account
                    SettingsRowItem(
                        icon = "💀",
                        title = "Delete Account",
                        subtext = "This is permanent",
                        titleColor = AppColorPalette.LossRed,
                        interFamily = interFamily,
                        hasChevron = true,
                        onClick = { onDeleteAccountClick?.invoke() }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionGroup(
    headerTitle: String,
    interFamily: FontFamily,
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header Label
        Text(
            text = headerTitle,
            color = if (headerTitle == "DANGER ZONE") AppColorPalette.LossRed.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            fontWeight = FontWeight.W800,
            fontFamily = interFamily,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: String,
    title: String,
    subtext: String,
    interFamily: FontFamily,
    titleColor: Color = Color.White,
    hasChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Icon container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (titleColor == AppColorPalette.LossRed) AppColorPalette.LossRed.copy(alpha = 0.1f) else AppColorPalette.DeepSurface)
                .border(1.dp, if (titleColor == AppColorPalette.LossRed) AppColorPalette.LossRed.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtext Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtext.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (hasChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Navigate",
                tint = if (titleColor == AppColorPalette.LossRed) AppColorPalette.LossRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchRowItem(
    icon: String,
    title: String,
    subtext: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    interFamily: FontFamily
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Icon container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColorPalette.DeepSurface)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtext Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp,
                fontFamily = interFamily,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Cyan Active Switch
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColorPalette.CyanCta,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
