package com.social.wakesync.feature.home

interface SoundPlayer {
    /**
     * Plays a sound from the given URL or local path.
     */
    fun playPreview(url: String)
    
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
