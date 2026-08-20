package com.social.wakesync.feature.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.wakesync.ui.theme.AppColorPalette
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onProfileCreated: () -> Unit = {}
) {
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val usernameStatus by viewModel.usernameStatus.collectAsState()

    val titleFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    var username by remember { mutableStateOf("") }
    var avatarEmoji by remember { mutableStateOf("😤") }
    var selectedGoal by remember { mutableStateOf("Wake up earlier") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showEmptyError by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            viewModel.checkUsername(username)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            onProfileCreated()
        }
    }

    val goals = listOf(
        Pair("🌅", "Wake up earlier"),
        Pair("🔁", "Build a habit"),
        Pair("🏆", "Beat my friends"),
        Pair("🧠", "Clear morning brain fog"),
        Pair("🏃‍♂️", "Get a head start on fitness"),
        Pair("📈", "Maximize daily productivity"),
        Pair("🧘‍♂️", "Start the day with mindfulness"),
        Pair("📵", "Reduce morning screen time"),
        Pair("💧", "Hydrate first thing"),
        Pair("📓", "Journal your thoughts")
    )

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

            // Fixed Header Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Set up your profile",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = titleFamily,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This is how rivals know who beat them.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W400,
                    fontFamily = interFamily,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fixed Avatar Picker
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showEmojiPicker = true }
            ) {
                Box(
                    modifier = Modifier.size(85.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = AppColorPalette.CyanCta.copy(alpha = 0.3f),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }
                    Text(text = avatarEmoji, fontSize = 42.sp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(AppColorPalette.CyanCta, CircleShape)
                            .border(2.dp, AppColorPalette.VoidBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to change",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W500
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fixed Username Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "USERNAME",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    fontFamily = interFamily,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val isUsernameTooShort = username.isNotEmpty() && username.length < 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(AppColorPalette.Surface, RoundedCornerShape(12.dp))
                        .border(
                            width = 1.5.dp,
                            color = when {
                                isUsernameTooShort -> AppColorPalette.LossRed
                                usernameStatus is UsernameStatus.Taken -> AppColorPalette.LossRed
                                usernameStatus is UsernameStatus.Available -> AppColorPalette.CyanCta
                                else -> Color.White.copy(alpha = 0.05f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "@",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 18.sp,
                        fontFamily = interFamily,
                        fontWeight = FontWeight.W400
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (username.isEmpty()) {
                            Text(
                                text = "Type your name",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 16.sp,
                                fontFamily = interFamily,
                                fontWeight = FontWeight.W600
                            )
                        }
                        BasicTextField(
                            value = username,
                            onValueChange = { 
                                if (it.length <= 15) {
                                    username = it.lowercase().filter { char -> char.isLetterOrDigit() || char == '.' || char == '_' }
                                    showEmptyError = false
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = interFamily,
                                fontWeight = FontWeight.W700
                            ),
                            cursorBrush = SolidColor(AppColorPalette.CyanCta),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                    
                    if (username.isNotEmpty()) {
                        when {
                            usernameStatus is UsernameStatus.Checking -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = AppColorPalette.CyanCta,
                                    strokeWidth = 2.dp
                                )
                            }
                            usernameStatus is UsernameStatus.Available && !isUsernameTooShort -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AppColorPalette.WinGreen, CircleShape)
                                )
                            }
                            usernameStatus is UsernameStatus.Taken || isUsernameTooShort -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AppColorPalette.LossRed, CircleShape)
                                )
                            }
                        }
                    }
                }
                if (showEmptyError) {
                    Text(
                        text = "Username is required",
                        color = AppColorPalette.LossRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                } else if (isUsernameTooShort) {
                    Text(
                        text = "Username must be at least 3 characters",
                        color = AppColorPalette.LossRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                } else if (usernameStatus is UsernameStatus.Taken) {
                    Text(
                        text = "This username is already taken",
                        color = AppColorPalette.LossRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scrollable Goals Section
            Text(
                text = "YOUR GOAL",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.W700,
                fontFamily = interFamily,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(goals) { (emoji, text) ->
                    GoalCard(
                        emoji = emoji,
                        text = text,
                        isSelected = selectedGoal == text,
                        onClick = {
                            selectedGoal = text
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        interFamily = interFamily
                    )
                }
            }
        }

        // Floating Continue Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val isLoading = uiState is ProfileUiState.Loading
            
            Button(
                onClick = {
                    if (isLoading) return@Button

                    if (username.isEmpty()) {
                        showEmptyError = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        return@Button
                    }
                    
                    if (username.length < 3) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        return@Button
                    }

                    if (usernameStatus is UsernameStatus.Taken) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        viewModel.saveProfile(username, avatarEmoji, selectedGoal)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.CyanCta,
                    contentColor = Color.Black,
                    disabledContainerColor = AppColorPalette.CyanCta.copy(alpha = 0.3f),
                    disabledContentColor = Color.Black.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Continue ⟶",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.W800,
                        fontFamily = titleFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (showEmojiPicker) {
            EmojiBottomSheet(
                onEmojiSelected = {
                    avatarEmoji = it
                    showEmojiPicker = false
                },
                onDismiss = { showEmojiPicker = false }
            )
        }

        // Error Feedback
        if (uiState is ProfileUiState.Error) {
            val errorMessage = (uiState as ProfileUiState.Error).message
            LaunchedEffect(errorMessage) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .padding(horizontal = 24.dp)
                    .background(AppColorPalette.LossRed.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GoalCard(
    emoji: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    interFamily: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColorPalette.Surface)
            .border(
                width = 1.5.dp,
                color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = text,
                color = if (isSelected) AppColorPalette.CyanCta else Color.White,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W600,
                fontFamily = interFamily,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AppColorPalette.CyanCta,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiBottomSheet(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val emojis = listOf(
        "🌅", "☀️", "⚡", "🚀", "🧠", "🏃‍♂️", "🧘‍♂️", "🔋", "🏆", "🔥",
        "🎯", "📈", "💎", "🦾", "🦁", "🦅", "🧗", "💪", "👊", "✨",
        "☕", "🦉", "🌅", "⭐", "🔋", "🦾", "🧬", "🧪", "🌋", "🏔️",
        "🌊", "🌈", "🎨", "🎮", "🎸", "🥊", "🏀", "⚽", "🍕", "🍔"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1522),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pick your avatar",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.W800
            )
            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 32.sp)
                    }
                }
            }
        }
    }
}
