package com.social.wakesync.feature.home

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.content.Context
import android.net.Uri

class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    override fun playPreview(url: String) {
        stopPreview()
        
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { start() }
            setOnCompletionListener { stopPreview() }
        }
    }

    override fun stopPreview() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun release() {
        stopPreview()
    }
}

// In a real app, you'd use a dependency injection framework or a better way to get the context.
// For now, we'll assume there's a way to provide context or use a placeholder.
private var appContext: Context? = null
fun initSoundPlayer(context: Context) {
    appContext = context
}

actual fun getSoundPlayer(): SoundPlayer {
    return AndroidSoundPlayer(appContext ?: throw IllegalStateException("SoundPlayer not initialized with context"))
}
