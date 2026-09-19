package com.social.wakesync.feature.games

/**
 * Platform-neutral helpers so game code stays 100% common (compiles on
 * Android AND iOS).
 */

/** Monotonic clock in nanoseconds — immune to wall-clock adjustments. */
internal expect fun monotonicNanos(): Long

/** Fixed 1-decimal formatting ("8.4") — common replacements for String.format. */
internal fun fmt1(value: Double): String {
    val scaled = (value * 10).toInt()
    val whole = scaled / 10
    val tenth = ((scaled % 10) + 10) % 10
    return "$whole.$tenth"
}

/** Fixed 2-decimal formatting ("8.42"). */
internal fun fmt2(value: Double): String {
    val scaled = (value * 100).toInt()
    val whole = scaled / 100
    val hundredths = ((scaled % 100) + 100) % 100
    return "$whole.${hundredths.toString().padStart(2, '0')}"
}
