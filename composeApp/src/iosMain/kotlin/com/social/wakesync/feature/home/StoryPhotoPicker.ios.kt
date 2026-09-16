package com.social.wakesync.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toKotlinByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }

/**
 * Delegate bridging PHPickerViewController callbacks back to Compose.
 * `currentCallback` is updated on recomposition so we never invoke a stale
 * lambda capturing old state (the OS holds only a weak pointer to delegates).
 */
private class StoryPickerDelegate : NSObject(), PHPickerViewControllerDelegateProtocol {

    var currentCallback: ((ByteArray?) -> Unit)? = null

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val callback = currentCallback
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        val provider = result?.itemProvider

        if (provider == null) {
            callback?.invoke(null)
            return
        }

        provider.loadDataRepresentationForTypeIdentifier(
            typeIdentifier = "public.image"
        ) { data, _ ->
            val bytes = data?.toKotlinByteArray()
            dispatch_async(dispatch_get_main_queue()) {
                callback?.invoke(bytes)
            }
        }
    }
}

@Composable
actual fun rememberStoryPhotoPickerLauncher(
    onPicked: (ByteArray?) -> Unit
): () -> Unit {
    // Resolve the Compose host view controller through the connected window scene
    val rootViewController: UIViewController? = remember {
        val scenes = UIApplication.sharedApplication.connectedScenes.toList()
        val windowScene = scenes.filterIsInstance<UIWindowScene>().firstOrNull()
        val windows: List<*> = windowScene?.windows ?: emptyList<Any>()
        val window = windows.filterIsInstance<UIWindow>().firstOrNull { it.isKeyWindow() }
        window?.rootViewController()
    }
    val delegate = remember { StoryPickerDelegate() }
    delegate.currentCallback = onPicked

    return {
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = 1
        }
        val picker = PHPickerViewController(configuration)
        picker.delegate = delegate
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
