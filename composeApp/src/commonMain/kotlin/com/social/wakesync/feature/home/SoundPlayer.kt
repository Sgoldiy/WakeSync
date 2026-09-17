package com.social.wakesync.feature.home

interface SoundPlayer {
    /**
     * Plays a sound from the given URL or local path with pitch and speed controls.
     */
    fun playPreview(url: String, pitch: Float = 1.0f, speed: Float = 1.0f)

    /**
     * Dynamically updates pitch and speed on the active preview.
     */
    fun updatePlaybackParams(pitch: Float, speed: Float)
    
    /**
     * Stops the current preview playback.
     */
    fun stopPreview()
    
    /**
     * Releases resources used by the player.
     */
    fun release()
}

expect fun getSoundPlayer(): SoundPlayer
