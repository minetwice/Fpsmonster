package com.ultraboost.engine

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ultraboost.engine.utils.PreferenceManager

/**
 * UltraBoost Application Class
 * Initializes core components on app startup
 */
class UltraBoostApplication : Application() {

    companion object {
        const val CHANNEL_OPTIMIZATION = "optimization_channel"
        const val CHANNEL_ALERTS = "alerts_channel"
        
        lateinit var instance: UltraBoostApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize preference manager
        PreferenceManager.init(this)
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize native engine
        try {
            System.loadLibrary("ultraboost_native")
        } catch (e: UnsatisfiedLinkError) {
            // Native library not available yet (first build)
            e.printStackTrace()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Optimization Channel - High priority for active optimization
            val optimizationChannel = NotificationChannel(
                CHANNEL_OPTIMIZATION,
                "Optimization Status",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows real-time optimization status during gaming"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            
            // Alerts Channel - For important notifications
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Important Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thermal warnings and optimization alerts"
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(optimizationChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }
}
