package com.gsr627.vpn

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VpnManager(private val context: Context) {
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _currentConfig = MutableStateFlow<VpnConfig?>(null)
    val currentConfig: StateFlow<VpnConfig?> = _currentConfig

    private val _trafficStats = MutableStateFlow<Pair<Long, Long>>(0L to 0L)
    val trafficStats: StateFlow<Pair<Long, Long>> = _trafficStats

    fun connectVpn(config: VpnConfig) {
        try {
            _currentConfig.value = config
            _isConnected.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isConnected.value = false
        }
    }

    fun disconnectVpn() {
        _isConnected.value = false
        _currentConfig.value = null
    }

    fun updateTrafficStats(download: Long, upload: Long) {
        _trafficStats.value = download to upload
    }
}