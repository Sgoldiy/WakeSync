package com.social.wakesync.feature.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.social.wakesync.MainActivity
import com.social.wakesync.ui.components.NotificationSounds

class WakeSyncMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ALARMS = "wakesync_alarms"
        const val CHANNEL_PUNISHMENTS = "wakesync_punishments"
        const val CHANNEL_SOCIAL = "wakesync_social"
        const val CHANNEL_DEFAULT = "wakesync_push"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Create all notification channels (Android 8+)
        createNotificationChannels()

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "WakeSync"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val type = message.data["type"] ?: ""
        val notificationId = message.data["notificationId"] ?: ""

        // Resolve alert config from type
        val alertType = NotificationSounds.AlertType.fromType(type)
        val channelId = NotificationSounds.getChannelId(type)

        // Build intent to open app on tap with type data
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            putExtra("notification_id", notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification with type-specific icon
        val smallIcon = when (alertType) {
            NotificationSounds.AlertType.ALARM_WIN -> android.R.drawable.ic_lock_idle_alarm
            NotificationSounds.AlertType.ALARM_LOSS -> android.R.drawable.ic_dialog_alert
            NotificationSounds.AlertType.PUNISHMENT -> android.R.drawable.ic_menu_close_clear_cancel
            NotificationSounds.AlertType.STREAK_MILESTONE -> android.R.drawable.ic_dialog_info
            NotificationSounds.AlertType.SOCIAL -> android.R.drawable.ic_dialog_info
            NotificationSounds.AlertType.DEFAULT -> android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        // Vibrate with type-specific pattern
        vibrateWithPattern(type)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Alarm results channel — high priority, loud
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "WakeSync Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm results — partner wake-ups and misses"
                enableVibration(true)
                vibrationPattern = NotificationSounds.AlertType.ALARM_WIN.vibrationPattern
                setBypassDnd(true)
            }

            // Punishment channel — high priority
            val punishmentChannel = NotificationChannel(
                CHANNEL_PUNISHMENTS,
                "WakeSync Punishments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Punishment assignments and proof reviews"
                enableVibration(true)
                vibrationPattern = NotificationSounds.AlertType.PUNISHMENT.vibrationPattern
            }

            // Social channel — default priority
            val socialChannel = NotificationChannel(
                CHANNEL_SOCIAL,
                "WakeSync Social",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Social updates, streaks, and achievements"
                enableVibration(true)
                vibrationPattern = NotificationSounds.AlertType.SOCIAL.vibrationPattern
            }

            // Default fallback channel
            val defaultChannel = NotificationChannel(
                CHANNEL_DEFAULT,
                "WakeSync Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General WakeSync notifications"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(alarmChannel, punishmentChannel, socialChannel, defaultChannel)
            )
        }
    }

    private fun vibrateWithPattern(type: String) {
        val pattern = NotificationSounds.getVibrationPattern(type)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Store token in user document
        db.collection("users").document(user.uid)
            .update(
                mapOf(
                    "fcmToken" to token,
                    "fcmTokenUpdatedAt" to System.currentTimeMillis(),
                    "platform" to "android"
                )
            )
            .addOnFailureListener {
                // If document doesn't exist, set it
                db.collection("users").document(user.uid)
                    .set(
                        mapOf(
                            "fcmToken" to token,
                            "fcmTokenUpdatedAt" to System.currentTimeMillis(),
                            "platform" to "android"
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
            }

        // Also store in tokens subcollection for multi-device support
        db.collection("users").document(user.uid)
            .collection("tokens").document(token)
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "createdAt" to System.currentTimeMillis(),
                    "active" to true
                )
            )
    }
}
