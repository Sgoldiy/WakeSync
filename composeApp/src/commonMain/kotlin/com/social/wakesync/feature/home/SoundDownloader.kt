package com.social.wakesync.feature.home

interface SoundDownloader {
    /**
     * Downloads a sound from the given URL and returns the local file path.
     * If the file is already downloaded, it returns the existing path.
     */
    suspend fun downloadSound(url: String, fileName: String): String?
    
    /**
     * Checks if a sound is already downloaded.
     */
    fun isDownloaded(fileName: String): Boolean
    
    /**
     * Gets the local URI/path for a downloaded sound.
     */
    fun getLocalPath(fileName: String): String?
}

expect fun getSoundDownloader(): SoundDownloader
