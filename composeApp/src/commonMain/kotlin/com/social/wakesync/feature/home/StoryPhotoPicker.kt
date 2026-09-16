package com.social.wakesync.feature.home

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic gallery photo picker for story backgrounds.
 *
 * Returns a launch function: invoking it opens the system gallery/photo picker.
 * When the user picks an image, [onPicked] is called with the raw image bytes
 * (or null if the user cancelled / picking is unavailable).
 */
@Composable
expect fun rememberStoryPhotoPickerLauncher(
    onPicked: (ByteArray?) -> Unit
): () -> Unit
