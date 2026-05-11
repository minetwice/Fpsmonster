package com.ultraboost.engine.optimization

/**
 * Optimization State Data Class
 * Represents the current state of the optimization engine
 */
data class OptimizationState(
    val isActive: Boolean = false,
    val currentMode: OptimizationMode = OptimizationMode.BALANCED,
    val targetApp: String? = null,
    val fpsEstimate: Int = 0,
    val temperature: Float = 0f,
    val ramUsagePercent: Float = 0f,
    val cpuUsagePercent: Float = 0f,
    val frameTimeMs: Float = 0f,
    val isRecording: Boolean = false,
    val isDiscordActive: Boolean = false,
    val thermalThrottling: Boolean = false,
    val optimizationIntensity: Int = 0
)

/**
 * Performance Metrics Data Class
 * Real-time performance statistics
 */
data class PerformanceMetrics(
    val fps: Int = 0,
    val frameTimeMs: Float = 0f,
    val jitterMs: Float = 0f,
    val minFrameTimeMs: Float = 0f,
    val maxFrameTimeMs: Float = 0f,
    val avgFrameTimeMs: Float = 0f,
    val frameDrops: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Thermal State Data Class
 */
data class ThermalState(
    val cpuTemperature: Float = 0f,
    val gpuTemperature: Float = 0f,
    val batteryTemperature: Float = 0f,
    val skinTemperature: Float = 0f,
    val isOverheating: Boolean = false,
    val thermalThrottlingActive: Boolean = false,
    val safeTemperatureThreshold: Float = 45f
)

/**
 * Memory State Data Class
 */
data class MemoryState(
    val totalRam: Long = 0,
    val usedRam: Long = 0,
    val availableRam: Long = 0,
    val usagePercent: Float = 0f,
    val isPressureHigh: Boolean = false
)
