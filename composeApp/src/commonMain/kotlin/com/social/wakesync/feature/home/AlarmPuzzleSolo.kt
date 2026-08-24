package com.social.wakesync.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.scale
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

enum class SoloPuzzleType(val displayName: String, val emoji: String) {
    MATH("Speed Math", "🧮"),            // 1. Math Game (Only game with 3 Levels: Easy=1 Q, Medium=2 Qs, Hard=3 Qs)
    MEMORY("Pattern Memory", "🧩"),        // 2. 3x3 Flashing Pattern Recall
    STROOP("Color Clash", "🎨"),           // 3. Stroop Color Conflict Test
    WORD_UNSCRAMBLE("Word Scramble", "🔤"), // 4. Anagram Unscramble
    SHAKE("Shake Energy", "📱"),          // 5. Rapid Motion Energy Bar
    NUMBER_ORDER("Speed Tap 1-6", "🔢"),   // 6. 1-to-6 Ascending Tap
    ODD_ONE_OUT("Odd One Out", "🔍"),      // 7. Visual Intruder Search
    SLIDING_TILE("Sliding Tiles", "🧱"),    // 8. 1-2-3 Tile Sequence Arrange
    BALANCE_MAZE("Orb Focus", "🎯"),        // 9. Bed-friendly Orb Center Touch Focus
    BED_TAP("Rapid Bed Tap", "👆")         // 10. Bed-friendly Finger Tap Sprint (Replaced Physical Squat)
}

@Composable
fun AlarmPuzzleSolo(
    onDismiss: () -> Unit,
    onFailure: () -> Unit = {},
    titleFamily: FontFamily,
    interFamily: FontFamily,
    challengeName: String = "Math",
    mathDifficulty: String = "Medium",
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var timeString by remember { mutableStateOf("06:30") }
    var currentStage by remember { mutableIntStateOf(1) }

    // 60-Second Timer Rules & Auto-Change Mechanism:
    // Attempt 1 (60s countdown) -> If time expires, automatically change task to a 2nd random task!
    // Attempt 2 (60s countdown) -> If 2nd attempt also expires, user FAILS & LOSES 3 STREAKS!
    // Successful Completion -> User WINS & GAINS 1 STREAK!
    var attemptNumber by remember { mutableIntStateOf(1) } // 1 or 2
    var secondsLeft by remember { mutableIntStateOf(60) }
    var activePuzzleType by remember {
        mutableStateOf(getPuzzleTypeFromName(challengeName))
    }

    val totalStages = remember(activePuzzleType, mathDifficulty) {
        if (activePuzzleType == SoloPuzzleType.MATH) {
            when (mathDifficulty) {
                "Easy" -> 1
                "Hard" -> 3
                else -> 2
            }
        } else {
            1
        }
    }

    // Dynamic Clock Updater
    LaunchedEffect(Unit) {
        while (true) {
            val nowTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val rawHour = nowTime.hour
            val hour = if (rawHour == 0) 12 else if (rawHour > 12) rawHour - 12 else rawHour
            val min = nowTime.minute.toString().padStart(2, '0')
            timeString = "$hour:$min"
            delay(1000)
        }
    }

    // 60-Second Countdown Timer Loop
    LaunchedEffect(attemptNumber, activePuzzleType, currentStage) {
        secondsLeft = 60
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }

        // Timer Expired!
        if (attemptNumber == 1) {
            // Attempt 1 Failed -> Auto-change task and give Attempt 2 (60s)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            attemptNumber = 2
            currentStage = 1
            val availablePuzzles = SoloPuzzleType.values().filter { it != activePuzzleType }
            activePuzzleType = availablePuzzles.random()
        } else {
            // Attempt 2 Failed -> User fails alarm, loses 3 streaks & triggers Broken Streak screen!
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFailure()
        }
    }

    val onStageSuccess = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (currentStage >= totalStages) {
            // Success! Gain 1 streak & dismiss alarm
            onDismiss()
        } else {
            currentStage++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.VoidBg)
            .padding(horizontal = 20.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Clock & Attempt Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily,
                    letterSpacing = (-2).sp
                )
                
                // 60s Countdown Timer Badge & Attempt Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (secondsLeft <= 10) AppColorPalette.LossRed.copy(alpha = 0.2f) else AppColorPalette.Surface)
                        .border(1.dp, if (secondsLeft <= 10) AppColorPalette.LossRed else AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⏱️ ${secondsLeft}s",
                        color = if (secondsLeft <= 10) AppColorPalette.LossRed else AppColorPalette.CyanCta,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = interFamily
                    )
                    Text(
                        text = "· Attempt $attemptNumber of 2",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = interFamily
                    )
                }
            }

            // Task Name Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = activePuzzleType.emoji, fontSize = 16.sp)
                Text(
                    text = if (activePuzzleType == SoloPuzzleType.MATH) 
                        "${activePuzzleType.displayName} · Question $currentStage of $totalStages"
                    else 
                        "${activePuzzleType.displayName} Challenge",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = interFamily
                )
            }

            // Progress Bar (Only for multi-question Math)
            if (activePuzzleType == SoloPuzzleType.MATH && totalStages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (stage in 1..totalStages) {
                        val isDone = stage < currentStage
                        val isCurrent = stage == currentStage
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    when {
                                        isDone -> AppColorPalette.WinGreen
                                        isCurrent -> AppColorPalette.CyanCta
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }

            // Active Bed-Friendly Task Component
            AnimatedContent(
                targetState = Pair(activePuzzleType, currentStage),
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "task_transition"
            ) { (_, stage) ->
                when (activePuzzleType) {
                    SoloPuzzleType.MATH -> SoloMathComponent(stage = stage, onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.MEMORY -> SoloMemoryComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.STROOP -> SoloStroopComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.WORD_UNSCRAMBLE -> SoloWordUnscrambleComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.SHAKE -> SoloShakeComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.NUMBER_ORDER -> SoloNumberOrderComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.ODD_ONE_OUT -> SoloOddOneOutComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.SLIDING_TILE -> SoloSlidingTileComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.BALANCE_MAZE -> SoloBalanceMazeComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                    SoloPuzzleType.BED_TAP -> SoloBedTapComponent(onSuccess = { onStageSuccess() }, titleFamily = titleFamily, interFamily = interFamily)
                }
            }
        }
    }
}

