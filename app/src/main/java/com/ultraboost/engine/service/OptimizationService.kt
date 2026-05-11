package com.ultraboost.engine.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ultraboost.engine.R
import com.ultraboost.engine.UltraBoostApplication
import com.ultraboost.engine.detection.AppDetectionEngine
import com.ultraboost.engine.jni.NativeEngineBridge
import com.ultraboost.engine.monitoring.MemoryMonitor
import com.ultraboost.engine.monitoring.PerformanceMonitor
import com.ultraboost.engine.monitoring.ThermalMonitor
import com.ultraboost.engine.optimization.OptimizationMode
import com.ultraboost.engine.optimization.OptimizationState
import com.ultraboost.engine.utils.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Main Optimization Foreground Service
 * Runs continuously to provide real-time gaming optimization
 */
class OptimizationService : Service() {
    
    companion object {
        private const val TAG = "OptimizationService"
        private const val NOTIFICATION_ID = 1001
        
        // Update intervals (milliseconds)
        private const val UPDATE_INTERVAL_FAST = 500L // 0.5 seconds
        private const val UPDATE_INTERVAL_NORMAL = 1000L // 1 second
        private const val UPDATE_INTERVAL_SLOW = 3000L // 3 seconds
    }
    
    // Binder for activity binding
    private val binder = OptimizationBinder()
    
    // Monitoring components
    private lateinit var thermalMonitor: ThermalMonitor
    private lateinit var memoryMonitor: MemoryMonitor
    private lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var appDetectionEngine: AppDetectionEngine
    
    // Service state
    private val _serviceState = MutableStateFlow(OptimizationState())
    val serviceState: StateFlow<OptimizationState> = _serviceState.asStateFlow()
    
    private var isOptimizationActive = false
    private var monitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Current optimization mode
    private var currentMode: OptimizationMode = OptimizationMode.BALANCED
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize monitors
        thermalMonitor = ThermalMonitor(this)
        memoryMonitor = MemoryMonitor(this)
        performanceMonitor = PerformanceMonitor()
        appDetectionEngine = AppDetectionEngine(this)
        
        Log.i(TAG, "OptimizationService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_OPTIMIZATION -> {
                val mode = intent.getStringExtra(EXTRA_MODE)?.let { 
                    try {
                        OptimizationMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        OptimizationMode.BALANCED
                    }
                } ?: OptimizationMode.BALANCED
                
                startOptimization(mode)
            }
            
            ACTION_STOP_OPTIMIZATION -> {
                stopOptimization()
            }
            
