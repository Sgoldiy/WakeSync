package com.social.wakesync.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmScreen(
    onBack: () -> Unit,
    onSave: (hour: Int, minute: Int, isAm: Boolean, days: List<Int>, mode: String, challenge: String, partnerUsername: String?, bondName: String?) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    sounds: List<SoundMetadata> = emptyList(),
    selectedSound: SoundMetadata? = null,
    onSoundSelected: (SoundMetadata) -> Unit = {},
    onSearchUsers: (suspend (String) -> List<Friend>)? = null,
    preselectedRival: String? = null
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()
    var isAm by remember { mutableStateOf(true) }
    var selectedHour by remember { mutableIntStateOf(6) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var selectedMode by remember { mutableStateOf(if (preselectedRival != null) "Duo" else "Solo") }
    // User must pick exactly 3 wake-up games — solved in order to dismiss the alarm.
    // No games pre-selected — the user chooses exactly 3 themselves.
    val selectedGames = remember { mutableStateListOf<String>() }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var demoGameTarget by remember { mutableStateOf<GenZGameItem?>(null) }
    val selectedDays = remember { mutableStateListOf(0, 1, 2, 3, 4) }
    var selectedPenalty by remember { mutableStateOf("shame") }
    var bondName by remember { mutableStateOf("") }
    var activeSound by remember(selectedSound) {
        mutableStateOf(selectedSound ?: DefaultGenZSoundCatalog.first())
    }
    var showAudioLibraryModal by remember { mutableStateOf(false) }

    // Duo / Group User Search Bottom Sheet State
    var showAddParticipantsSheet by remember { mutableStateOf(false) }
    var showFindRivalsScreen by remember { mutableStateOf(false) }
    var pendingParticipantUsername by remember { mutableStateOf<String?>(null) }
    val selectedParticipants = remember { mutableStateListOf<String>() }
    
    // Pre-fill rival if coming from UserProfileScreen challenge
    androidx.compose.runtime.LaunchedEffect(preselectedRival) {
        if (preselectedRival != null && selectedParticipants.isEmpty()) {
            selectedParticipants.add(preselectedRival)
        }
    }
    
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()

    // Confirmation dialog before adding partner
    pendingParticipantUsername?.let { username ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingParticipantUsername = null },
            title = {
                Text(
                    text = "Add Partner?",
                    fontFamily = titleFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You're about to add @$username as your ${if (selectedMode == "Duo") "duo partner" else "group member"}. " +
                        "They'll receive an invitation to join this alarm. Continue?",
                    fontFamily = interFamily,
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val maxAllowed = if (selectedMode == "Duo") 1 else 7
                    if (selectedParticipants.size < maxAllowed && !selectedParticipants.contains(username)) {
                        selectedParticipants.add(username)
                    }
                    pendingParticipantUsername = null
                }) {
                    Text(
                        text = "Add Partner",
                        color = AppColorPalette.CyanCta,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingParticipantUsername = null }) {
                    Text(
                        text = "Cancel",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            },
            containerColor = AppColorPalette.Surface,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f)
        )
    }

    if (showFindRivalsScreen) {
        FindRivalsScreen(
            onBack = { showFindRivalsScreen = false },
            onRivalSelected = { username ->
                selectedParticipants.clear()
                selectedParticipants.add(username)
                showFindRivalsScreen = false
            },
            titleFamily = titleFamily,
            interFamily = interFamily
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = AppColorPalette.VoidBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColorPalette.VoidBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        com.social.wakesync.ui.utils.LongArrowBackIcon(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "New Alarm",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = titleFamily,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Clock Picker Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131829)),
                    border = BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Combined Centered Row (Time Pickers + AM/PM stacked Column)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp)
                        ) {
                            // Time Picker Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Hour Picker
                                WheelTimePicker(
                                    items = (1..12).map { it.toString().padStart(2, '0') },
                                    initialIndex = 5,
                                    onItemSelected = { selectedHour = it.toInt() },
                                    titleFamily = titleFamily,
                                    modifier = Modifier.weight(1f)
                                )

                                // Colon separator - text character instead of dots
                                Text(
                                    text = ":",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = titleFamily,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )

                                // Minute Picker
                                WheelTimePicker(
                                    items = (0..59).map { it.toString().padStart(2, '0') },
                                    initialIndex = 30, // index 30 = "30"
                                    onItemSelected = { selectedMinute = it.toInt() },
                                    titleFamily = titleFamily,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // AM/PM STACKED ON RIGHT
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.width(48.dp)
                            ) {
                                AmPmButton(
                                    text = "AM",
                                    isSelected = isAm,
                                    onClick = { isAm = true }
                                )
                                AmPmButton(
                                    text = "PM",
                                    isSelected = !isAm,
                                    onClick = { isAm = false }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(1.dp)
                                .background(AppColorPalette.CyanCta.copy(alpha = 0.3f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                // REPEAT Section
                SectionHeader("REPEAT", interFamily)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                    dayNames.forEachIndexed { index, day ->
                        val isSelected = selectedDays.contains(index)
                        DayBox(
                            day = day,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) selectedDays.remove(index) else selectedDays.add(
                                    index
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ALARM MODE Section
                SectionHeader("ALARM MODE", interFamily)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Solo", "Duo", "Group").forEach { mode ->
                        ModeButton(
                            text = mode,
                            isSelected = selectedMode == mode,
                            onClick = {
                                selectedMode = mode
                                if (mode == "Duo") {
                                    showFindRivalsScreen = true
                                } else if (mode == "Group") {
                                    showAddParticipantsSheet = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Show selected participants badge if Duo or Group mode
                if (selectedMode != "Solo") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColorPalette.Surface)
                            .border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable {
                                if (selectedMode == "Duo") {
                                } else {
                                    showAddParticipantsSheet = true
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (selectedMode == "Duo") "Duo Partner (${selectedParticipants.size}/1)" else "Group Challenge (${selectedParticipants.size + 1}/8)",
                                color = AppColorPalette.CyanCta,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                            val statusMsg = when {
                                selectedMode == "Duo" && selectedParticipants.isEmpty() -> "Tap to search & add 1 partner"
                                selectedMode == "Duo" -> "@${selectedParticipants.first()}"
                                selectedMode == "Group" && selectedParticipants.size < 2 -> "Need min 3 total members (${2 - selectedParticipants.size} more)"
                                else -> "${selectedParticipants.size + 1} members added: " + selectedParticipants.joinToString { "@$it" }
                            }
                            Text(
                                text = statusMsg,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontFamily = interFamily
                            )
                        }
                        Text("➕", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bond/Group Name input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (selectedMode == "Duo") "BOND NAME" else "GROUP NAME",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = interFamily,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColorPalette.Surface)
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.TextField(
                                value = bondName,
                                onValueChange = { bondName = it },
                                placeholder = {
                                    Text(
                                        text = if (selectedMode == "Duo") "Name your duo bond (e.g. Dream Team)" else "Name your group (e.g. Rise & Grind)",
                                        color = Color.White.copy(alpha = 0.2f),
                                        fontFamily = interFamily,
                                        fontSize = 14.sp
                                    )
                                },
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = AppColorPalette.CyanCta,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CHALLENGE Section - GEN Z CYBER CARD SYSTEM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("CHALLENGE", interFamily)
                    Text(
                        text = "PICK 3 GAMES",
                        color = AppColorPalette.CyanCta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Gen Z Category Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All (12)",
                        "BRAIN" to "🧠 Brain",
                        "REFLEX" to "⚡ Reflex",
                        "BED" to "🛏️ In-Bed"
                    ).forEach { (catKey, label) ->
                        val isCatSel = selectedCategoryFilter == catKey
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(if (isCatSel) AppColorPalette.CyanCta.copy(alpha = 0.15f) else Color(0xFF0F1322))
                                .border(
                                    if (isCatSel) 1.5.dp else 1.dp,
                                    if (isCatSel) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(99.dp)
                                )
                                .clickable { selectedCategoryFilter = catKey }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isCatSel) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = interFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Game Picker — 12 games, 3 swipeable pages of 4; user picks exactly 3 (none pre-selected)
                val allGames = remember {
                    listOf(
                        GenZGameItem("Memory Chain", "Memory Chain", "🧠", "🧠 Simon Says", "BRAIN", "20s", AppColorPalette.CyanCta),
                        GenZGameItem("Number Sequence", "Number Sequence", "🧩", "🧩 Sequences", "BRAIN", "20s", Color(0xFFA855F7)),
                        GenZGameItem("Stroop", "Color Clash", "🎨", "🧠 Stroop Shock", "BRAIN", "15s", AppColorPalette.LossRed),
                        GenZGameItem("Target Tap", "Target Tap", "🎯", "🎯 Selective Focus", "BRAIN", "25s", AppColorPalette.GoldPremium),
                        GenZGameItem("Sliding Tiles", "Sliding Tiles", "🧱", "🧠 Tile Slide", "BRAIN", "25s", AppColorPalette.GoldPremium),
                        GenZGameItem("Number Hunt", "Number Hunt", "🧮", "🧠 Math Hunt", "BRAIN", "20s", Color(0xFFA855F7)),
                        GenZGameItem("Speed Tap", "Speed Tap 1–9", "🔢", "⚡ Ascending", "REFLEX", "20s", AppColorPalette.CyanCta),
                        GenZGameItem("Odd One Out", "Odd One Out", "🔍", "⚡ Search", "REFLEX", "20s", Color(0xFFA855F7)),
                        GenZGameItem("Reaction", "Reaction Rush", "⚡", "⚡ Go/No-Go", "REFLEX", "15s", AppColorPalette.LossRed),
                        GenZGameItem("Shake", "Shake Energy", "📱", "🛏️ Charge Bar", "BED", "10s", AppColorPalette.WinGreen),
                        GenZGameItem("Orb Focus", "Orb Focus", "🎯", "🛏️ Bed Touch", "BED", "15s", AppColorPalette.WinGreen),
                        GenZGameItem("Rapid Tap", "Thumb Sprint", "👆", "🛏️ Tap Sprint", "BED", "15s", AppColorPalette.CyanCta)
                    )
                }
                val filteredGames = remember(selectedCategoryFilter, allGames) {
                    if (selectedCategoryFilter == "ALL") allGames
                    else allGames.filter { it.category == selectedCategoryFilter }
                }

                // Selection counter
                val selCount = selectedGames.size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selCount < 3) "Pick ${3 - selCount} more game${if (3 - selCount == 1) "" else "s"} to continue"
                               else "Ladder ready — solve all 3 to dismiss! 🔥",
                        color = if (selCount < 3) AppColorPalette.GoldPremium else AppColorPalette.WinGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "$selCount / 3",
                        color = if (selCount == 3) AppColorPalette.WinGreen else AppColorPalette.CyanCta,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Swipeable pager — 4 games per page, 3 pages total (swipe left/right)
                val pagerGames = filteredGames.chunked(4)
                val pagerState = rememberPagerState(pageCount = { pagerGames.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSpacing = 12.dp
                ) { page ->
                    val pageGames = pagerGames.getOrElse(page) { emptyList() }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pageGames.chunked(2).forEach { rowGames ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowGames.forEach { game ->
                                    Box(modifier = Modifier.weight(1f).height(150.dp)) {
                                        val slot = selectedGames.indexOf(game.id)
                                        GenZGameCard(
                                            game = game,
                                            isSelected = slot != -1,
                                            selectionSlot = if (slot != -1) slot + 1 else null,
                                            onClick = {
                                                if (selectedGames.contains(game.id)) {
                                                    selectedGames.remove(game.id)
                                                } else if (selectedGames.size < 3) {
                                                    selectedGames.add(game.id)
                                                }
                                            },
                                            onDemoClick = { demoGameTarget = game },
                                            interFamily = interFamily
                                        )
                                    }
                                }
                                // Fill trailing space on odd-sized rows
                                repeat(2 - rowGames.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Page dots
                if (pagerGames.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerGames.size) { i ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (i == pagerState.currentPage) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == pagerState.currentPage) AppColorPalette.CyanCta
                                        else Color.White.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 🔊 ALARM SOUND & AUDIO ENGINE SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131724)),
                    border = BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "🔊", fontSize = 16.sp)
                                Text(
                                    text = "ALARM SOUNDSCAPE",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = titleFamily,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = activeSound.category,
                                color = AppColorPalette.CyanCta,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFamily,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppColorPalette.CyanCta.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = activeSound.emoji, fontSize = 22.sp)
                                Column {
                                    Text(
                                        text = activeSound.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = interFamily
                                    )
                                    Text(
                                        text = "🎛️ Pitch: ${activeSound.pitch}x · ⚡ Speed: ${activeSound.speed}x",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontFamily = interFamily
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColorPalette.CyanCta)
                                    .clickable { showAudioLibraryModal = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Change 🎧",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    }
                }

                if (showAudioLibraryModal) {
                    CyberAudioLibraryModal(
                        onDismiss = { showAudioLibraryModal = false },
                        onSelectSound = { sound ->
                            activeSound = sound
                            onSoundSelected(sound)
                        },
                        titleFamily = titleFamily,
                        interFamily = interFamily,
                        initialSound = activeSound
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PENALTY Section - MATCHING IMAGE STYLE
                PenaltyCard(
                    selectedPenalty = selectedPenalty,
                    onPenaltySelected = { selectedPenalty = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // SET ALARM Button - MATCHING IMAGE CYAN
                Button(
                    onClick = {
                        // Enforce Duo / Group participant limits
                        if (selectedMode == "Duo" && selectedParticipants.isEmpty()) {
                            showAddParticipantsSheet = true
                            return@Button
                        }
                        if (selectedMode == "Group" && selectedParticipants.size < 2) { // You + 2 friends = min 3
                            showAddParticipantsSheet = true
                            return@Button
                        }
                        // Enforce exactly 3 games selected
                        if (selectedGames.size != 3) {
                            return@Button
                        }

                        onSave(
                            selectedHour,
                            selectedMinute,
                            isAm,
                            selectedDays.toList(),
                            selectedMode,
                            selectedGames.joinToString(","),
                            selectedParticipants.joinToString(","),
                            if (selectedMode != "Solo" && bondName.isNotBlank()) bondName else null
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = AppColorPalette.CyanCta.copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColorPalette.CyanCta
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Set Alarm ⏰",
                        color = Color.Black,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Search Username Bottom Sheet for Duo & Group Challenges
        if (showAddParticipantsSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showAddParticipantsSheet = false },
                sheetState = sheetState,
                containerColor = AppColorPalette.Surface,
                contentColor = Color.White,
                dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
            ) {
                AddParticipantsSheetContent(
                    mode = selectedMode,
                    selectedParticipants = selectedParticipants,
                    onAddParticipant = { username ->
                        val maxAllowed = if (selectedMode == "Duo") 1 else 7
                        if (selectedParticipants.size < maxAllowed && !selectedParticipants.contains(username)) {
                            pendingParticipantUsername = username
                        }
                    },
                    onRemoveParticipant = { username ->
                        selectedParticipants.remove(username)
                    },
                    onDone = {
                        showAddParticipantsSheet = false
                    },
                    onSearchUsers = onSearchUsers,
                    titleFamily = titleFamily,
                    interFamily = interFamily
                )
            }
        }

        // ── FULL-SCREEN PRACTICE DEMO OVERLAY ─────────────────────────────────────
        // Renders on top of the entire SetAlarm screen when the user taps 🎮 Try.
        demoGameTarget?.let { game ->
            GameDemoScreen(
                onDismiss = { demoGameTarget = null },
                titleFamily = titleFamily,
                interFamily = interFamily,
                gameName = game.id
            )
        }
        } // end root Box overlay layer
    }
}
}

// -----------------------------------------------------------------------------
// USER SEARCH & PARTICIPANT MANAGEMENT BOTTOM SHEET
// -----------------------------------------------------------------------------
@Composable
private fun AddParticipantsSheetContent(
    mode: String,
    selectedParticipants: List<String>,
    onAddParticipant: (String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onDone: () -> Unit,
    onSearchUsers: (suspend (String) -> List<Friend>)? = null,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var searchQuery by remember { mutableStateOf("") }
    val maxPartners = if (mode == "Duo") 1 else 7
    val minRequiredTotal = if (mode == "Duo") 2 else 3

    val sampleUsers = remember {
        listOf(
            "maya.rises", "5amclub_lord", "nocturna_fox", "grind_rio",
            "alex_warrior", "sam_hustle", "viper_dawn", "zenith_rise",
            "early_bird_99", "titan_wake"
        )
    }

    var firebaseFoundUsers by remember { mutableStateOf<List<String>>(emptyList()) }

    // Live Firebase Firestore Username Query
    LaunchedEffect(searchQuery) {
        if (onSearchUsers != null) {
            val results = onSearchUsers(searchQuery)
            firebaseFoundUsers = results.map { it.name }
        }
    }

    val filteredUsers = remember(searchQuery, selectedParticipants, firebaseFoundUsers) {
        if (firebaseFoundUsers.isNotEmpty()) {
            firebaseFoundUsers.filter { !selectedParticipants.contains(it) }
        } else {
            sampleUsers.filter { user ->
                user.contains(searchQuery, ignoreCase = true) && !selectedParticipants.contains(user)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Info Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (mode == "Duo") "Add Duo Partner ⚔️" else "Build Challenge Group ⚡",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily
                )
                Text(
                    text = if (mode == "Duo") "Duo Limit: Exactly 2 Members (You + 1 Partner)" else "Group Limit: Min 3, Max 8 Members",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = interFamily
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColorPalette.CyanCta.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${selectedParticipants.size + 1}/${maxPartners + 1}",
                    color = AppColorPalette.CyanCta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Active Added Members Chips
        if (selectedParticipants.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedParticipants.forEach { partner ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColorPalette.DeepSurface)
                            .border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("@$partner", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Text(
                                "✕",
                                color = AppColorPalette.LossRed,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onRemoveParticipant(partner) }
                            )
                        }
                    }
                }
            }
        }

        // Search Input Box (Single Unique Username)
        androidx.compose.material3.OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Type unique username (e.g. maya.rises)...", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColorPalette.CyanCta,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedContainerColor = AppColorPalette.DeepSurface,
                unfocusedContainerColor = AppColorPalette.DeepSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Custom Add Button if typing a new unique username not in sample list
        if (searchQuery.isNotBlank() && !selectedParticipants.contains(searchQuery.trim())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColorPalette.CyanCta.copy(alpha = 0.1f))
                    .border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable {
                        onAddParticipant(searchQuery.trim())
                        searchQuery = ""
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Add user @${searchQuery.trim()}", color = AppColorPalette.CyanCta, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                Text("➕ Add", color = AppColorPalette.CyanCta, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }

        // Suggested Unique Users List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredUsers.size) { idx ->
                val user = filteredUsers[idx]
                val canAdd = selectedParticipants.size < maxPartners

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1322))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("👤", fontSize = 18.sp)
                        Column {
                            Text("@$user", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Text("Unique ID · Ready for $mode", color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp)
                        }
                    }

                    if (canAdd) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColorPalette.CyanCta)
                                .clickable { onAddParticipant(user) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Add", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Confirm Button with Validation Rule (Min 2 for Duo, Min 3 for Group)
        val currentTotal = selectedParticipants.size + 1
        val isValid = if (mode == "Duo") currentTotal == 2 else currentTotal >= 3

        Button(
            onClick = onDone,
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValid) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.1f),
                disabledContainerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Text(
                text = if (isValid) "Confirm $mode Setup (${currentTotal} members)" else "Need min $minRequiredTotal members to continue",
                color = if (isValid) Color.Black else Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = interFamily
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Suppress("FrequentlyChangingValue")
@Composable
fun WheelTimePicker(
    items: List<String>,
    initialIndex: Int,
    onItemSelected: (String) -> Unit,
    titleFamily: FontFamily,
    modifier: Modifier = Modifier,
    itemHeight: androidx.compose.ui.unit.Dp = 44.dp // 44dp item height per slot
) {
    val loopOffset = items.size * 100000
    val startIndex = loopOffset + (initialIndex - 1) // -1 so center item is at index+1

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val itemHeightPx = itemHeight.value * density.density

    val centerIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex + 1 }
    }

    LaunchedEffect(centerIndex) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val finalIndex = listState.firstVisibleItemIndex + 1
            onItemSelected(items[finalIndex % items.size])
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * 3),
            contentPadding = PaddingValues(vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(Int.MAX_VALUE) { index ->
                val actualIndex = index % items.size
                val item = items[actualIndex]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    // Calculate exact scroll distance from middle slot (index == firstVisibleItemIndex + 1)
                    val scrollOffset = if (itemHeightPx > 0) listState.firstVisibleItemScrollOffset / itemHeightPx else 0f
                    val diff = index - listState.firstVisibleItemIndex - scrollOffset
                    val distFromCenter = abs(diff - 1f)
                    val progress = (1f - distFromCenter).coerceIn(0f, 1f)

                    // Perfectly symmetric scaling for top & bottom adjacent numbers
                    val fontSize = (14 + 30 * progress).sp
                    val alpha = 0.25f + 0.75f * progress

                    Text(
                        text = item,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = fontSize,
                        fontWeight = if (progress > 0.5f) FontWeight.W900 else FontWeight.W400,
                        fontFamily = titleFamily,
                        letterSpacing = (-1.5).sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberSnapFlingBehavior(lazyListState: androidx.compose.foundation.lazy.LazyListState): androidx.compose.foundation.gestures.FlingBehavior {
    return androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = lazyListState)
}

@Composable
fun AmPmButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.12f) else AppColorPalette.Surface,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AppColorPalette.CyanCta else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.25f),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = Modifier
            .width(48.dp) // Sleeker width to match zoomed image
            .height(32.dp) // Sleeker height to match zoomed image
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp // Slightly smaller text to fit size
        )
    }
}

@Composable
fun SectionHeader(text: String, fontFamily: FontFamily) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = fontFamily,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun DayBox(day: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 2.dp) // Slightly tighter box spacing (reduces width by 2dp per side)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.12f) else Color(0xFF0F1322))
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, AppColorPalette.CyanCta, RoundedCornerShape(10.dp))
                } else {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(10.dp)
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            day,
            color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.35f),
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emoji = when (text) {
        "Solo" -> "👤"
        "Duo" -> "⚔️"
        "Group" -> "⚡"
        else -> ""
    }

    Box(
        modifier = modifier
            .height(42.dp) // Reduced height by 2dp (from 44dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.12f) else Color(0xFF0F1322))
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, AppColorPalette.CyanCta, RoundedCornerShape(14.dp))
                } else {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(14.dp)
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (emoji.isNotEmpty()) {
                Text(
                    text = emoji,
                    fontSize = 13.sp
                )
            }
            Text(
                text = text,
                color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }
    }
}

data class GenZGameItem(
    val id: String,
    val name: String,
    val emoji: String,
    val tag: String,
    val category: String,
    val estTime: String,
    val accentColor: Color
)

@Composable
fun GenZGameCard(
    game: GenZGameItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDemoClick: () -> Unit,
    interFamily: FontFamily,
    selectionSlot: Int? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) game.accentColor.copy(alpha = 0.15f)
                else Color(0xFF0F1322)
            )
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) game.accentColor else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: slot badge (or spacer)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(game.accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        // Slot number = order in the 3-game ladder
                        Text(
                            text = selectionSlot?.toString() ?: "✓",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Middle: emoji + compact title
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = game.emoji, fontSize = 22.sp)
                Text(
                    text = game.name,
                    color = if (isSelected) game.accentColor else Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.W800,
                    fontFamily = interFamily,
                    maxLines = 2,
                    lineHeight = 10.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Bottom: Try button (full-width pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(game.accentColor.copy(alpha = 0.18f))
                    .border(1.dp, game.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onDemoClick() }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎮 Try",
                    color = game.accentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }
        }
    }
}



@Composable
fun ChallengeChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.1f) else Color(0xFF0F1322))
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, AppColorPalette.CyanCta, RoundedCornerShape(99.dp))
                } else {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(99.dp)
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SoundCard(sound: SoundMetadata, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(135.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.1f) else Color(0xFF0F1322))
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.15f)
                        else Color.White.copy(alpha = 0.04f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                    contentDescription = null,
                    tint = if (isSelected) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(14.dp)
                )
            }
            Column {
                Text(
                    text = sound.name,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = sound.category,
                    color = if (isSelected) AppColorPalette.CyanCta.copy(alpha = 0.8f) else Color.White.copy(
                        alpha = 0.2f
                    ),
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun PenaltyCard(selectedPenalty: String, onPenaltySelected: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161217)),
        border = BorderStroke(1.dp, AppColorPalette.LossRed.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "IF YOU FAIL...",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            PenaltyItem(
                text = "Post your shame to the feed 💀",
                active = selectedPenalty == "shame",
                onClick = { onPenaltySelected("shame") }
            )
            PenaltyItem(
                text = "20 pushups (photo proof) 💪",
                active = selectedPenalty == "pushups",
                onClick = { onPenaltySelected("pushups") }
            )
            PenaltyItem(
                text = "Streak freezes (Premium) 🛡️",
                active = selectedPenalty == "freeze",
                onClick = { onPenaltySelected("freeze") }
            )
        }
    }
}

@Composable
fun PenaltyItem(text: String, active: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.LossRed)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            )
        }
        Text(
            text = text,
            color = if (active) Color.White else Color.White.copy(alpha = 0.35f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow
    ) {
        content()
    }
}