private fun getPuzzleTypeFromName(name: String): SoloPuzzleType {
    return when (name) {
        "Memory" -> SoloPuzzleType.MEMORY
        "Stroop" -> SoloPuzzleType.STROOP
        "Word Scramble" -> SoloPuzzleType.WORD_UNSCRAMBLE
        "Shake" -> SoloPuzzleType.SHAKE
        "Speed Tap" -> SoloPuzzleType.NUMBER_ORDER
        "Odd One Out" -> SoloPuzzleType.ODD_ONE_OUT
        "Sliding Tiles" -> SoloPuzzleType.SLIDING_TILE
        "Orb Focus" -> SoloPuzzleType.BALANCE_MAZE
        "Rapid Tap" -> SoloPuzzleType.BED_TAP
        else -> SoloPuzzleType.MATH
    }
}

// -----------------------------------------------------------------------------
// 1. MATH TASK (3 Levels: Easy=1 Q, Medium=2 Qs, Hard=3 Qs)
// -----------------------------------------------------------------------------
@Composable
private fun SoloMathComponent(
    stage: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val puzzle = remember(stage) { generateStageSoloPuzzle(stage) }
    var currentInput by remember { mutableStateOf("") }

    LaunchedEffect(currentInput) {
        if (currentInput == puzzle.answer.toString()) {
            delay(150)
            onSuccess()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(84.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface),
            border = BorderStroke(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "${puzzle.question} = ${currentInput.ifEmpty { "?" }}",
                    color = AppColorPalette.CyanCta,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily
                )
            }
        }

        SoloNumpad(
            onNumberClick = { num -> if (currentInput.length < 5) currentInput += num },
            onDeleteClick = { if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1) },
            titleFamily = titleFamily
        )
    }
}

