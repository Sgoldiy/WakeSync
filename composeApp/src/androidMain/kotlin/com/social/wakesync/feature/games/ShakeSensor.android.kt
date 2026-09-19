package com.social.wakesync.feature.games

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock

/**
 * Android accelerometer implementation of [ShakeSensor].
 *
 * Detection pipeline (per spec):
 *  1. **Magnitude:** linear acceleration ‖accel − gravity‖, where gravity is
 *     tracked with a low-pass filter (noise filtering that also removes the
 *     constant ~9.8 m/s² bias so orientation doesn't matter).
 *  2. **Threshold:** a shake only counts above [SHAKE_ACCEL_THRESHOLD]
 *     (~1.7g swing) — ordinary handling never triggers it.
 *  3. **Debounce:** a minimum [SHAKE_MIN_INTERVAL_MS] between valid shakes —
 *     one physical shake produces one count, not a burst of sensor events.
 *  4. **Lifecycle:** the listener is registered on [start] and unregistered
 *     on [stop]; the composable's DisposableEffect ties both to game-active
 *     state, so no listeners outlive the game (no leaks, no battery drain).
 */
internal actual class ShakeSensor {
    private val sensorManager: SensorManager? =
        appContext()?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    actual val isAvailable: Boolean
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    private var listener: (() -> Unit)? = null
    private var sensorEventListener: SensorEventListener? = null

    // Gravity low-pass state (per axis).
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 0f
    private var gravityInitialised = false

    // Debounce state.
    private var lastShakeAtMs = 0L

    actual fun start(onShake: () -> Unit) {
        val manager = sensorManager ?: return
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        listener = onShake

        val eventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // 1) Low-pass filter tracks gravity (alpha tuned for GAME rate).
                if (!gravityInitialised) {
                    gravityX = x; gravityY = y; gravityZ = z
                    gravityInitialised = true
                } else {
                    val alpha = 0.8f
                    gravityX = gravityX * alpha + x * (1f - alpha)
                    gravityY = gravityY * alpha + y * (1f - alpha)
                    gravityZ = gravityZ * alpha + z * (1f - alpha)
                }

                // 2) Linear acceleration magnitude ‖accel − gravity‖.
                val dx = x - gravityX
                val dy = y - gravityY
                val dz = z - gravityZ
                val magnitude = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)

                if (magnitude > SHAKE_ACCEL_THRESHOLD) {
                    // 3) Debounce: one count per minimum interval.
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastShakeAtMs >= SHAKE_MIN_INTERVAL_MS) {
                        lastShakeAtMs = now
                        listener?.invoke()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorEventListener = eventListener

        // GAME rate: responsive to real shakes without spinning at max frequency.
        manager.registerListener(eventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    actual fun stop() {
        sensorEventListener?.let { sensorManager?.unregisterListener(it) }
        sensorEventListener = null
        listener = null
    }
}

/** Resolves the application context without holding an Activity reference. */
private fun appContext(): Context? = try {
    val activityThread = Class.forName("android.app.ActivityThread")
    activityThread.getMethod("currentApplication").invoke(null) as? Context
} catch (_: Throwable) {
    null
}

/** Android factory — null when no usable accelerometer exists (tap fallback). */
internal actual fun createShakeSensor(): ShakeSensor? {
    val manager = appContext()?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    return if (manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
        ShakeSensor()
    } else null
}
