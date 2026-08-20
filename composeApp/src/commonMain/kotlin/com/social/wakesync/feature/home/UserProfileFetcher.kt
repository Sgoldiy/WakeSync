package com.social.wakesync.feature.home

expect fun fetchUserProfile(onResult: (name: String, imageUrl: String) -> Unit)
