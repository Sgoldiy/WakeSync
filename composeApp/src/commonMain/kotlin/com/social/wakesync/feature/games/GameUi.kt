package com.social.wakesync.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.wakesync.ui.theme.AppColorPalette

/**
 * Shared visibility toolkit used by ALL 12 games so the player always knows
 * what to do: one consistent visual language across the whole app.
 *
 *   WATCH  = cyan/gold   (game is playing/demoing — hands off)
 *   ACT    = green       (your turn — tap!)
 *   WRONG  = red         (mistake — retry incoming)
 *   INFO   = white/gold  (how-to text, neutral status)
 */

enum class GamePhase { WATCH, ACT, WRONG, INFO }

@Composable
fun phaseColor(phase: GamePhase): Color = when (phase) {
    GamePhase.WATCH -> AppColorPalette.CyanCta
    GamePhase.ACT -> AppColorPalette.WinGreen
    GamePhase.WRONG -> AppColorPalette.LossRed
    GamePhase.INFO -> AppColorPalette.GoldPremium
}

/** Pill banner at the top of every game: the big "what do I do now?" signal.
 *  Also fires the phase sound effect exactly when the phase/text changes —
 *  every game that renders a banner gets sounds with zero extra wiring. */
@Composable
fun PhaseBanner(
    phase: GamePhase,
    text: String,
    interFamily: FontFamily
) {
    LaunchedEffect(phase, text) { GameFx.onPhase(phase) }
    val c = phaseColor(phase)
    Box(
        Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(c.copy(alpha = 0.2f))
            .border(1.5.dp, c, RoundedCornerShape(99.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = c,
            fontSize = 14.sp, fontWeight = FontWeight.Black,
            fontFamily = interFamily, textAlign = TextAlign.Center
        )
    }
}

/** Big step readout: "3 / 5" style progress the player can read at a glance. */
@Composable
fun StepCounter(
    text: String,
    phase: GamePhase,
    titleFamily: FontFamily
) {
    Text(
        text = text,
        color = phaseColor(phase),
        fontSize = 20.sp, fontWeight = FontWeight.W900, fontFamily = titleFamily
    )
}

/** One-line how-to-play hint — shown on the first round / first attempt. */
@Composable
fun HowToHint(
    text: String,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = AppColorPalette.GoldPremium,
        fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 10.dp)
    )
}

/** Small neutral status line under the game. */
@Composable
fun GameStatusLine(
    text: String,
    interFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = interFamily,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 10.dp)
    )
}
