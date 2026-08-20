package com.social.wakesync.feature.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font

@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    onAllPermissionsGranted: () -> Unit = {}
) {
    val titleFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    val handler = remember { getPermissionHandler() }
    
    var alarmInteracted by remember { mutableStateOf(false) }
    var notificationInteracted by remember { mutableStateOf(false) }
    var cameraInteracted by remember { mutableStateOf(false) }

    // Start all permissions as false to force the user to grant them manually
    var alarmGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(false) }

    // Logic to refresh permissions when returning to the screen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (alarmInteracted) alarmGranted = handler.isAlarmPermissionGranted()
                if (notificationInteracted) notificationGranted = handler.isNotificationPermissionGranted()
                if (cameraInteracted) cameraGranted = handler.isCameraPermissionGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val allEssentialGranted = alarmGranted && notificationGranted && cameraGranted

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Grant access",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = titleFamily,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "We need a few things to do our job.",
                    color = Color.White.copy(alpha = 0.64f),
                    fontSize = 15.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W400
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permission Items
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PermissionItem(
                    title = "Alarms & Reminders",
                    description = "We can't wake you up without this. Obviously.",
                    emoji = "⏰",
                    isGranted = alarmGranted,
                    onToggle = { 
                        if (!it) return@PermissionItem
                        alarmInteracted = true
                        handler.requestAlarmPermission() 
                    },
                    interFamily = interFamily
                )

                PermissionItem(
                    title = "Notifications",
                    description = "Get battle updates and friend activity in real time.",
                    emoji = "🔔",
                    isGranted = notificationGranted,
                    onToggle = { 
                        if (!it) return@PermissionItem
                        notificationInteracted = true
                        handler.requestNotificationPermission() 
                    },
                    interFamily = interFamily
                )

                PermissionItem(
                    title = "Camera",
                    description = "Photo proof for challenges. No cam = no alibi.",
                    emoji = "📸",
                    isGranted = cameraGranted,
                    onToggle = { 
                        if (!it) return@PermissionItem
                        cameraInteracted = true
                        handler.requestCameraPermission() 
                    },
                    interFamily = interFamily
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Warning Box
            if (!cameraGranted || !alarmGranted || !notificationGranted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFF8A3D).copy(alpha = 0.08f))
                        .border(1.2.dp, Color(0xFFFF8A3D).copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF8A3D).copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        val warningText = when {
                            !alarmGranted -> "Alarms are critical. We can't wake you up without this permission."
                            !notificationGranted -> "Notifications are recommended to keep track of your streaks."
                            else -> "Camera permission is required for photo-proof challenges. You can still use math or shake modes without it."
                        }
                        Text(
                            text = warningText,
                            color = Color(0xFFFF8A3D).copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontFamily = interFamily,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = { if (allEssentialGranted) onAllPermissionsGranted() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 0.dp),
                enabled = allEssentialGranted,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.CyanCta,
                    contentColor = Color.Black,
                    disabledContainerColor = AppColorPalette.CyanCta.copy(alpha = 0.25f),
                    disabledContentColor = Color.Black.copy(alpha = 0.35f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "All set. Let's go",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = titleFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    emoji: String,
    isGranted: Boolean,
    onToggle: (Boolean) -> Unit,
    interFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColorPalette.Surface)
            .border(
                width = 1.dp,
                color = if (isGranted) AppColorPalette.CyanCta.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorPalette.VoidBg)
                    .border(1.dp, if(isGranted) AppColorPalette.WinGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W400
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Switch
            Switch(
                checked = isGranted,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColorPalette.WinGreen,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.08f),
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}
