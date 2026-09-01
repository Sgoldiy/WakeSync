package com.social.wakesync.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class DuoPuzzleType(val displayName: String, val emoji: String) {
    SPEED_MATH("Duo Math Race", "🧮"),
    PATTERN_MEMORY("Memory Battle", "🧩"),
    COLOR_CLASH("Stroop Clash", "🎨"),
    WORD_RACE("Word Unscramble", "🔤"),
    RAPID_TAP("Rapid Tap Sprint", "👆")
}

@Composable
fun AlarmPuzzleDuo(
    onDismiss: () -> Unit,
    onFailure: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
    modifier: Modifier = Modifier,
    userName: String = "You",
    userAvatar: String = "🤯",
    rivalName: String = "Partner",
    rivalAvatar: String = "🦁",
    alarmId: String? = null,
    currentUserId: String? = null,
    onListenToDuoAlarm: ((String) -> kotlinx.coroutines.flow.Flow<String?>)? = null,
    onSetDuoAlarmWinner: ((String, String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isUserDone by remember { mutableStateOf(false) }
    var isRivalDone by remember { mutableStateOf(false) }

    // Map the selected alarm challenge to the corresponding Duo puzzle type
    val duoPuzzleType = remember {
        when (AlarmState.activeAlarmChallenge) {
            "Math" -> DuoPuzzleType.SPEED_MATH
            "Memory" -> DuoPuzzleType.PATTERN_MEMORY
            "Stroop" -> DuoPuzzleType.COLOR_CLASH
            "Word Scramble" -> DuoPuzzleType.WORD_RACE
            else -> DuoPuzzleType.RAPID_TAP
        }
    }

    val seed = remember(alarmId) {
        alarmId?.hashCode() ?: Random.nextInt()
    }

    var timeLeft by remember { mutableIntStateOf(120) }

    // 120-Second High-Stakes Timeout Logic
    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isUserDone && !isRivalDone) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0 && !isUserDone && !isRivalDone) {
            // Time out! Alarm stops, both lose 3 streaks
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFailure()
        }
    }

    // Real-Time Firestore Sync Listener: Stops alarm when any partner solves it
    LaunchedEffect(alarmId) {
        if (alarmId != null && onListenToDuoAlarm != null && currentUserId != null) {
            onListenToDuoAlarm(alarmId).collect { winnerUid ->
                if (!winnerUid.isNullOrBlank()) {
                    if (winnerUid == currentUserId) {
                        isUserDone = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(800)
                        onDismiss()
                    } else {
                        isRivalDone = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(800)
                        onFailure()
                    }
                }
            }
        }
    }

    // Failsafe backup timer: runs in parallel to prevent hang when offline
    val rivalSolveTime = remember { Random.nextLong(20000, 30000) }
    LaunchedEffect(Unit) {
        delay(rivalSolveTime)
        if (!isUserDone && !isRivalDone) {
            isRivalDone = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1000)
            onFailure()
        }
    }

    // User Solved First!
    val onUserSolveFirst = {
        if (!isRivalDone && !isUserDone) {
            if (alarmId != null && onSetDuoAlarmWinner != null && currentUserId != null) {
                onSetDuoAlarmWinner(alarmId, currentUserId)
            } else {
                // Local Fallback
                isUserDone = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Real-Time Split VS Screen Header
        // Real-Time Header (Group Battle vs Duo Split VS)
        if (AlarmState.activeAlarmMode == "Group") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF041E2B), Color(0xFF020617)))),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚡ GROUP ALARM BATTLE CLASH ⚡",
                        color = AppColorPalette.CyanCta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily,
                        letterSpacing = 1.5.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // YOU
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.5.dp, if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(userAvatar, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("You", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Text(
                                text = if (isUserDone) "WON! 🎉" else "Solving...",
                                color = if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = interFamily
                            )
                        }

                        // Partners (Competitors)
                        val partnerNames = remember(AlarmState.activeAlarmPartnerUsername) {
                            AlarmState.activeAlarmPartnerUsername?.split(",")?.filter { it.isNotBlank() } ?: listOf("Partner 1", "Partner 2")
                        }

                        partnerNames.take(4).forEachIndexed { idx, name ->
                            val isThisPartnerWinner = isRivalDone
                            val competitorColor = if (isThisPartnerWinner) AppColorPalette.WinGreen else AppColorPalette.LossRed
                            val competitorAvatar = when (idx % 4) {
                                0 -> "🦁"
                                1 -> "🐺"
                                2 -> "🦊"
                                else -> "🐻"
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.5.dp, competitorColor, CircleShape)
                                        .background(Color.White.copy(alpha = 0.04f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(competitorAvatar, fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (name.length > 8) name.take(6) + ".." else name,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = interFamily
                                )
                                Text(
                                    text = if (isThisPartnerWinner) "WON! 💀" else "Solving...",
                                    color = competitorColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFamily
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT SIDE - YOU
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF041E2B), Color(0xFF061521)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("YOU", color = AppColorPalette.CyanCta, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = interFamily, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(userAvatar, fontSize = 32.sp)
                            }
                            Text(userName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (isUserDone) AppColorPalette.WinGreen.copy(alpha = 0.15f) else AppColorPalette.CyanCta.copy(alpha = 0.15f))
                                    .border(1.dp, if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, RoundedCornerShape(99.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isUserDone) "WON! 🎉" else "Solving...", color = if (isUserDone) AppColorPalette.WinGreen else AppColorPalette.CyanCta, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            }
                        }
                    }

                    // RIGHT SIDE - RIVAL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF1C0D16), Color(0xFF260D1A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("RIVAL", color = AppColorPalette.LossRed, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = interFamily, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, if (isRivalDone) AppColorPalette.WinGreen else AppColorPalette.LossRed, CircleShape)
                                    .background(Color.White.copy(alpha = 0.04f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rivalAvatar, fontSize = 32.sp)
                            }
                            Text(rivalName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(if (isRivalDone) AppColorPalette.WinGreen.copy(alpha = 0.15f) else AppColorPalette.LossRed.copy(alpha = 0.15f))
                                    .border(1.dp, if (isRivalDone) AppColorPalette.WinGreen else AppColorPalette.LossRed, RoundedCornerShape(99.dp))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isRivalDone) "WON! 💀" else "Solving...", color = if (isRivalDone) AppColorPalette.WinGreen else AppColorPalette.LossRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                            }
                        }
                    }
                }

                // Central VS Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppColorPalette.VoidBg)
                        .border(2.dp, AppColorPalette.CyanCta, CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", color = AppColorPalette.CyanCta, fontSize = 13.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
                }
            }
        }

        // Pressure Copy Text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Don't let them win again. 💀",
                color = AppColorPalette.LossRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFamily
            )
            Text(
                text = "${rivalName} beat you 3 times last week",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                fontFamily = interFamily
            )
        }

        // Active Duo Challenge Title & 120s Countdown Timer Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(duoPuzzleType.emoji, fontSize = 16.sp)
                Text(
                    text = "${duoPuzzleType.displayName} · Solve First!",
                    color = AppColorPalette.CyanCta,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFamily
                )
            }

            val badgeColor = if (timeLeft <= 30) AppColorPalette.LossRed else AppColorPalette.CyanCta
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⏱️ ${timeLeft}s",
                    color = badgeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = interFamily
                )
            }
        }

        // Interactive Duo Challenge Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = duoPuzzleType,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "duo_task_transition"
            ) { puzzle ->
                when (puzzle) {
                    DuoPuzzleType.SPEED_MATH -> DuoMathComponent(onSolve = onUserSolveFirst, seed = seed, titleFamily = titleFamily, interFamily = interFamily)
                    DuoPuzzleType.PATTERN_MEMORY -> DuoMemoryComponent(onSolve = onUserSolveFirst, seed = seed, titleFamily = titleFamily, interFamily = interFamily)
                    DuoPuzzleType.COLOR_CLASH -> DuoStroopComponent(onSolve = onUserSolveFirst, seed = seed, titleFamily = titleFamily, interFamily = interFamily)
                    DuoPuzzleType.WORD_RACE -> DuoWordUnscrambleComponent(onSolve = onUserSolveFirst, seed = seed, titleFamily = titleFamily, interFamily = interFamily)
                    DuoPuzzleType.RAPID_TAP -> DuoRapidTapComponent(onSolve = onUserSolveFirst, seed = seed, titleFamily = titleFamily, interFamily = interFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 1. DUO SPEED MATH CHALLENGE
// -----------------------------------------------------------------------------
@Composable
private fun DuoMathComponent(onSolve: () -> Unit, seed: Int, titleFamily: FontFamily, interFamily: FontFamily) {
    val a = remember(seed) { (14..48).random(kotlin.random.Random(seed)) }
    val b = remember(seed) { (12..48).random(kotlin.random.Random(seed + 1)) }
    val answer = a + b
    var currentInput by remember { mutableStateOf("") }

    LaunchedEffect(currentInput) {
        if (currentInput == answer.toString()) {
            delay(100)
            onSolve()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().height(76.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
            border = BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("$a + $b = ${currentInput.ifEmpty { "?" }}", color = AppColorPalette.CyanCta, fontSize = 30.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
            }
        }

        DuoNumpad(
            onNumberClick = { num -> if (currentInput.length < 4) currentInput += num },
            onDeleteClick = { if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) },
            titleFamily = titleFamily
        )
    }
}

// -----------------------------------------------------------------------------
// 2. DUO PATTERN MEMORY CHALLENGE
// -----------------------------------------------------------------------------
@Composable
private fun DuoMemoryComponent(onSolve: () -> Unit, seed: Int, titleFamily: FontFamily, interFamily: FontFamily) {
    val targetSequence = remember(seed) { (0..8).shuffled(kotlin.random.Random(seed)).take(4) }
    val userSequence = remember { mutableStateListOf<Int>() }
    var isShowing by remember { mutableStateOf(true) }
    var activeFlashTile by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        delay(300)
        for (tile in targetSequence) {
            activeFlashTile = tile
            delay(400)
            activeFlashTile = -1
            delay(150)
        }
        isShowing = false
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(if (isShowing) "Memorize Pattern..." else "Tap sequence in order!", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontFamily = interFamily)

        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.size(220.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(9) { idx ->
                val isHighlighted = activeFlashTile == idx || userSequence.contains(idx)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isHighlighted) AppColorPalette.CyanCta else AppColorPalette.Surface)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable(enabled = !isShowing) {
                            userSequence.add(idx)
                            val step = userSequence.size - 1
                            if (userSequence[step] != targetSequence[step]) {
                                userSequence.clear()
                            } else if (userSequence.size == targetSequence.size) {
                                onSolve()
                            }
                        }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. DUO COLOR CLASH CHALLENGE
// -----------------------------------------------------------------------------
@Composable
private fun DuoStroopComponent(onSolve: () -> Unit, seed: Int, titleFamily: FontFamily, interFamily: FontFamily) {
    val colors = listOf(
        Pair("RED", AppColorPalette.LossRed),
        Pair("CYAN", AppColorPalette.CyanCta),
        Pair("GREEN", AppColorPalette.WinGreen),
        Pair("GOLD", AppColorPalette.GoldPremium)
    )
    val targetPair = remember(seed) { colors.random(kotlin.random.Random(seed)) }
    val displayColor = remember(seed) { colors.filter { it != targetPair }.random(kotlin.random.Random(seed + 1)).second }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tap the FONT COLOR (not written word)", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontFamily = interFamily)
        Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)).background(AppColorPalette.Surface), contentAlignment = Alignment.Center) {
            Text(targetPair.first, color = displayColor, fontSize = 36.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
        }
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(colors.size) { idx ->
                val colorOption = colors[idx]
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorOption.second.copy(alpha = 0.15f))
                        .border(1.5.dp, colorOption.second, RoundedCornerShape(12.dp))
                        .clickable { if (colorOption.second == displayColor) onSolve() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(colorOption.first, color = colorOption.second, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. DUO WORD RACE CHALLENGE
// -----------------------------------------------------------------------------
@Composable
private fun DuoWordUnscrambleComponent(onSolve: () -> Unit, seed: Int, titleFamily: FontFamily, interFamily: FontFamily) {
    val wordList = listOf("WAKE", "RISE", "POWER", "ALERT")
    val originalWord = remember(seed) { wordList.random(kotlin.random.Random(seed)) }
    val scrambled = remember(seed, originalWord) { originalWord.toList().shuffled(kotlin.random.Random(seed + 1)).joinToString("") }
    var currentInput by remember { mutableStateOf("") }

    LaunchedEffect(currentInput) {
        if (currentInput.equals(originalWord, ignoreCase = true)) {
            delay(100)
            onSolve()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            scrambled.forEach { char ->
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(AppColorPalette.DeepSurface), contentAlignment = Alignment.Center) {
                    Text(char.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = titleFamily)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).background(AppColorPalette.Surface).border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Text(currentInput.ifEmpty { "Tap letters below" }, color = if (currentInput.isEmpty()) Color.White.copy(alpha = 0.3f) else AppColorPalette.CyanCta, fontSize = 20.sp, fontWeight = FontWeight.W800, fontFamily = titleFamily)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            scrambled.toList().shuffled(kotlin.random.Random(seed + 2)).forEach { letter ->
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.08f)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)).clickable {
                        if (currentInput.length < originalWord.length) currentInput += letter
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(letter.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. DUO RAPID TAP SPRINT CHALLENGE
// -----------------------------------------------------------------------------
@Composable
private fun DuoRapidTapComponent(onSolve: () -> Unit, seed: Int, titleFamily: FontFamily, interFamily: FontFamily) {
    var tapCount by remember { mutableIntStateOf(0) }
    val targetTaps = 12

    LaunchedEffect(tapCount) {
        if (tapCount >= targetTaps) onSolve()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(AppColorPalette.WinGreen.copy(alpha = 0.15f))
                .border(2.dp, AppColorPalette.WinGreen, CircleShape)
                .clickable { tapCount++ },
            contentAlignment = Alignment.Center
        ) {
            Text("👆", fontSize = 54.sp)
        }

        Text("Progress: $tapCount / $targetTaps Taps", color = AppColorPalette.WinGreen, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
    }
}

// Helper Numpad for Duo Math
@Composable
private fun DuoNumpad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit, titleFamily: FontFamily) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "DEL"))
        for (row in rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth(0.85f)) {
                for (item in row) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (item.isEmpty()) Color.Transparent else AppColorPalette.DeepSurface)
                            .then(if (item.isNotEmpty()) Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)) else Modifier)
                            .clickable(enabled = item.isNotEmpty()) {
                                if (item == "DEL") onDeleteClick() else onNumberClick(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (item == "DEL") {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        } else {
                            Text(item, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.W700, fontFamily = titleFamily)
                        }
                    }
                }
            }
        }
    }
}
