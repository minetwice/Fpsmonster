package com.ultraboost.engine.optimization

/**
 * Optimization Modes for different gaming scenarios
 * Each mode provides different levels of optimization intensity
 */
enum class OptimizationMode(
    val displayName: String,
    val description: String,
    val intensity: Int, // 1-5 scale
    val thermalPriority: Boolean,
    val batteryImpact: BatteryImpact
) {
    
    /**
     * Balanced Mode - Safe optimization with stable thermals
     * Best for everyday gaming with good battery life
     */
    BALANCED(
        displayName = "Balanced",
        description = "Safe optimization with stable thermals and balanced battery usage",
        intensity = 2,
        thermalPriority = true,
        batteryImpact = BatteryImpact.LOW
    ),
    
    /**
     * Gaming Mode - Aggressive frame stabilization
     * Lower latency, better responsiveness for competitive gaming
     */
    GAMING(
        displayName = "Gaming",
        description = "Aggressive frame stabilization with lower latency and better responsiveness",
        intensity = 3,
        thermalPriority = false,
        batteryImpact = BatteryImpact.MEDIUM
    ),
    
    /**
     * Recording Mode - Optimized for screen recording and voice chat
     * Prioritizes stable frametimes during multitasking
     */
    RECORDING(
        displayName = "Recording",
        description = "Optimized for screen recording and Discord voice chat with stable frametimes",
        intensity = 3,
        thermalPriority = true,
        batteryImpact = BatteryImpact.MEDIUM
    ),
    
    /**
     * PvP Mode - Ultra-low latency for competitive play
     * Focus on touch responsiveness and frametime consistency
     */
    PVP(
        displayName = "PvP",
        description = "Ultra-low latency with focus on touch responsiveness and frame consistency",
        intensity = 4,
        thermalPriority = false,
        batteryImpact = BatteryImpact.HIGH
    ),
    
    /**
     * Extreme Mode - Maximum safe optimization
     * Aggressive performance balancing while respecting thermal safety
     */
    EXTREME(
        displayName = "Extreme",
        description = "Maximum safe optimization with aggressive performance balancing",
        intensity = 5,
        thermalPriority = false,
        batteryImpact = BatteryImpact.VERY_HIGH
    );
    
    companion object {
        fun getAllModes(): List<OptimizationMode> = values().toList()
        
        fun getSafeModes(): List<OptimizationMode> = listOf(BALANCED, GAMING, RECORDING)
        
        fun getPerformanceModes(): List<OptimizationMode> = listOf(GAMING, PVP, EXTREME)
    }
}

/**
 * Battery Impact Levels
 */
enum class BatteryImpact(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    VERY_HIGH("Very High")
}
