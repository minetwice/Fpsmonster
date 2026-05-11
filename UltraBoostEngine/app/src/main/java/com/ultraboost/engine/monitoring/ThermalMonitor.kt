package com.ultraboost.engine.monitoring

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.ultraboost.engine.optimization.ThermalState
import com.ultraboost.engine.optimization.MemoryState
import java.io.File

/**
 * Thermal Monitoring System
 * Monitors CPU and device temperatures safely
 */
class ThermalMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "ThermalMonitor"
        
        // Common thermal zone paths (may vary by device)
        val THERMAL_ZONES = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp",
            "/sys/class/thermal/thermal_zone3/temp",
            "/sys/class/thermal/thermal_zone4/temp",
            "/sys/class/thermal/thermal_zone5/temp"
        )
        
        // Default safe temperature threshold (Celsius)
        const val DEFAULT_SAFE_TEMP = 45f
        const val WARNING_TEMP = 50f
        const val CRITICAL_TEMP = 60f
    }
    
    private val _thermalState = MutableStateFlow(ThermalState())
    val thermalState: StateFlow<ThermalState> = _thermalState.asStateFlow()
    
    private var isMonitoring = false
    
    /**
     * Get CPU temperature from thermal zones
     * Returns temperature in Celsius
     */
    fun getCpuTemperature(): Float {
        for (path in THERMAL_ZONES) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val tempString = file.readText().trim()
                    // Temperature is usually in millidegrees Celsius
                    val temp = tempString.toFloatOrNull() ?: continue
                    return if (temp > 1000) temp / 1000f else temp
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading thermal zone: $path", e)
            }
        }
        
        // Fallback: estimate from battery temperature
        return getBatteryTemperature()
    }
    
    /**
     * Get battery temperature
     */
    fun getBatteryTemperature(): Float {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryTemp = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE)
            batteryTemp / 10f // Convert from decicelsius to Celsius
        } catch (e: Exception) {
            30f // Default fallback
        }
    }
    
    /**
     * Update thermal state
     */
    fun updateThermalState(safeThreshold: Float = DEFAULT_SAFE_TEMP) {
        val cpuTemp = getCpuTemperature()
        val batteryTemp = getBatteryTemperature()
        
        val isOverheating = cpuTemp >= WARNING_TEMP || batteryTemp >= WARNING_TEMP
        val isThrottling = cpuTemp >= CRITICAL_TEMP
        
        _thermalState.value = ThermalState(
            cpuTemperature = cpuTemp,
            gpuTemperature = cpuTemp, // Estimate (GPU temp often same as CPU on mobile)
            batteryTemperature = batteryTemp,
            skinTemperature = (cpuTemp + batteryTemp) / 2f, // Estimate
            isOverheating = isOverheating,
            thermalThrottlingActive = isThrottling,
            safeTemperatureThreshold = safeThreshold
        )
        
        if (isOverheating) {
            Log.w(TAG, "Device overheating detected! CPU: $cpuTemp°C, Battery: $batteryTemp°C")
        }
    }
    
    /**
     * Check if temperature is safe for optimization
     */
    fun isTemperatureSafe(threshold: Float = DEFAULT_SAFE_TEMP): Boolean {
        return _thermalState.value.cpuTemperature < threshold
    }
    
    /**
     * Get recommended optimization intensity based on temperature
     * Returns value from 0.0 to 1.0
     */
    fun getRecommendedIntensity(threshold: Float = DEFAULT_SAFE_TEMP): Float {
        val currentTemp = _thermalState.value.cpuTemperature
        
        return when {
            currentTemp < threshold - 5 -> 1.0f // Full intensity
            currentTemp < threshold -> 0.8f // High intensity
            currentTemp < WARNING_TEMP -> 0.5f // Medium intensity
            currentTemp < CRITICAL_TEMP -> 0.3f // Low intensity
            else -> 0.1f // Minimal intensity (cooling down)
        }
    }
}

/**
 * Memory Monitor
 * Monitors RAM usage and memory pressure
 */
class MemoryMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "MemoryMonitor"
        const val HIGH_PRESSURE_THRESHOLD = 0.85f // 85% usage
    }
    
    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()
    
    /**
     * Update memory state
     */
    fun updateMemoryState() {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalRam = memoryInfo.totalMem
            val availableRam = memoryInfo.availMem
            val usedRam = totalRam - availableRam
            val usagePercent = if (totalRam > 0) usedRam.toFloat() / totalRam else 0f
            val isPressureHigh = usagePercent >= HIGH_PRESSURE_THRESHOLD
            
            _memoryState.value = MemoryState(
                totalRam = totalRam,
                usedRam = usedRam,
                availableRam = availableRam,
                usagePercent = usagePercent,
                isPressureHigh = isPressureHigh
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating memory state", e)
        }
    }
    
    /**
     * Get human-readable RAM size
     */
    fun formatRamSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000L -> String.format("%.2f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000L -> String.format("%.2f MB", bytes / 1_000_000.0)
            bytes >= 1_000L -> String.format("%.2f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}

/**
 * Performance Monitor
 * Monitors overall system performance metrics
 */
class PerformanceMonitor {
    
    companion object {
        private const val TAG = "PerformanceMonitor"
    }
    
    private val _cpuUsage = MutableStateFlow(0f)
    val cpuUsage: StateFlow<Float> = _cpuUsage.asStateFlow()
    
    /**
     * Estimate CPU usage (simplified)
     * Note: Accurate CPU monitoring requires root or special permissions on Android 11+
     */
    fun updateCpuUsage() {
        // Simplified estimation based on available methods
        // For accurate readings, native implementation would be needed
        try {
            // Read from /proc/stat (if accessible)
            val statFile = File("/proc/stat")
            if (statFile.exists() && statFile.canRead()) {
                val lines = statFile.readLines()
                for (line in lines) {
                    if (line.startsWith("cpu ")) {
                        // Parse CPU stats
                        val parts = line.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                        if (parts.size >= 8) {
                            val user = parts[1].toLongOrNull() ?: 0
                            val nice = parts[2].toLongOrNull() ?: 0
                            val system = parts[3].toLongOrNull() ?: 0
                            val idle = parts[4].toLongOrNull() ?: 0
                            
                            val total = user + nice + system + idle
                            val used = user + nice + system
                            
                            _cpuUsage.value = if (total > 0) used.toFloat() / total else 0f
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading CPU stats", e)
            // Use estimation based on active processes
            _cpuUsage.value = 0.5f // Default estimate
        }
    }
}
