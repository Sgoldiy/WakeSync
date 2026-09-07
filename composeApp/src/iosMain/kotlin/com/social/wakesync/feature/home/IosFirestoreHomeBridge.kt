package com.social.wakesync.feature.home

import com.social.wakesync.feature.profile.IosFirestoreBridge
import com.social.wakesync.feature.profile.IosFirestoreBridgeHolder

/**
 * Re-use the profile bridge for home operations.
 * The SwiftFirestoreBridge implements both profile and home Firestore operations.
 */
typealias IosFirestoreHomeBridge = IosFirestoreBridge

object IosFirestoreHomeBridgeHolder {
    val bridge: IosFirestoreHomeBridge?
        get() = IosFirestoreBridgeHolder.bridge
}
