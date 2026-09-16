package com.social.wakesync.feature.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.File
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.social.wakesync.FIRESTORE_DATABASE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "wakesync_alarm_channel"
        const val ACTION_DISMISS = "com.social.wakesync.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.social.wakesync.ACTION_SNOOZE"
        const val SNOOZE_DURATION_MS = 5 * 60 * 1000L
        const val AUTO_STOP_MS = 5 * 60 * 1000L
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentAlarmId: String? = null
    private var currentNotificationId: Int = 0
    private val serviceScope = CoroutineScope(Dispatchers.Main + kotlinx.coroutines.SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.O)
    private val autoStopRunnable = Runnable {
        handleDismissal()
        stopAlarm()
        AlarmState.showStreakBroken = true
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val action = intent.action
        val alarmId = intent.getStringExtra("ALARM_ID") ?: currentAlarmId ?: System.currentTimeMillis().toString()
        val alarmTime = intent.getStringExtra("ALARM_TIME") ?: ""
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: ""
        val alarmMode = intent.getStringExtra("ALARM_MODE") ?: ""
        val alarmChallenge = intent.getStringExtra("ALARM_CHALLENGE") ?: "Math"
        val alarmPartners = intent.getStringExtra("ALARM_PARTNERS")
        val soundId = intent.getStringExtra("SOUND_ID")

        currentAlarmId = alarmId
        currentNotificationId = alarmId.hashCode()

        // Always ensure channel is created and startForeground is called immediately
        // to comply with Android 8.0+ and 14+ strict foreground service rules.
        createNotificationChannel()
        val notification = buildNotification(alarmId, alarmTime, alarmLabel, alarmMode, soundId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                currentNotificationId,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(currentNotificationId, notification)
        }

        if (action == ACTION_DISMISS) {
            handleDismissal()
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_SNOOZE) {
            snoozeAlarm(alarmId, alarmLabel, alarmMode, soundId)
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        // Set global alarm ringing state
        AlarmState.isRinging = true
        AlarmState.activeAlarmId = alarmId
        AlarmState.activeAlarmMode = alarmMode
        AlarmState.activeAlarmChallenge = alarmChallenge
        AlarmState.activeAlarmPartnerUsername = alarmPartners

        if (alarmMode == "Duo" || alarmMode == "Group") {
            serviceScope.launch {
                try {
                    getHomeRepository().resetDuoAlarmWinner(alarmId)
                } catch (_: Exception) {}
            }
        }

        val timeoutMs = if (alarmMode.equals("Solo", ignoreCase = true) || alarmMode.equals("Duo", ignoreCase = true)) {
            2 * 60 * 1000L // 2 minutes
        } else {
            AUTO_STOP_MS
        }

        acquireWakeLock(timeoutMs)
        startRinging(soundId)
        startVibrating()

        handler.removeCallbacks(autoStopRunnable)
        handler.postDelayed(autoStopRunnable, timeoutMs)

        return START_STICKY
    }

    @SuppressLint("Wakelock")
    private fun acquireWakeLock(timeoutMs: Long) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, maxVol, 0)
        } catch (e: Exception) {
            // Ignore volume control restriction if any
        }

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WakeSync::AlarmWakeLock"
            ).apply {
                acquire(timeoutMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WakeSync Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm notifications"
                setSound(null, null) // Sound is handled by MediaPlayer
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        alarmId: String,
        alarmTime: String,
        label: String,
        mode: String,
        soundId: String?
    ): Notification {
        val title = if (mode.equals("Solo", ignoreCase = true)) "⏰ Solo Wake Up!" else "WakeSync Alarm"
        val text = if (alarmTime.isNotBlank()) "It's $alarmTime! Time to wake up." else "Time to wake up!"

        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_DISMISS
        }
        val dismissPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this, alarmId.hashCode() + 1, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this, alarmId.hashCode() + 1, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_MODE", mode)
            putExtra("SOUND_ID", soundId)
        }
        val snoozePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this, alarmId.hashCode() + 2, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this, alarmId.hashCode() + 2, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val fullScreenIntent = Intent().apply {
            setClassName(this@AlarmService, "com.social.wakesync.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, alarmId.hashCode() + 3, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 5m", snoozePendingIntent)
            .build()
    }

    private fun startRinging(soundId: String?) {
        try {
            var soundUri: Uri? = null
            if (soundId != null) {
                val soundFile = File(filesDir, "sounds/$soundId.mp3")
                if (soundFile.exists()) {
                    soundUri = Uri.fromFile(soundFile)
                }
            }
            if (soundUri == null) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer?.apply {
                try {
                    if (isPlaying) stop()
                    release()
                } catch (_: Exception) {}
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, soundUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                setOnPreparedListener { mp ->
                    try {
                        if (mediaPlayer == mp) {
                            mp.start()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibrating() {
        try {
            val pattern = longArrayOf(0, 1000, 500, 1000)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibrator = vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun snoozeAlarm(alarmId: String?, label: String?, mode: String?, soundId: String?) {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val snoozeTime = System.currentTimeMillis() + SNOOZE_DURATION_MS

            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmId ?: "snooze")
                putExtra("ALARM_TIME", "Snoozed")
                putExtra("ALARM_LABEL", label ?: "WakeSync Alarm")
                putExtra("ALARM_MODE", mode ?: "Solo")
                putExtra("SOUND_ID", soundId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this, (alarmId?.hashCode() ?: 0) + 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val showIntent = PendingIntent.getActivity(
                this, (alarmId?.hashCode() ?: 0) + 101,
                Intent(this, com.social.wakesync.MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(snoozeTime, showIntent),
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopAlarm() {
        try {
            mediaPlayer?.apply {
                setOnPreparedListener(null)
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vibrator = null

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        wakeLock = null

        handler.removeCallbacks(autoStopRunnable)
        
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(currentNotificationId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Clear global alarm ringing state
        AlarmState.isRinging = false
        AlarmState.activeAlarmId = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleDismissal() {
        val alarmId = currentAlarmId ?: AlarmState.activeAlarmId ?: return
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser ?: return
        val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
        
        // Use NonCancellable context so firestore updates persist even if serviceScope is cancelled on stopSelf()
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                try {
                    val docRef = db.collection("users")
                        .document(user.uid)
                        .collection("alarms")
                        .document(alarmId)
                    
                    val doc = docRef.get().await()
                    if (doc != null && doc.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val days = doc.get("days") as? List<Long> ?: emptyList()
                        
                        if (days.isEmpty()) {
                            // One-time alarm, disable it
                            docRef.update("isEnabled", false).await()
                        } else {
                            // Recurring alarm: calculate next timestamp
                            val timeStr = doc.getString("time") ?: ""
                            if (timeStr.contains(":")) {
                                val parts = timeStr.split(":")
                                val hour = parts[0].toIntOrNull() ?: 0
                                val minute = parts[1].toIntOrNull() ?: 0
                                
                                val nextTimestamp = calculateNextOccurrence(hour, minute, days.map { it.toInt() })
                                docRef.update("timestamp", nextTimestamp).await()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        try {
            serviceScope.cancel()
        } catch (_: Exception) {}
    }
}
