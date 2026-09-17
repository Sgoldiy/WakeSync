package com.social.wakesync.feature.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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

data class SoloPuzzle(val question: String, val answer: Int)

fun generateStageSoloPuzzle(stage: Int): SoloPuzzle {
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

@Composable
fun SpeedMathGame(
    stage: Int,
    onSuccess: () -> Unit,
    titleFamily: FontFamily,
    interFamily: FontFamily
) {
    val puzzle = remember(stage) { generateStageSoloPuzzle(stage) }
    var currentInput by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(currentInput) {
        if (currentInput == puzzle.answer.toString()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(150)
            onSuccess()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(AppColorPalette.CyanCta, AppColorPalette.MagentaHot))
                    ),
                    RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColorPalette.Surface)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "${puzzle.question} = ${currentInput.ifEmpty { "···" }}",
                    color = AppColorPalette.CyanCta,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.W900,
                    fontFamily = titleFamily
                )
            }
        }

        SoloNumpad(
            onNumberClick = { num ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (currentInput.length < 5) currentInput += num
            },
            onDeleteClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1)
            },
            titleFamily = titleFamily
        )
    }
}

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
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val keyScale by animateFloatAsState(if (isPressed) 0.92f else 1.0f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .scale(keyScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (item.isEmpty()) Color.Transparent else AppColorPalette.DeepSurface)
                            .then(
                                if (item.isNotEmpty()) Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                else Modifier
                            )
                            .clickable(enabled = item.isNotEmpty(), interactionSource = interactionSource, indication = null) {
                                if (item == "DEL") onDeleteClick() else onNumberClick(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (item == "DEL") {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = item,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.W800,
                                fontFamily = titleFamily
                            )
                        }
                    }
                }
            }
        }
    }
}
