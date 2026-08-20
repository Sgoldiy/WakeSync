package com.social.wakesync.feature.permission

interface PermissionHandler {
    fun isAlarmPermissionGranted(): Boolean
    fun isNotificationPermissionGranted(): Boolean
    fun isCameraPermissionGranted(): Boolean
    
    fun requestAlarmPermission()
    fun requestNotificationPermission()
    fun requestCameraPermission()
}

expect fun getPermissionHandler(): PermissionHandler
