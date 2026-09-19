package com.social.wakesync.feature.games

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSProcessInfo

/**
 * iOS actual: system uptime — monotonic nanoseconds since boot (immune to
 * wall-clock changes, pauses during deep sleep which is irrelevant for a
 * sub-minute game).
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun monotonicNanos(): Long =
    NSProcessInfo.processInfo.systemUptime.let { uptime -> (uptime * 1_000_000_000.0).toLong() }
