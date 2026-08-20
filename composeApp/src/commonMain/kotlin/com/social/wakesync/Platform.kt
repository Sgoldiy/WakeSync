package com.social.wakesync

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform