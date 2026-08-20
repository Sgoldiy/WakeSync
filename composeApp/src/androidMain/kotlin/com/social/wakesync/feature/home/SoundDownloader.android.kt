package com.social.wakesync.feature.home

import android.content.Context
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

class AndroidSoundDownloader(private val context: Context) : SoundDownloader {
    private val client = HttpClient()

    override suspend fun downloadSound(url: String, fileName: String): String? {
        val file = File(context.filesDir, "sounds/$fileName")
        if (file.exists()) return file.absolutePath
        
        file.parentFile?.mkdirs()
        
        return try {
            val response = client.get(url)
            if (response.status.value in 200..299) {
                response.bodyAsChannel().copyTo(file.writeChannel())
                file.absolutePath
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun isDownloaded(fileName: String): Boolean {
        return File(context.filesDir, "sounds/$fileName").exists()
    }

    override fun getLocalPath(fileName: String): String? {
        val file = File(context.filesDir, "sounds/$fileName")
        return if (file.exists()) file.absolutePath else null
    }
}

private var androidSoundDownloader: SoundDownloader? = null

fun initializeSoundDownloader(context: Context) {
    androidSoundDownloader = AndroidSoundDownloader(context)
}

actual fun getSoundDownloader(): SoundDownloader {
    return androidSoundDownloader ?: throw IllegalStateException("SoundDownloader not initialized")
}