// -----------------------------------------------------------------------------
// 2. PATTERN MEMORY TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloMemoryComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val gridSize = 9 // 3x3
    val targetSequence = remember { (0 until gridSize).shuffled().take(4) }
    val userSequence = remember { mutableStateListOf<Int>() }
    var isShowingPattern by remember { mutableStateOf(true) }
    var activeFlashTile by remember { mutableIntStateOf(-1) }
    var isErrorFlash by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isShowingPattern = true
        userSequence.clear()
        delay(300)
        for (tile in targetSequence) {
            activeFlashTile = tile
            delay(450)
            activeFlashTile = -1
            delay(180)
        }
        isShowingPattern = false
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = when {
                isErrorFlash -> "❌ Pattern Missed! Resetting..."
                isShowingPattern -> "Watch & Memorize Sequence..."
                else -> "Tap the 4 tiles in order!"
            },
            color = when {
                isErrorFlash -> AppColorPalette.LossRed
                isShowingPattern -> Color.White.copy(alpha = 0.6f)
                else -> AppColorPalette.CyanCta
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = interFamily
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(230.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(gridSize) { index ->
                val isHighlighted = activeFlashTile == index || userSequence.contains(index)
                val color by animateColorAsState(
                    targetValue = when {
                        isErrorFlash -> AppColorPalette.LossRed.copy(alpha = 0.4f)
                        isHighlighted -> AppColorPalette.CyanCta
                        else -> AppColorPalette.Surface
                    },
                    animationSpec = tween(150)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(color)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .clickable(enabled = !isShowingPattern && !isErrorFlash) {
                            userSequence.add(index)
                            val step = userSequence.size - 1
                            if (userSequence[step] != targetSequence[step]) {
                                isErrorFlash = true
                                userSequence.clear()
                                isShowingPattern = true
                                activeFlashTile = -1
                            } else if (userSequence.size == targetSequence.size) {
                                onSuccess()
                            }
                        }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. STROOP COLOR CLASH TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloStroopComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val colors = listOf(
        Pair("RED", AppColorPalette.LossRed),
        Pair("CYAN", AppColorPalette.CyanCta),
        Pair("GREEN", AppColorPalette.WinGreen),
        Pair("GOLD", AppColorPalette.GoldPremium)
    )

    val targetPair = remember { colors.random() }
    val displayColor = remember { colors.filter { it != targetPair }.random().second }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Tap the FONT COLOR (not the written word)", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontFamily = interFamily)

        Box(
            modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(20.dp)).background(AppColorPalette.Surface),
            contentAlignment = Alignment.Center
        ) {
            Text(targetPair.first, color = displayColor, fontSize = 38.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colors.size) { idx ->
                val colorOption = colors[idx]
                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colorOption.second.copy(alpha = 0.15f))
                        .border(1.5.dp, colorOption.second, RoundedCornerShape(14.dp))
                        .clickable { if (colorOption.second == displayColor) onSuccess() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(colorOption.first, color = colorOption.second, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. WORD UNSCRAMBLE TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloWordUnscrambleComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val wordList = listOf("WAKE", "RISE", "FOCUS", "ALERT", "POWER")
    val originalWord = remember { wordList.random() }
    val scrambled = remember(originalWord) { originalWord.toList().shuffled().joinToString("") }
    var currentInput by remember { mutableStateOf("") }

    LaunchedEffect(currentInput) {
        if (currentInput.equals(originalWord, ignoreCase = true)) {
            delay(150)
            onSuccess()
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Unscramble the wake-up word", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontFamily = interFamily)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scrambled.forEach { char ->
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AppColorPalette.DeepSurface), contentAlignment = Alignment.Center) {
                    Text(char.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = titleFamily)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(16.dp)).background(AppColorPalette.Surface).border(1.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(currentInput.ifEmpty { "Tap letters below" }, color = if (currentInput.isEmpty()) Color.White.copy(alpha = 0.3f) else AppColorPalette.CyanCta, fontSize = 22.sp, fontWeight = FontWeight.W800, fontFamily = titleFamily)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scrambled.toList().shuffled().forEach { letter ->
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.08f)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).clickable {
                        if (currentInput.length < originalWord.length) currentInput += letter
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text(letter.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. SHAKE ENERGY TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloShakeComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var shakesCount by remember { mutableIntStateOf(0) }
    val requiredShakes = 15

    LaunchedEffect(shakesCount) {
        if (shakesCount >= requiredShakes) onSuccess()
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(initialValue = 0.95f, targetValue = 1.05f, animationSpec = infiniteRepeatable(tween(400, easing = EaseInOutSine), RepeatMode.Reverse), label = "scale")

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp).scale(scale).clickable { shakesCount++ }) {
            Box(modifier = Modifier.fillMaxSize().background(AppColorPalette.CyanCta.copy(alpha = 0.15f), CircleShape))
            Text("📱", fontSize = 60.sp)
        }
        Text("Shake / Tap rapidly to charge energy!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
        Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))) {
            Box(modifier = Modifier.fillMaxWidth(shakesCount.toFloat() / requiredShakes).fillMaxSize().background(Brush.horizontalGradient(listOf(AppColorPalette.CyanCta, AppColorPalette.WinGreen))))
        }
    }
}

// -----------------------------------------------------------------------------
// 6. SPEED TAP (1-6) TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloNumberOrderComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var nextExpected by remember { mutableIntStateOf(1) }
    val numbers = remember { (1..6).shuffled() }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tap numbers in order: $nextExpected ➔ 6", color = AppColorPalette.CyanCta, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(230.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(numbers.size) { idx ->
                val num = numbers[idx]
                val isCleared = num < nextExpected

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCleared) AppColorPalette.WinGreen.copy(alpha = 0.2f) else AppColorPalette.Surface)
                        .border(1.5.dp, if (isCleared) AppColorPalette.WinGreen else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable(enabled = !isCleared) {
                            if (num == nextExpected) {
                                if (nextExpected == 6) onSuccess() else nextExpected++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(num.toString(), color = if (isCleared) AppColorPalette.WinGreen else Color.White, fontSize = 24.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 7. ODD ONE OUT TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloOddOneOutComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val items = remember { listOf("🔥", "🔥", "🔥", "🔥", "🔥", "🔥", "🔥", "💥", "🔥") }
    val targetIndex = 7

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Find the odd intruder emoji!", color = AppColorPalette.CyanCta, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(230.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items.size) { idx ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColorPalette.Surface)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable { if (idx == targetIndex) onSuccess() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(items[idx], fontSize = 32.sp)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 8. SLIDING TILE TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloSlidingTileComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val currentTiles = remember { mutableStateListOf(2, 1, 3, 4) }

    LaunchedEffect(currentTiles.toList()) {
        if (currentTiles == listOf(1, 2, 3, 4)) onSuccess()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tap tiles to arrange in order 1 ➔ 2 ➔ 3 ➔ 4", color = AppColorPalette.CyanCta, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.size(200.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { idx ->
                val num = currentTiles[idx]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColorPalette.Surface)
                        .border(1.5.dp, AppColorPalette.CyanCta.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable {
                            if (idx < 3) {
                                val temp = currentTiles[idx]
                                currentTiles[idx] = currentTiles[idx + 1]
                                currentTiles[idx + 1] = temp
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(num.toString(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 9. BED-FRIENDLY ORB FOCUS TASK
// -----------------------------------------------------------------------------
@Composable
private fun SoloBalanceMazeComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var tapsLeft by remember { mutableIntStateOf(4) }

    LaunchedEffect(tapsLeft) {
        if (tapsLeft <= 0) onSuccess()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Tap the glowing center orb to focus!", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = interFamily)

        Box(
            modifier = Modifier.size(170.dp).clip(CircleShape).background(AppColorPalette.Surface).border(2.dp, AppColorPalette.CyanCta, CircleShape).clickable {
                tapsLeft--
            },
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(AppColorPalette.CyanCta))
        }

        Text("$tapsLeft focus taps remaining", color = AppColorPalette.CyanCta, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily)
    }
}

// -----------------------------------------------------------------------------
// 10. BED-FRIENDLY RAPID BED TAP TASK (Replaced Physical Squat)
// -----------------------------------------------------------------------------
@Composable
private fun SoloBedTapComponent(
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    var tapCount by remember { mutableIntStateOf(0) }
    val requiredTaps = 12

    LaunchedEffect(tapCount) {
        if (tapCount >= requiredTaps) onSuccess()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Rapidly tap button in bed to wake up!", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily)

        Box(
            modifier = Modifier.size(130.dp).clip(CircleShape).background(AppColorPalette.WinGreen.copy(alpha = 0.15f)).border(2.dp, AppColorPalette.WinGreen, CircleShape).clickable { tapCount++ },
            contentAlignment = Alignment.Center
        ) {
            Text("👆", fontSize = 54.sp)
        }

        Text("Progress: $tapCount / $requiredTaps Taps", color = AppColorPalette.WinGreen, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = interFamily)
    }
}

// Helper Numpad
@Composable
private fun SoloNumpad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    titleFamily: FontFamily
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                for (item in row) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (item.isEmpty()) Color.Transparent else AppColorPalette.DeepSurface)
                            .then(
                                if (item.isNotEmpty()) Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                else Modifier
                            )
                            .clickable(enabled = item.isNotEmpty()) {
                                if (item == "DEL") onDeleteClick() else onNumberClick(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (item == "DEL") {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = item,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W700,
                                fontFamily = titleFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SoloPuzzle(val question: String, val answer: Int)

private fun generateStageSoloPuzzle(stage: Int): SoloPuzzle {
    return when (stage) {
        1 -> {
            val a = (12..45).random()
            val b = (11..45).random()
            if ((1..2).random() == 1) SoloPuzzle("$a + $b", a + b) else SoloPuzzle("${a + b} - $a", b)
        }
        2 -> {
            val a = (4..12).random()
            val b = (6..14).random()
            SoloPuzzle("$a × $b", a * b)
        }
        else -> {
            val a = (3..9).random()
            val b = (4..11).random()
            val c = (12..35).random()
            SoloPuzzle("$a × $b + $c", a * b + c)
        }
    }
}
