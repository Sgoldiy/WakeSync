package com.social.wakesync.feature.games

import android.os.SystemClock

/** Android actual: elapsedRealtime since boot — monotonic, includes deep sleep. */
internal actual fun monotonicNanos(): Long = SystemClock.elapsedRealtimeNanos()
