package com.ultraboost.engine.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.ultraboost.engine.R
import com.ultraboost.engine.databinding.OverlayPerformanceBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Overlay Service for in-game performance display
 * Shows FPS, temperature, and optimization status during gameplay
 */
class OverlayService : Service() {
    
    companion object {
        private const val TAG = "OverlayService"
    }
    
    private val binder = OverlayBinder()
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var binding: OverlayPerformanceBinding? = null
    
    private val _isOverlayVisible = MutableStateFlow(false)
    val isOverlayVisible: StateFlow<Boolean> = _isOverlayVisible.asStateFlow()
    
    private val overlayScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> showOverlay()
            ACTION_HIDE_OVERLAY -> hideOverlay()
            ACTION_TOGGLE_OVERLAY -> toggleOverlay()
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        overlayScope.cancel()
    }
    
    /**
     * Show the performance overlay
     */
    fun showOverlay() {
        if (overlayView != null) {
            Log.w(TAG, "Overlay already visible")
            return
        }
        
        try {
            // Inflate overlay layout
            binding = OverlayPerformanceBinding.inflate(LayoutInflater.from(this))
            overlayView = binding!!.root
            
            // Configure layout parameters for overlay
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                marginEnd = 32
                topMargin = 32
            }
            
            windowManager?.addView(overlayView, layoutParams)
            _isOverlayVisible.value = true
            
            // Start update loop
            startUpdateLoop()
            
            Log.i(TAG, "Overlay shown")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing overlay", e)
        }
    }
    
    /**
     * Hide the performance overlay
     */
    fun hideOverlay() {
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
                binding = null
                _isOverlayVisible.value = false
                
                Log.i(TAG, "Overlay hidden")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding overlay", e)
        }
    }
    
    /**
     * Toggle overlay visibility
     */
    fun toggleOverlay() {
        if (_isOverlayVisible.value) {
            hideOverlay()
        } else {
            showOverlay()
        }
    }
    
    /**
     * Start the overlay update loop
     */
    private fun startUpdateLoop() {
        overlayScope.launch {
            while (_isOverlayVisible.value && isActive) {
                try {
                    // Update overlay with latest stats
                    // This would be connected to the OptimizationService state
                    delay(1000) // Update every second
                } catch (e: Exception) {
                    Log.e(TAG, "Error in overlay update loop", e)
                    delay(1000)
                }
            }
        }
    }
    
    /**
     * Update overlay statistics
     */
    fun updateStats(fps: Int, temperature: Float, ramUsage: Float, mode: String) {
        binding?.let {
            it.tvFps.text = "$fps"
            it.tvTemperature.text = "${temperature.toInt()}°C"
            it.tvRamUsage.text = "${ramUsage.toInt()}%"
            it.tvMode.text = mode
        }
    }
    
    inner class OverlayBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }
    
    companion object {
        const val ACTION_SHOW_OVERLAY = "com.ultraboost.engine.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.ultraboost.engine.HIDE_OVERLAY"
        const val ACTION_TOGGLE_OVERLAY = "com.ultraboost.engine.TOGGLE_OVERLAY"
    }
}
