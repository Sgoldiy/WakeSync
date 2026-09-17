package com.social.wakesync.feature.home

import platform.AVFoundation.*
import platform.Foundation.NSURL

class IosSoundPlayer : SoundPlayer {
    private var avPlayer: AVPlayer? = null

    override fun playPreview(url: String, pitch: Float, speed: Float) {
        stopPreview()
        val nsUrl = NSURL.URLWithString(url) ?: return
        avPlayer = AVPlayer.playerWithURL(nsUrl).apply {
            rate = speed
            play()
        }
    }

    override fun updatePlaybackParams(pitch: Float, speed: Float) {
        avPlayer?.rate = speed
    }

    override fun stopPreview() {
        avPlayer?.pause()
        avPlayer = null
    }

    override fun release() {
        stopPreview()
    }
}

actual fun getSoundPlayer(): SoundPlayer = IosSoundPlayer()
