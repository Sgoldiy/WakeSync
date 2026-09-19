package com.social.wakesync.feature.games

/**
 * Phase-keyed sound effects, shared by all 12 games.
 *
 *   CHIME  = ACT phase     (player's turn — a bright ascending two-note chime)
 *   BUZZ   = WRONG phase   (mistake — a low harsh buzz)
 *   SPARK  = WIN           (puzzle solved — a quick sparkle arpeggio)
 *
 * Backed by haptic feedback as well so the game still "speaks" with the
 * phone on silent / media volume down.
 */
expect object GameSounds {
    /** Bright ascending chime — the ACT phase signal. */
    fun chime()

    /** Low harsh buzz — the WRONG phase signal. */
    fun buzz()

    /** Sparkle arpeggio — the win signal. */
    fun spark()

    /** Tiny neutral tick for a correct tap mid-flow. */
    fun tick()

    /**
     * Rising combo chime — pitch climbs one semitone per consecutive correct
     * tap. [comboStep] is 1-based (capped by the caller at [GameCombo.MAX]).
     */
    fun chimeRising(comboStep: Int)
}

/**
 * Combo streak tracker: consecutive correct taps chime higher and higher.
 * Call [onCorrectTap] after every correct tap and [reset] on any wrong tap /
 * phase failure. The WRONG phase automatically resets it via [GameFx].
 */
object GameCombo {
    const val MAX = 12

    private var combo = 0

    /** Play a chime one semitone higher than the last correct tap. */
    fun onCorrectTap() {
        combo = (combo + 1).coerceAtMost(MAX)
        GameSounds.chimeRising(combo)
    }

    fun reset() {
        combo = 0
    }
}

/** Convenience so game code reads naturally next to the phase enum. */
object GameFx {
    fun onPhase(phase: GamePhase) = when (phase) {
        GamePhase.ACT -> GameSounds.chime()
        GamePhase.WRONG -> {
            GameCombo.reset() // a miss kills the combo streak
            GameSounds.buzz()
        }
        GamePhase.WATCH -> GameSounds.tick()
        GamePhase.INFO -> {}
    }
}
