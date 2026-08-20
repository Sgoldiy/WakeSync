package com.social.wakesync.ui.utils

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a physical back button, so this is a no-op.
    // For production, you could implement swipe-to-back detection here.
}
