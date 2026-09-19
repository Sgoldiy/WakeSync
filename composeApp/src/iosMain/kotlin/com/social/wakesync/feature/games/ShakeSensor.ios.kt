package com.social.wakesync.feature.games

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import platform.darwin.NSObject
import kotlin.math.sqrt

/**
 * iOS actual for the [ShakeSensor] expect — real CoreMotion accelerometer.
 *
 * Detection pipeline (mirrors the Android implementation exactly):
 *  1. **Magnitude:** linear acceleration ‖accel − gravity‖, where gravity is
 *     tracked with a low-pass filter (removes the constant ~1g bias so the
 *     phone's orientation doesn't matter).
 *  2. **Threshold:** a shake only counts above [SHAKE_ACCEL_THRESHOLD]
 *     (~1.7g swing) — ordinary handling never triggers it.
 *  3. **Debounce:** a minimum [SHAKE_MIN_INTERVAL_MS] between valid shakes —
 *     one physical shake produces exactly one count, not a burst.
 *  4. **Lifecycle:** updates start on [start] and stop on [stop]; the
 *     composable's DisposableEffect ties both to game-active state (no
 *     leaks, no battery drain). Sensor updates arrive on the main queue,
 *     which Compose tolerates for a simple state callback.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual class ShakeSensor {

    private val motionManager = CMMotionManager()

    /** ~1.7g linear-acceleration swing counts as a genuine shake (m/s²). */
    private val shakeThreshold = 16.7

    /** Minimum interval between valid shakes (debounce). */
    private val minIntervalNanos = 280_000_000L

    private var lastShakeNanos = 0L
    private var gravity = Triple(0.0, 0.0, 0.0)
    private var onShakeCallback: (() -> Unit)? = null

    actual val isAvailable: Boolean
        get() = motionManager.isAccelerometerAvailable()

    actual fun start(onShake: () -> Unit) {
        onShakeCallback = onShake
        if (!motionManager.isAccelerometerAvailable()) return
        gravity = Triple(0.0, 0.0, 0.0)
        lastShakeNanos = 0L
        motionManager.accelerometerUpdateInterval = 1.0 / 60.0 // 60 Hz — game-grade, not battery-max
        motionManager.startAccelerometerUpdatesToQueue(
            NSOperationQueue.mainQueue()
        ) { data, _ ->
            val accel = data?.acceleration() ?: return@startAccelerometerUpdatesToQueue
            val (ax, ay, az) = accel.useContents { Triple(x, y, z) }

            // Low-pass gravity tracking (same filter constant as Android).
            val alpha = 0.8
            gravity = Triple(
                gravity.first * alpha + ax * (1 - alpha),
                gravity.second * alpha + ay * (1 - alpha),
                gravity.third * alpha + az * (1 - alpha)
            )

            // Linear acceleration magnitude (g units from CoreMotion).
            val lx = ax - gravity.first
            val ly = ay - gravity.second
            val lz = az - gravity.third
            val magnitude = sqrt(lx * lx + ly * ly + lz * lz)

            val nowNanos = monotonicNanos()
            if (magnitude > shakeThreshold &&
                nowNanos - lastShakeNanos > minIntervalNanos
            ) {
                lastShakeNanos = nowNanos
                onShakeCallback?.invoke()
            }
        }
    }

    actual fun stop() {
        onShakeCallback = null
        if (motionManager.isAccelerometerActive()) {
            motionManager.stopAccelerometerUpdates()
        }
    }
}

/** iOS factory — null when no accelerometer exists (tap fallback covers it). */
internal actual fun createShakeSensor(): ShakeSensor? = ShakeSensor().takeIf { it.isAvailable }