            ACTION_UPDATE_MODE -> {
                val mode = intent.getStringExtra(EXTRA_MODE)?.let {
                    try {
                        OptimizationMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        OptimizationMode.BALANCED
                    }
                }
                
                if (mode != null) {
                    updateOptimizationMode(mode)
                }
            }
        }
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopOptimization()
        serviceScope.cancel()
        NativeEngineBridge.nativeCleanup()
        Log.i(TAG, "OptimizationService destroyed")
    }
    
    /**
     * Start optimization for a specific mode
     */
    fun startOptimization(mode: OptimizationMode = PreferenceManager.optimizationMode) {
        if (isOptimizationActive) {
            Log.w(TAG, "Optimization already active")
            return
        }
        
        currentMode = mode
        isOptimizationActive = true
        
        // Initialize native engine
        val initSuccess = NativeEngineBridge.nativeInit(mode.intensity)
        Log.i(TAG, "Native engine initialized: $initSuccess")
        
        // Start monitoring loop
        startMonitoringLoop()
        
        // Update state
        _serviceState.value = _serviceState.value.copy(
            isActive = true,
            currentMode = mode,
            targetApp = PreferenceManager.selectedAppPackage,
            optimizationIntensity = mode.intensity
        )
        
        appDetectionEngine.setTargetApp(PreferenceManager.selectedAppPackage)
        
        Log.i(TAG, "Optimization started with mode: ${mode.displayName}")
    }
    
    /**
     * Stop optimization
     */
    fun stopOptimization() {
        if (!isOptimizationActive) return
        
        isOptimizationActive = false
        monitoringJob?.cancel()
        
        NativeEngineBridge.nativeStopOptimization()
        
        _serviceState.value = _serviceState.value.copy(
            isActive = false,
            optimizationIntensity = 0
        )
        
        Log.i(TAG, "Optimization stopped")
    }
    
    /**
     * Update optimization mode dynamically
     */
    fun updateOptimizationMode(mode: OptimizationMode) {
        currentMode = mode
        PreferenceManager.optimizationMode = mode
        
        // Update native engine parameters
        NativeEngineBridge.nativeUpdateParameters(mode.intensity, getThreadPriority(mode))
        
        _serviceState.value = _serviceState.value.copy(
            currentMode = mode,
            optimizationIntensity = mode.intensity
        )
        
        Log.i(TAG, "Optimization mode updated to: ${mode.displayName}")
    }
    
    /**
     * Start the monitoring and optimization loop
     */
    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isOptimizationActive && isActive) {
                try {
                    // Update all monitors
                    thermalMonitor.updateThermalState(PreferenceManager.thermalThreshold.toFloat())
                    memoryMonitor.updateMemoryState()
                    performanceMonitor.updateCpuUsage()
                    appDetectionEngine.updateForegroundApp()
                    
                    // Get latest states
                    val thermalState = thermalMonitor.thermalState.value
                    val memoryState = memoryMonitor.memoryState.value
                    val cpuUsage = performanceMonitor.cpuUsage.value
                    
                    // Adaptive optimization based on conditions
                    adaptOptimizationToConditions(thermalState, memoryState)
                    
                    // Get frame time from native engine
                    val frameTime = NativeEngineBridge.nativeGetFrameTimeMs()
                    val jitter = NativeEngineBridge.nativeGetJitterMs()
                    
                    // Estimate FPS from frame time
                    val fps = if (frameTime > 0) (1000f / frameTime).toInt() else 60
                    
                    // Update service state
                    _serviceState.value = _serviceState.value.copy(
                        temperature = thermalState.cpuTemperature,
                        ramUsagePercent = memoryState.usagePercent * 100,
                        cpuUsagePercent = cpuUsage * 100,
                        frameTimeMs = frameTime,
                        isRecording = appDetectionEngine.isRecordingActive.value,
                        isDiscordActive = appDetectionEngine.isDiscordActive.value,
                        thermalThrottling = thermalState.thermalThrottlingActive,
                        fpsEstimate = fps
                    )
                    
                    // Update notification
                    updateNotification()
                    
                    // Delay based on mode intensity
                    val delay = when (currentMode.intensity) {
                        5 -> UPDATE_INTERVAL_FAST
                        4, 3 -> UPDATE_INTERVAL_NORMAL
                        else -> UPDATE_INTERVAL_SLOW
                    }
                    
                    delay(delay)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitoring loop", e)
                    delay(UPDATE_INTERVAL_SLOW)
                }
            }
        }
    }
    
    /**
     * Adapt optimization based on current conditions
     */
    private fun adaptOptimizationToConditions(
        thermalState: com.ultraboost.engine.optimization.ThermalState,
        memoryState: com.ultraboost.engine.optimization.MemoryState
    ) {
        // Get recommended intensity based on temperature
        val thermalIntensity = thermalMonitor.getRecommendedIntensity(PreferenceManager.thermalThreshold.toFloat())
        
        // Reduce intensity if memory pressure is high
        val memoryFactor = if (memoryState.isPressureHigh) 0.7f else 1.0f
        
        // Calculate final intensity
        val finalIntensity = thermalIntensity * memoryFactor
        
        // Apply adaptive changes
        if (finalIntensity < 0.5f) {
            // Reduce optimization pressure
            NativeEngineBridge.nativeUpdateParameters((currentMode.intensity * finalIntensity).toInt(), 0)
            
            if (thermalState.isOverheating) {
                Log.w(TAG, "Device overheating - reducing optimization intensity")
            }
        }
        
        // Enable low latency mode for PvP/Extreme modes
        if (currentMode == OptimizationMode.PVP || currentMode == OptimizationMode.EXTREME) {
            NativeEngineBridge.nativeSetLowLatencyMode(true)
        }
    }
    
    /**
     * Get thread priority based on optimization mode
     */
    private fun getThreadPriority(mode: OptimizationMode): Int {
        return when (mode) {
            OptimizationMode.BALANCED -> 0
            OptimizationMode.GAMING -> 1
            OptimizationMode.RECORDING -> 1
            OptimizationMode.PVP -> 2
            OptimizationMode.EXTREME -> 2
        }
    }
    
    /**
     * Create foreground service notification
     */
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.ultraboost.engine.ui.MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, UltraBoostApplication.CHANNEL_OPTIMIZATION)
            .setContentTitle("UltraBoost Engine")
            .setContentText("Optimization Active - ${currentMode.displayName} Mode")
            .setSmallIcon(R.drawable.ic_boost)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * Update notification with current stats
     */
    private fun updateNotification() {
        val state = _serviceState.value
        
        val notification = NotificationCompat.Builder(this, UltraBoostApplication.CHANNEL_OPTIMIZATION)
            .setContentTitle("UltraBoost Engine")
            .setContentText("${state.fpsEstimate} FPS | ${state.temperature.toInt()}°C | ${currentMode.displayName}")
            .setSmallIcon(R.drawable.ic_boost)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Binder class for activity communication
     */
    inner class OptimizationBinder : Binder() {
        fun getService(): OptimizationService = this@OptimizationService
    }
    
    companion object {
        const val ACTION_START_OPTIMIZATION = "com.ultraboost.engine.START_OPTIMIZATION"
        const val ACTION_STOP_OPTIMIZATION = "com.ultraboost.engine.STOP_OPTIMIZATION"
        const val ACTION_UPDATE_MODE = "com.ultraboost.engine.UPDATE_MODE"
        const val EXTRA_MODE = "extra_mode"
    }
}
