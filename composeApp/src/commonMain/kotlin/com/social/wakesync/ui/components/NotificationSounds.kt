package com.social.wakesync.ui.components

/**
 * Notification sound catalog and vibration patterns for different alert types.
 * Android uses this to pick sound + vibration per push notification type.
 * iOS uses this to pick the correct APNs sound name.
 */
object NotificationSounds {

    /**
     * Notification alert types and their corresponding configuration.
     */
    enum class AlertType(
        val androidSoundName: String,
        val iosSoundName: String,
        val vibrationPattern: LongArray,
        val channelName: String,
        val channelDescription: String
    ) {
        /** Partner woke up on time */
        ALARM_WIN(
            androidSoundName = "alarm_win",
            iosSoundName = "alarm_win.caf",
            vibrationPattern = longArrayOf(0, 200, 100, 200, 100, 400), // Quick triple pulse
            channelName = "WakeSync Alarms",
            channelDescription = "Alarm results and wake-up alerts"
        ),

        /** Partner missed the alarm */
        ALARM_LOSS(
            androidSoundName = "alarm_loss",
            iosSoundName = "alarm_loss.caf",
            vibrationPattern = longArrayOf(0, 500, 300, 500), // Slow double buzz
            channelName = "WakeSync Alarms",
            channelDescription = "Alarm results and wake-up alerts"
        ),

        /** Punishment assigned to you or your partner */
        PUNISHMENT(
            androidSoundName = "punishment",
            iosSoundName = "punishment.caf",
            vibrationPattern = longArrayOf(0, 300, 200, 300, 200, 300, 200, 500), // Escalating
            channelName = "WakeSync Punishments",
            channelDescription = "Punishment assignments and proof reviews"
        ),

        /** Streak milestone reached */
        STREAK_MILESTONE(
            androidSoundName = "streak_milestone",
            iosSoundName = "streak_milestone.caf",
            vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100, 50, 300, 100, 500), // Celebration pattern
            channelName = "WakeSync Social",
            channelDescription = "Social updates, streaks, and achievements"
        ),

        /** Friend request or social interaction */
        SOCIAL(
            androidSoundName = "social",
            iosSoundName = "social.caf",
            vibrationPattern = longArrayOf(0, 150, 100, 150), // Quick double tap
            channelName = "WakeSync Social",
            channelDescription = "Social updates, streaks, and achievements"
        ),

        /** Generic / default notification */
        DEFAULT(
            androidSoundName = "default",
            iosSoundName = "default",
            vibrationPattern = longArrayOf(0, 250), // Single pulse
            channelName = "WakeSync Notifications",
            channelDescription = "General notifications"
        );

        companion object {
            fun fromType(type: String): AlertType = when (type) {
                "alarm_win" -> ALARM_WIN
                "alarm_loss" -> ALARM_LOSS
                "punishment_assigned", "proof_submitted", "punishment_completed" -> PUNISHMENT
                "streak_milestone" -> STREAK_MILESTONE
                "friend_request", "group_invite", "feed_reaction" -> SOCIAL
                else -> DEFAULT
            }
        }
    }

    /**
     * Maps notification type strings (from Firestore) to vibration patterns.
     */
    fun getVibrationPattern(type: String): LongArray {
        return AlertType.fromType(type).vibrationPattern
    }

    /**
     * Maps notification type to Android sound resource name (without extension).
     */
    fun getAndroidSoundName(type: String): String {
        return AlertType.fromType(type).androidSoundName
    }

    /**
     * Maps notification type to iOS sound filename.
     */
    fun getIOSSoundName(type: String): String {
        return AlertType.fromType(type).iosSoundName
    }

    /**
     * Maps notification type to the correct notification channel ID.
     */
    fun getChannelId(type: String): String {
        return when (AlertType.fromType(type)) {
            AlertType.ALARM_WIN, AlertType.ALARM_LOSS -> "wakesync_alarms"
            AlertType.PUNISHMENT -> "wakesync_punishments"
            AlertType.STREAK_MILESTONE, AlertType.SOCIAL -> "wakesync_social"
            AlertType.DEFAULT -> "wakesync_push"
        }
    }

    /**
     * Maps notification type to human-readable title prefix.
     */
    fun getTitleForType(type: String): String = when (AlertType.fromType(type)) {
        AlertType.ALARM_WIN -> "⏰ Partner woke up!"
        AlertType.ALARM_LOSS -> "😴 Partner missed alarm"
        AlertType.PUNISHMENT -> "🏆 Punishment Assigned"
        AlertType.STREAK_MILESTONE -> "🔥 Streak Milestone!"
        AlertType.SOCIAL -> "👋 WakeSync"
        AlertType.DEFAULT -> "WakeSync"
    }

    /**
     * User-configurable notification preferences.
     */
    data class NotificationPreferences(
        val alarmSoundsEnabled: Boolean = true,
        val punishmentSoundsEnabled: Boolean = true,
        val socialSoundsEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val soundVolume: Float = 1.0f, // 0.0 to 1.0
        val quietHoursStart: Int = -1, // -1 = disabled, 0-23 = hour
        val quietHoursEnd: Int = -1
    )
}
