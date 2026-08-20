package com.social.wakesync.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.social.wakesync.ui.theme.AppColorPalette
import com.social.wakesync.ui.utils.BackHandler

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmScreen(
    onBack: () -> Unit,
    onSave: (hour: Int, minute: Int, isAm: Boolean, days: List<Int>, mode: String, challenge: String, partnerUsername: String?) -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily,
    sounds: List<SoundMetadata> = emptyList(),
    selectedSound: SoundMetadata? = null,
    onSoundSelected: (SoundMetadata) -> Unit = {},
    onSearchUsers: (suspend (String) -> List<Friend>)? = null
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()
    var isAm by remember { mutableStateOf(true) }
    var selectedHour by remember { mutableIntStateOf(6) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var selectedMode by remember { mutableStateOf("Solo") }
    var selectedChallenge by remember { mutableStateOf("Math") }
    var selectedMathDifficulty by remember { mutableStateOf("Medium") }
    val selectedDays = remember { mutableStateListOf(0, 1, 2, 3, 4) }
    var selectedPenalty by remember { mutableStateOf("shame") }

    // Duo / Group User Search Bottom Sheet State
    var showAddParticipantsSheet by remember { mutableStateOf(false) }
    val selectedParticipants = remember { mutableStateListOf<String>() }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()

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
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
                Spacer(modifier = Modifier.height(24.dp))
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

                Spacer(modifier = Modifier.height(24.dp))

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
                                if (mode == "Duo" || mode == "Group") {
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
                            .clickable { showAddParticipantsSheet = true }
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ALARM SOUND Section
                SectionHeader("ALARM SOUND", interFamily)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sounds) { sound ->
                        SoundCard(
                            sound = sound,
                            isSelected = selectedSound?.id == sound.id,
                            onClick = { onSoundSelected(sound) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CHALLENGE Section
                SectionHeader("CHALLENGE", interFamily)
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "Math", "Memory", "Stroop", "Word Scramble", "Shake",
                        "Speed Tap", "Odd One Out", "Sliding Tiles", "Orb Focus", "Rapid Tap"
                    ).forEach { challenge ->
                        ChallengeChip(
                            text = challenge,
                            isSelected = selectedChallenge == challenge,
                            onClick = { selectedChallenge = challenge }
                        )
                    }
                }

                // ONLY Math displays the Level 1, Level 2, Level 3 sub-selector bar!
                if (selectedChallenge == "Math") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F1322))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Level 1" to "1 Qs",
                            "Level 2" to "2 Qs",
                            "Level 3" to "3 Qs"
                        ).forEachIndexed { idx, (lvl, desc) ->
                            val levelNum = idx + 1
                            val isSel = (selectedMathDifficulty == "Easy" && levelNum == 1) ||
                                        (selectedMathDifficulty == "Medium" && levelNum == 2) ||
                                        (selectedMathDifficulty == "Hard" && levelNum == 3)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) AppColorPalette.CyanCta.copy(alpha = 0.15f) else Color.Transparent)
                                    .then(if (isSel) Modifier.border(1.dp, AppColorPalette.CyanCta, RoundedCornerShape(10.dp)) else Modifier)
                                    .clickable {
                                        selectedMathDifficulty = when (levelNum) {
                                            1 -> "Easy"
                                            3 -> "Hard"
                                            else -> "Medium"
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = lvl,
                                        color = if (isSel) AppColorPalette.CyanCta else Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = interFamily
                                    )
                                    Text(
                                        text = desc,
                                        color = if (isSel) AppColorPalette.CyanCta.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.35f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = interFamily
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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

                        onSave(
                            selectedHour,
                            selectedMinute,
                            isAm,
                            selectedDays.toList(),
                            selectedMode,
                            selectedChallenge,
                            selectedParticipants.joinToString(",")
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
                        fontSize = 17.sp,
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
                        val maxAllowed = if (selectedMode == "Duo") 1 else 7 // You + 1 for Duo = 2 max, You + 7 for Group = 8 max
                        if (selectedParticipants.size < maxAllowed && !selectedParticipants.contains(username)) {
                            selectedParticipants.add(username)
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
