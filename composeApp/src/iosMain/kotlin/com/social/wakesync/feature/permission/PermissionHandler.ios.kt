package com.social.wakesync.feature.permission

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait

class IosPermissionHandler : PermissionHandler {

    private var _notificationStatus: UNAuthorizationStatus = UNAuthorizationStatusNotDetermined

    override fun isAlarmPermissionGranted(): Boolean {
        // Alarms on iOS are tied to Notification permissions
        return isNotificationPermissionGranted()
    }

    override fun isNotificationPermissionGranted(): Boolean {
        var isGranted = false
        val semaphore = dispatch_semaphore_create(0)
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                if (settings != null) {
                    isGranted = settings.authorizationStatus == UNAuthorizationStatusAuthorized
                }
                dispatch_semaphore_signal(semaphore)
            }
        dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER)
        return isGranted
    }

    override fun isCameraPermissionGranted(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }

    override fun requestAlarmPermission() {
        requestNotificationPermission()
    }

    override fun requestNotificationPermission() {
        val options =
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        UNUserNotificationCenter.currentNotificationCenter()
            .requestAuthorizationWithOptions(options) { granted, error ->
                // Feedback handled by UI refresh on Resume
            }
    }

    override fun requestCameraPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            // Feedback handled by UI refresh on Resume
        }
    }
}

actual fun getPermissionHandler(): PermissionHandler = IosPermissionHandler()
