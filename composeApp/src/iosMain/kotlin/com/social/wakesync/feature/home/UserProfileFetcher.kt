package com.social.wakesync.feature.home

actual fun fetchUserProfile(onResult: (name: String, imageUrl: String) -> Unit) {
    // iOS implementation placeholder. 
    // In a real app, you would use Firebase iOS SDK or a KMP wrapper here.
    onResult("Jake", "")
}
