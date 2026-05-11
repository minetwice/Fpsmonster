package com.ultraboost.engine.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ultraboost.engine.service.OptimizationService
import com.ultraboost.engine.utils.PreferenceManager

/**
 * Boot Receiver - Starts optimization service on device boot if enabled
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Check if auto-start is enabled
            if (PreferenceManager.autoStart) {
                // Start optimization service
                val serviceIntent = Intent(context, OptimizationService::class.java).apply {
                    action = OptimizationService.ACTION_START_OPTIMIZATION
                }
                
                try {
                    context.startForegroundService(serviceIntent)
                } catch (e: Exception) {
                    // Service start failed
                    e.printStackTrace()
                }
            }
        }
    }
}
