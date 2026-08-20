@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.social.wakesync.feature.home

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import platform.Foundation.*
import kotlinx.cinterop.*

class IosSoundDownloader : SoundDownloader {
    private val client = HttpClient()

    override suspend fun downloadSound(url: String, fileName: String): String? {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentsDirectory = urls.first() as NSURL
        val soundFolder = documentsDirectory.URLByAppendingPathComponent("sounds")!!
        
        if (!fileManager.fileExistsAtPath(soundFolder.path!!)) {
            fileManager.createDirectoryAtURL(soundFolder, withIntermediateDirectories = true, attributes = null, error = null)
        }
        
        val destinationUrl = soundFolder.URLByAppendingPathComponent(fileName)!!
        if (fileManager.fileExistsAtPath(destinationUrl.path!!)) return destinationUrl.path
        
        return try {
            val response = client.get(url)
            if (response.status.value in 200..299) {
                val data = response.readRawBytes()
                val nsData = data.toNSData()
                nsData.writeToURL(destinationUrl, true)
                destinationUrl.path
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun isDownloaded(fileName: String): Boolean {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentsDirectory = urls.first() as NSURL
        val destinationUrl = documentsDirectory.URLByAppendingPathComponent("sounds/$fileName")!!
        return fileManager.fileExistsAtPath(destinationUrl.path!!)
    }

    override fun getLocalPath(fileName: String): String? {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentsDirectory = urls.first() as NSURL
        val destinationUrl = documentsDirectory.URLByAppendingPathComponent("sounds/$fileName")!!
        return if (fileManager.fileExistsAtPath(destinationUrl.path!!)) destinationUrl.path else null
    }

    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.dataWithBytes(it.addressOf(0), size.toULong())
    }
}

private var iosSoundDownloader: SoundDownloader? = null

fun initializeSoundDownloader() {
    iosSoundDownloader = IosSoundDownloader()
}

actual fun getSoundDownloader(): SoundDownloader {
    return iosSoundDownloader ?: throw IllegalStateException("SoundDownloader not initialized")
}
