package com.ultraboost.engine.jni

/**
 * JNI Bridge for Native Performance Engine
 * Connects Kotlin Android layer with C++ native optimization engine
 */
class NativeEngineBridge {
    
    companion object {
        // Load native library
        init {
            try {
                System.loadLibrary("ultraboost_native")
            } catch (e: UnsatisfiedLinkError) {
                // Native library not available in debug builds without NDK
                e.printStackTrace()
            }
        }
        
        /**
         * Initialize the native optimization engine
         * @param mode Optimization mode intensity (1-5)
         * @return true if initialization successful
         */
        external fun nativeInit(mode: Int): Boolean
        
        /**
         * Start optimization loop
         * @param targetFps Target FPS for frame pacing
         * @param thermalLimit Thermal limit in Celsius
         */
        external fun nativeStartOptimization(targetFps: Int, thermalLimit: Float)
        
        /**
         * Stop optimization loop
         */
        external fun nativeStopOptimization()
        
        /**
         * Update optimization parameters dynamically
         * @param intensity New intensity level (0-100)
         * @param priority Thread priority adjustment
         */
        external fun nativeUpdateParameters(intensity: Int, priority: Int)
        
        /**
         * Get current frame time from native engine
         * @return Frame time in milliseconds
         */
        external fun nativeGetFrameTimeMs(): Float
        
        /**
         * Get jitter measurement
         * @return Jitter in milliseconds
         */
        external fun nativeGetJitterMs(): Float
        
        /**
         * Request CPU thread balancing
         * @param coreMask Bitmask of cores to use
         */
        external fun nativeRequestThreadBalance(coreMask: Int)
        
        /**
         * Optimize for low latency
         * @param enabled Enable/disable low latency mode
         */
        external fun nativeSetLowLatencyMode(enabled: Boolean)
        
        /**
         * Record frame time sample for analysis
         * @param frameTime Frame time in milliseconds
         */
        external fun nativeRecordFrameSample(frameTime: Float)
        
        /**
         * Get optimization statistics
         * @return Array containing [avgFrameTime, minFrameTime, maxFrameTime, jitter]
         */
        external fun nativeGetStats(): FloatArray
        
        /**
         * Cleanup native resources
         */
        external fun nativeCleanup()
        
        /**
         * Check if native engine is available
         */
        fun isNativeEngineAvailable(): Boolean {
            return try {
                // Try to call a simple native method
                nativeGetFrameTimeMs() >= 0
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
