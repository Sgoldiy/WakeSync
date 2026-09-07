package com.social.wakesync.feature.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import platform.Network.*
import platform.darwin.dispatch_queue_create

class IosNetworkMonitor : NetworkMonitor {

    private val _isOnline = MutableStateFlow(false)
    private val monitor: nw_path_monitor_t

    override val isOnline: Flow<Boolean> = _isOnline

    init {
        monitor = nw_path_monitor_create()
        val queue = dispatch_queue_create("com.wakesync.networkmonitor", null)

        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            _isOnline.value = (status == nw_path_status_satisfied)
        }

        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    override suspend fun awaitOnline() {
        if (_isOnline.value) return
        _isOnline.first { it }
    }
}

actual fun getNetworkMonitor(): NetworkMonitor = IosNetworkMonitor()
