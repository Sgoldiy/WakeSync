package com.social.wakesync.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.wakesync.ui.navigation.HomeTab
import com.social.wakesync.ui.navigation.WakeSyncBottomBar
import com.social.wakesync.ui.theme.AppColorPalette
import myapplication.composeapp.generated.resources.Res
import myapplication.composeapp.generated.resources.inter_variable
import myapplication.composeapp.generated.resources.space_grotesk_variable
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(viewModel: HomeViewModel = viewModel { HomeViewModel() }) {
    val titleFamily = FontFamily(
        Font(Res.font.space_grotesk_variable, FontWeight.W700),
    )
    val interFamily = FontFamily(
        Font(Res.font.inter_variable, FontWeight.W600),
        Font(Res.font.inter_variable, FontWeight.W400),
    )

    var selectedTab by remember { mutableStateOf(HomeTab.HOME) }
    var showAlarmsScreen by remember { mutableStateOf(false) }
    var showSetAlarmScreen by remember { mutableStateOf(false) }
    var showAddHabitScreen by remember { mutableStateOf(false) }
    var selectedHabitForDetail by remember { mutableStateOf<Habit?>(null) }
    var activeHabit by remember { mutableStateOf<Habit?>(null) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Emoji Picker State
    var showEmojiPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (AlarmState.isRinging) {
        if (AlarmState.activeAlarmMode == "Duo" || AlarmState.activeAlarmMode == "Group") {
            AlarmPuzzleDuo(
                onDismiss = {
                    AlarmState.isRinging = false
                },
                titleFamily = titleFamily,
                interFamily = interFamily,
                userName = uiState.userName.ifEmpty { "You" },
                userAvatar = uiState.avatarEmoji.ifEmpty { "🤯" },
                alarmId = AlarmState.activeAlarmId,
                currentUserId = viewModel.getCurrentUserUid(),
                onListenToDuoAlarm = { id -> viewModel.listenToDuoAlarm(id) },
                onSetDuoAlarmWinner = { id, uid -> viewModel.setDuoAlarmWinner(id, uid) },
                onRecordWin = { id, mode -> viewModel.recordAlarmWin(id, mode) },
                onRecordLoss = { id, mode -> viewModel.recordAlarmLoss(id, mode) }
            )
        } else {
            AlarmPuzzleSolo(
                onDismiss = {
                    val activeId = AlarmState.activeAlarmId ?: ""
                    viewModel.recordAlarmWin(activeId, "Solo")
                    AlarmState.isRinging = false
                    AlarmState.showStreakSave = true
                },
                titleFamily = titleFamily,
                interFamily = interFamily
            )
        }
    } else if (AlarmState.showStreakSave) {
        StreakSaveScreen(
            onBackToHome = { AlarmState.showStreakSave = false },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else if (AlarmState.showStreakBroken) {
        StreakBrokenScreen(
            onBackToHome = { AlarmState.showStreakBroken = false },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else if (selectedHabitForDetail != null) {
        HabitDetailScreen(
            habit = selectedHabitForDetail!!,
            onBack = { selectedHabitForDetail = null },
            onDelete = { habitId ->
                viewModel.deleteHabit(habitId)
                selectedHabitForDetail = null
                activeHabit = null
            },
            onEdit = { habit ->
                editingHabit = habit
                selectedHabitForDetail = null
            },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else if (editingHabit != null) {
        AddHabitScreen(
            habit = editingHabit,
            onBack = { editingHabit = null },
            onSave = { title, icon, frequency, reminderTime, partnerUsername, bondName ->
                viewModel.updateHabit(editingHabit!!.id, title, icon, frequency, reminderTime, partnerUsername, bondName)
                editingHabit = null
                activeHabit = null
            },
            friends = uiState.friends,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else if (activeHabit != null) {
        val currentActiveHabit =
            uiState.habits.firstOrNull { it.id == activeHabit?.id } ?: activeHabit!!
        HabitActiveScreen(
            habit = currentActiveHabit,
            onBack = { activeHabit = null },
            onToggleDone = { habitId ->
                viewModel.toggleHabit(habitId)
            },
            onViewStats = { habit ->
                selectedHabitForDetail = habit
            },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else if (showAddHabitScreen) {
        AddHabitScreen(
            onBack = { showAddHabitScreen = false },
            onSave = { title, icon, frequency, reminderTime, partnerUsername, bondName ->
                viewModel.addHabit(title, icon, frequency, reminderTime, partnerUsername, bondName)
                showAddHabitScreen = false
            },
            friends = uiState.friends,
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else if (showSetAlarmScreen) {
        SetAlarmScreen(
            onBack = { showSetAlarmScreen = false },
            onSave = { hour, minute, isAm, days, mode, challenge, partnerUsername, bondName ->
                viewModel.addAlarm(hour, minute, isAm, days, mode, challenge, partnerUsername, bondName)
            },
            titleFamily = titleFamily,
            interFamily = interFamily,
            sounds = uiState.sounds,
            selectedSound = uiState.selectedSound,
            onSoundSelected = { viewModel.selectSound(it) },
            onSearchUsers = { query -> viewModel.searchUsers(query) }
        )
    } else if (showAlarmsScreen) {
        AlarmsScreen(
            viewModel = viewModel,
            onBack = { showAlarmsScreen = false },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else {
        Scaffold(
            bottomBar = {
                WakeSyncBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            containerColor = AppColorPalette.VoidBg
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    HomeTab.HOME -> {
                        when {
                            uiState.isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AppColorPalette.CyanCta)
                                }
                            }

                            else -> {
                                HomeContent(
                                    uiState = uiState,
                                    onHabitToggle = viewModel::toggleHabit,
                                    onAddHabitClick = { showAddHabitScreen = true },
                                    onHabitClick = { activeHabit = it },
                                    titleFamily = titleFamily,
                                    interFamily = interFamily,
                                    onAvatarClick = { showEmojiPicker = true },
                                    onAlarmClick = { showAlarmsScreen = true },
                                    onAutoSet = { viewModel.autoSetAlarm() },
                                    onAddNew = { showSetAlarmScreen = true },
                                    onToggleAlarm = viewModel::toggleAlarm,
                                    onAutoSetHabit = { viewModel.autoSetHabit() }
                                )
                            }
                        }
                    }

                    HomeTab.SOCIAL -> {
                        SocialFeedScreen(
                            titleFamily = titleFamily,
                            interFamily = interFamily
                        )
                    }

                    else -> PlaceholderContent(selectedTab.title)
                }

                if (showEmojiPicker) {
                    ModalBottomSheet(
                        onDismissRequest = { showEmojiPicker = false },
                        sheetState = sheetState,
                        containerColor = AppColorPalette.Surface,
                        contentColor = Color.White,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
                    ) {
                        EmojiPickerContent(
                            onEmojiSelected = { emoji ->
                                viewModel.updateAvatar(emoji)
                                showEmojiPicker = false
                            },
                            titleFamily = titleFamily,
                            interFamily = interFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onHabitToggle: (String) -> Unit,
    onAddHabitClick: () -> Unit,
    onHabitClick: (Habit) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onAvatarClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onAutoSet: () -> Unit,
    onAddNew: () -> Unit,
    onToggleAlarm: (String, Boolean) -> Unit,
    onAutoSetHabit: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Sticky Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorPalette.VoidBg)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            HomeHeader(
                uiState.dateText,
                uiState.userName,
                uiState.avatarEmoji,
                titleFamily,
                interFamily,
                onAvatarClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp)) // Increased to 20dp
            StreakCard(
                uiState.streak,
                uiState.wins,
                uiState.losses,
                uiState.rank,
                titleFamily,
                interFamily
            )
            Spacer(modifier = Modifier.height(10.dp))
            AlarmCard(
                hasAlarmToday = uiState.hasAlarmToday,
                time = uiState.nextAlarmTime,
                suggestedTime = uiState.suggestedTime,
                timeLeft = uiState.timeLeftToAlarm,
                mode = uiState.activeAlarmMode,
                titleFamily = titleFamily,
                interFamily = interFamily,
                onAlarmClick = onAlarmClick,
                onAutoSet = onAutoSet,
                onAddNew = onAddNew
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.habits.isEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Habits",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFamily
                            )
                            Text(
                                text = "You vs. yesterday.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontFamily = interFamily
                            )
                        }
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColorPalette.DeepSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏃", fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy((-2).dp)
                                ) {
                                    Text(
                                        text = "No habits set for today",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 11.sp,
                                        fontFamily = interFamily,
                                        fontWeight = FontWeight.W500
                                    )
                                    Text(
                                        text = "Suggested: Morning Run",
                                        color = AppColorPalette.CyanCta,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.W700,
                                        fontFamily = titleFamily
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AppColorPalette.CyanCta.copy(alpha = 0.15f))
                                        .border(
                                            1.dp,
                                            AppColorPalette.CyanCta.copy(alpha = 0.4f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onAutoSetHabit() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("⚡", fontSize = 13.sp)
                                        Text(
                                            text = "Auto Set",
                                            color = AppColorPalette.CyanCta,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.W800,
                                            fontFamily = interFamily
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.08f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onAddHabitClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("＋", fontSize = 13.sp, color = Color.White)
                                        Text(
                                            text = "Add New",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.W700,
                                            fontFamily = interFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                HabitSection(
                    uiState.habits,
                    onHabitToggle,
                    onHabitClick,
                    onAddHabitClick,
                    titleFamily,
                    interFamily
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            FriendsSection(uiState.friends, titleFamily, interFamily)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeHeader(
    date: String,
    name: String,
    avatar: String,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onAvatarClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 0.dp), // Removed top space
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy((-4).dp)) {
            Text(
                text = date,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                fontFamily = interFamily
            )
            Text(
                text = "Morning, $name 👋",
                color = Color.White,
                fontSize = 20.sp, // Morning text set to 20
                fontWeight = FontWeight.W700,
                fontFamily = titleFamily
            )
        }

        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onAvatarClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.DeepSurface)
                    .border(2.dp, AppColorPalette.WinGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatar,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun EmojiPickerContent(
    onEmojiSelected: (String) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val emojis = listOf(
        "🤯", "🔥", "🏃", "🚿", "📵", "📚", "🧘", "🦁",
        "🐺", "🦊", "🐻", "🐶", "😎", "🚀", "💪", "✨"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Update Avatar",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            fontFamily = titleFamily
        )
        Text(
            text = "You can only change this once a day",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontFamily = interFamily,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // Simple 4x4 grid
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { rowIndex ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { colIndex ->
                        val emoji = emojis[rowIndex * 4 + colIndex]
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColorPalette.DeepSurface)
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 34.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StreakCard(
    streak: Int,
    wins: Int,
    losses: Int,
    rank: String,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val transition = rememberInfiniteTransition(label = "streak_card_anim")

    // Background Animation: Soft drifting stars/particles and nebulae
    val animProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Space Background Animation
            Canvas(modifier = Modifier.matchParentSize()) {
                // Nebula-like glow clouds
                val nebula1Pos = Offset(
                    size.width * (0.2f + 0.1f * kotlin.math.sin(animProgress * 2 * kotlin.math.PI.toFloat())),
                    size.height * (0.5f + 0.1f * kotlin.math.cos(animProgress * 2 * kotlin.math.PI.toFloat()))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColorPalette.CyanCta.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = nebula1Pos,
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = nebula1Pos
                )

                val nebula2Pos = Offset(
                    size.width * (0.8f + 0.05f * kotlin.math.cos(animProgress * 2 * kotlin.math.PI.toFloat())),
                    size.height * (0.3f + 0.05f * kotlin.math.sin(animProgress * 2 * kotlin.math.PI.toFloat()))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColorPalette.StreakFireStart.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = nebula2Pos,
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = nebula2Pos
                )

                // Distant tiny stars (static-ish)
                for (i in 0 until 15) {
                    val x = (size.width * (i * 0.13f + 0.05f)) % size.width
                    val y = (size.height * (i * 0.17f + 0.07f)) % size.height
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = 0.8f,
                        center = Offset(x, y)
                    )
                }

                // Drifting particles / bigger stars
                for (i in 0 until 12) {
                    val progress = (animProgress + i * 0.15f) % 1f
                    val x = size.width * ((i * 0.17f + 0.1f) % 1f)
                    val y = size.height * (1.1f - progress * 1.2f)

                    val alpha = if (progress < 0.2f) {
                        progress / 0.2f
                    } else if (progress > 0.8f) {
                        (1f - progress) / 0.2f
                    } else {
                        1f
                    }

                    // Draw a small cross/star shape for some particles
                    if (i % 4 == 0) {
                        val starSize = 2f + (i % 2)
                        drawLine(
                            color = Color.White.copy(alpha = alpha * 0.15f),
                            start = Offset(x - starSize, y),
                            end = Offset(x + starSize, y),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = alpha * 0.15f),
                            start = Offset(x, y - starSize),
                            end = Offset(x, y + starSize),
                            strokeWidth = 1f
                        )
                    } else {
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.12f),
                            radius = 1.2f + (i % 3),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 6.dp
                    ) // Vertical padding reduced (Total -4dp)
            ) {
                Text(
                    text = "CURRENT STREAK",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = interFamily,
                    letterSpacing = 0.8.sp
                )

                Row(
                    verticalAlignment = Alignment.Bottom, // Fix: Align flame to bottom right of number
                    modifier = Modifier.offset(y = (-4).dp)
                ) {
                    Text(
                        text = streak.toString(),
                        color = Color.White,
                        fontSize = 76.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        lineHeight = 76.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .padding(bottom = 10.dp) // Pushes flame to match baseline of the number
                            .size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Soft Glow for the custom fire icon
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF8A3D).copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        FlameIcon(modifier = Modifier.size(34.dp))
                    }
                }

                Spacer(modifier = Modifier.height(2.dp)) // Spacer reduced (Total -4dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatItem(
                        label = "Wins",
                        value = "${wins}W",
                        color = AppColorPalette.WinGreen,
                        modifier = Modifier.weight(1f),
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                    StatItem(
                        label = "Losses",
                        value = "${losses}L",
                        color = Color(0xFFF54291),
                        modifier = Modifier.weight(1f),
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                    StatItem(
                        label = "Rank",
                        value = rank,
                        color = Color(0xFFFFD23D),
                        modifier = Modifier.weight(1f),
                        titleFamily = titleFamily,
                        interFamily = interFamily
                    )
                }
            }
        }
    }
}


@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    Box(
        modifier = modifier
            .height(52.dp) // Height reduced from 64dp (Total -12dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColorPalette.DeepSurface.copy(alpha = 0.6f))
            .border(1.dp, color.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy((-3).dp)) {
            Text(
                text = value,
                color = color,
                fontSize = 17.sp, // Slightly smaller to fit reduced height
                fontWeight = FontWeight.W900,
                fontFamily = titleFamily
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp, // Slightly smaller
                fontWeight = FontWeight.W500,
                fontFamily = interFamily
            )
        }
    }
}

@Composable
fun AlarmCard(
    hasAlarmToday: Boolean,
    time: String,
    suggestedTime: String,
    timeLeft: String,
    mode: String,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    onAlarmClick: () -> Unit,
    onAutoSet: () -> Unit,
    onAddNew: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onAlarmClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (hasAlarmToday) {
                // SCENARIO 1: User has an active scheduled alarm (clean, button-less layout)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColorPalette.DeepSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF8A3D).copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Text("⏰", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy((-4).dp)
                    ) {
                        Text(
                            text = "Next alarm",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            text = time,
                            color = AppColorPalette.CyanCta,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            fontFamily = titleFamily
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = "in $timeLeft",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W700
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val modeEmoji = when (mode) {
                                "Group" -> "⚡"
                                "Duo" -> "⚔️"
                                else -> "👤"
                            }
                            val modeColor = when (mode) {
                                "Group" -> AppColorPalette.WinGreen
                                "Duo" -> AppColorPalette.CyanCta
                                else -> Color.White.copy(alpha = 0.5f)
                            }
                            Text(
                                text = modeEmoji,
                                color = modeColor,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$mode Mode",
                                color = modeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = interFamily
                            )
                        }
                    }
                }
            } else {
                // SCENARIO 2: No alarm set for today -> Suggest default time + Auto Set & Add New buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColorPalette.DeepSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💤", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy((-2).dp)
                    ) {
                        Text(
                            text = "No alarm set for today",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontFamily = interFamily,
                            fontWeight = FontWeight.W500
                        )
                        Text(
                            text = "Suggested: $suggestedTime",
                            color = AppColorPalette.CyanCta,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W700,
                            fontFamily = titleFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons inside card: Left = Auto Set, Right = Add New
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Auto Set Button (Primary Cyan accent)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColorPalette.CyanCta.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                AppColorPalette.CyanCta.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onAutoSet() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⚡", fontSize = 13.sp)
                            Text(
                                text = "Auto Set",
                                color = AppColorPalette.CyanCta,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W800,
                                fontFamily = interFamily
                            )
                        }
                    }

                    // Add New Button (Secondary subtle style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .clickable { onAddNew() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⏰", fontSize = 13.sp)
                            Text(
                                text = "Add New",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W700,
                                fontFamily = interFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeAlarmItem(
    alarm: AlarmData,
    onToggle: (Boolean) -> Unit,
    onAlarmClick: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val alpha = if (alarm.isEnabled) 1f else 0.35f

    val displayTime = remember(alarm.time) {
        try {
            val parts = alarm.time.split(":")
            var h = parts[0].toInt()
            val m = parts[1]
            val ampm = if (h >= 12) "PM" else "AM"
            if (h > 12) h -= 12
            if (h == 0) h = 12
            "$h:$m $ampm"
        } catch (e: Exception) {
            alarm.time
        }
    }

    val displayDays = remember(alarm.days) {
        if (alarm.days.size == 7) "Daily"
        else if (alarm.days.isEmpty()) "Once"
        else {
            val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
            alarm.days.sorted().joinToString(" ") { dayNames[it] }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAlarmClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) AppColorPalette.Surface else AppColorPalette.Surface.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (alarm.isEnabled) AppColorPalette.CyanCta.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayTime,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily
                    )
                    
                    // Small mode indicator badge
                    val modeColor = when (alarm.mode) {
                        "Group" -> AppColorPalette.WinGreen
                        "Duo" -> AppColorPalette.CyanCta
                        else -> Color.White.copy(alpha = 0.4f)
                    }
                    Text(
                        text = alarm.mode,
                        color = modeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        modifier = Modifier
                            .background(modeColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .border(1.dp, modeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${alarm.label} · $displayDays",
                    color = Color.White.copy(alpha = 0.4f * alpha),
                    fontSize = 11.sp,
                    fontFamily = interFamily,
                    fontWeight = FontWeight.W600
                )
            }

            // Custom Switch
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(24.dp)
                    .clip(CircleShape)
                    .background(if (alarm.isEnabled) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.1f))
                    .clickable { onToggle(!alarm.isEnabled) }
                    .padding(3.dp),
                contentAlignment = if (alarm.isEnabled) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
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


@Composable
fun HabitSection(
    habits: List<Habit>,
    onHabitToggle: (String) -> Unit,
    onHabitClick: (Habit) -> Unit,
    onAddHabitClick: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    val total = habits.size
    val doneCount = habits.count { it.isDone }
    val remainingCount = total - doneCount
    val completionRate = if (total > 0) (doneCount * 100 / total) else 0

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Habits Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Habits",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = titleFamily
                )
                Text(
                    text = "You vs. yesterday.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontFamily = interFamily
                )
            }

            // Green Percentage Box
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorPalette.WinGreen.copy(alpha = 0.08f))
                    .border(
                        1.dp,
                        AppColorPalette.WinGreen.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completionRate%",
                        color = AppColorPalette.WinGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "today",
                        color = AppColorPalette.WinGreen.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }
        }

        // Horizontal Progress Bar
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val progress = if (total > 0) (doneCount.toFloat() / total.toFloat()) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AppColorPalette.CyanCta, AppColorPalette.WinGreen)
                            )
                        )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$doneCount of $total done",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
                Text(
                    text = "$remainingCount remaining",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily
                )
            }
        }

        // List of Habits (Card Layout)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            habits.forEachIndexed { index, habit ->
                HabitItem(
                    habit = habit,
                    index = index,
                    onHabitToggle = onHabitToggle,
                    onHabitClick = onHabitClick,
                    interFamily = interFamily
                )
            }
        }

        // "+ Add habit" dashed button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        color = AppColorPalette.CyanCta.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .background(AppColorPalette.Surface.copy(alpha = 0.1f))
                .clickable { onAddHabitClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add habit",
                color = AppColorPalette.CyanCta,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFamily
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    index: Int,
    onHabitToggle: (String) -> Unit,
    onHabitClick: (Habit) -> Unit,
    interFamily: FontFamily,
) {
    val emoji = when (habit.iconType) {
        HabitIconType.RUN -> "🏃"
        HabitIconType.SHOWER -> "🚿"
        HabitIconType.NO_PHONE -> "📵"
        HabitIconType.READING -> "📚"
        HabitIconType.STRETCH -> "🧘"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHabitClick(habit) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress around icon
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                // Determine progress arc based on the habit index or streak
                val progressSweep = when (index % 5) {
                    0 -> 280f
                    1 -> 360f
                    2 -> 220f
                    3 -> 140f
                    else -> 90f
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.minDimension / 2 - 2.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Active progress arc
                    drawArc(
                        color = if (habit.isDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta,
                        startAngle = -90f,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Habit Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    color = Color.White.copy(alpha = if (habit.isDone) 0.5f else 0.95f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFamily,
                    textDecoration = if (habit.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔥 ${habit.streak} streak",
                        color = AppColorPalette.StreakFireStart,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "·",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = habit.frequency,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = interFamily
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkmark Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (habit.isDone) AppColorPalette.WinGreen else Color.White.copy(
                            alpha = 0.03f
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (habit.isDone) AppColorPalette.WinGreen else Color.White.copy(
                            alpha = 0.12f
                        ),
                        shape = CircleShape
                    )
                    .clickable { onHabitToggle(habit.id) },
                contentAlignment = Alignment.Center
            ) {
                if (habit.isDone) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FriendsSection(friends: List<Friend>, titleFamily: FontFamily, interFamily: FontFamily) {
    Column {
        Text(
            text = "Friends Right Now",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.W700,
            fontFamily = titleFamily
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(friends) { friend ->
                val ringColor = when (friend.status) {
                    FriendStatus.ACTIVE -> AppColorPalette.WinGreen
                    FriendStatus.INACTIVE -> AppColorPalette.CyanCta
                    FriendStatus.FAILED -> AppColorPalette.LossRed
                    FriendStatus.NEW -> AppColorPalette.CyanCta
                }

                FriendItem(
                    friend.name,
                    friend.avatar,
                    friend.streak,
                    ringColor,
                    titleFamily,
                    interFamily
                )
            }
        }
    }
}

@Composable
fun FriendItem(
    name: String,
    avatar: String,
    streak: Int,
    ringColor: Color,
    titleFamily: FontFamily,
    interFamily: FontFamily,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .border(1.5.dp, ringColor, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(AppColorPalette.DeepSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatar,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.W500,
            fontFamily = interFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Center
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-2).dp)
        ) {
            FlameIcon(modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(2.dp))
            Text(
                text = streak.toString(),
                color = Color(0xFFFF8A3D),
                fontSize = 10.sp,
                fontWeight = FontWeight.W800,
                fontFamily = interFamily
            )
        }
    }
}

@Composable
fun PlaceholderContent(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title Screen",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
@Preview
fun HomeContentPreview() {
    val titleFamily = FontFamily.Default
    val interFamily = FontFamily.Default

    val mockUiState = HomeUiState(
        isLoading = false,
        userName = "Jake",
        avatarEmoji = "🤯",
        dateText = "Thursday, May 1",
        streak = 23,
        wins = 18,
        losses = 5,
        rank = "#4",
        nextAlarmTime = "6:30 AM",
        timeLeftToAlarm = "9h 14m",
        isGroupAlarm = true,
        habits = listOf(
            Habit("1", "Morning Run", HabitIconType.RUN, true),
            Habit("2", "Cold Shower", HabitIconType.SHOWER, true),
            Habit("3", "No Phone 1hr", HabitIconType.NO_PHONE, true),
            Habit("4", "Read 20 mins", HabitIconType.READING, false),
            Habit("5", "Stretch", HabitIconType.STRETCH, false)
        ),
        friends = listOf(
            Friend("1", "maya.rises", "🦁", 41, FriendStatus.ACTIVE),
            Friend("2", "5amclub..", "🐺", 89, FriendStatus.INACTIVE),
            Friend("3", "nocturna..", "🦊", 7, FriendStatus.FAILED),
            Friend("4", "grind.rio", "🐻", 15, FriendStatus.NEW)
        )
    )

    MaterialTheme {
        Surface(color = AppColorPalette.VoidBg) {
            HomeContent(
                uiState = mockUiState,
                onHabitToggle = {},
                onAddHabitClick = {},
                onHabitClick = {},
                titleFamily = titleFamily,
                interFamily = interFamily,
                onAvatarClick = {},
                onAlarmClick = {},
                onAutoSet = {},
                onAddNew = {},
                onToggleAlarm = { _, _ -> },
                onAutoSetHabit = {}
            )
        }
    }
}
