package com.gsr627.vpn

import android.content.Intent
import android.net.VpnService
import android.os.IBinder

class VpnService : VpnService() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startVpnConnection()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startVpnConnection() {
        try {
            val builder = Builder()
            builder.setSession("VPN Matroules")
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("0.0.0.0", 0)
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("8.8.4.4")
            
            val vpnInterface = builder.establish()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}