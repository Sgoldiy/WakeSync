package com.social.wakesync

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import com.social.wakesync.app.App
import com.social.wakesync.feature.profile.IosFirestoreBridge
import com.social.wakesync.feature.profile.IosFirestoreBridgeHolder

import androidx.compose.runtime.remember
import com.social.wakesync.app.MainViewModel

fun MainViewController(
    initiallyAuthenticated: Boolean = false,
    isPermissionsGranted: Boolean = false,
    onGoogleSignInRequested: (((String?) -> Unit) -> Unit)? = null,
    firestoreBridge: IosFirestoreBridge? = null,
): UIViewController {
    // Install the Firestore bridge before returning the controller
    if (firestoreBridge != null) {
        IosFirestoreBridgeHolder.bridge = firestoreBridge
    }

    return ComposeUIViewController {
        val viewModel = remember { MainViewModel() }
        App(
            viewModel = viewModel,
            initiallyAuthenticated = initiallyAuthenticated,
            isPermissionsGranted = isPermissionsGranted,
            onGoogleSignInRequested = onGoogleSignInRequested ?: { callback ->
                callback("Google Sign-In bridge is not configured.")
            },
        )
    }
}
