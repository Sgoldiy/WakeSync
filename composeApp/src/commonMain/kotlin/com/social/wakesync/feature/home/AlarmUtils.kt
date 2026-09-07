package com.social.wakesync.feature.home

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Calculate the next occurrence timestamp for an alarm based on hour, minute, and days.
 * This is a shared utility used by both HomeViewModel and AlarmService to avoid duplication.
 *
 * @param hour Hour in 24h format (0-23)
 * @param minute Minute (0-59)
 * @param days List of day indices (0=Mon to 6=Sun). Empty = once (today or tomorrow).
 * @return Epoch milliseconds of the next occurrence
 */
fun calculateNextOccurrence(hour: Int, minute: Int, days: List<Int>): Long {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(timeZone)

    val candidate = LocalDateTime(now.year, now.month, now.day, hour, minute)

    // If no days selected, it's a "Once" alarm (today or tomorrow)
    if (days.isEmpty()) {
        return if (candidate > now) {
            candidate.toInstant(timeZone).toEpochMilliseconds()
        } else {
            candidate.toInstant(timeZone).plus(1, DateTimeUnit.DAY, timeZone).toEpochMilliseconds()
        }
    }

    // Find the closest upcoming day (including today if time hasn't passed)
    val currentDayIdx = now.dayOfWeek.ordinal // 0 (Mon) to 6 (Sun)

    for (i in 0..7) {
        val checkDayIdx = (currentDayIdx + i).rem(7)
        if (days.contains(checkDayIdx)) {
            val potentialInstant = candidate.toInstant(timeZone).plus(i, DateTimeUnit.DAY, timeZone)
            if (potentialInstant.toEpochMilliseconds() > Clock.System.now().toEpochMilliseconds()) {
                return potentialInstant.toEpochMilliseconds()
            }
        }
    }

    // Fallback to tomorrow
    return candidate.toInstant(timeZone).plus(1, DateTimeUnit.DAY, timeZone).toEpochMilliseconds()
}

/**
 * Format a 24h time string "HH:mm" to 12h format "H:mm AM/PM"
 */
fun formatAlarmTime12h(time: String): String {
    return try {
        val parts = time.split(":")
        var hour = parts[0].toInt()
        val min = parts[1]
        val ampm = if (hour >= 12) "PM" else "AM"
        if (hour > 12) hour -= 12
        if (hour == 0) hour = 12
        "$hour:$min $ampm"
    } catch (_: Exception) {
        time
    }
}

/**
 * Calculate the time remaining until a target timestamp
 * @return Pair of (hours, minutes) remaining, or null if past
 */
fun calculateTimeRemaining(targetTimestamp: Long): Pair<Long, Long>? {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = targetTimestamp - now
    if (diff <= 0L) return null
    val hours = diff / 3600000L
    val minutes = (diff / 60000L).rem(60L)
    return hours to minutes
}
