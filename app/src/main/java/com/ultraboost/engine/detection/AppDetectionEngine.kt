package com.ultraboost.engine.detection

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App Detection Engine
 * Monitors foreground applications and detects when selected apps are launched
 */
class AppDetectionEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "AppDetectionEngine"
        
        // Common recording apps
        val RECORDING_APPS = listOf(
            "com.google.android.apps.capture", // Google Screen Recorder
            "com.miui.screenrecorder", // MIUI Screen Recorder
            "com.samsung.screenrecorder", // Samsung Screen Recorder
            "com.oneplus.screenrecorder", // OnePlus Screen Recorder
            "com.noshufou.android.su.recorder", // Advanced Screen Recorder
            "com.kimcy929.screenrecorder" // Screen Recorder by Kimcy929
        )
        
        // Discord package name
        const val DISCORD_PACKAGE = "com.discord"
        
        // Minecraft related packages
        val MINECRAFT_PACKAGES = listOf(
            "net.kdt.pojavlaunch", // PojavLauncher
            "org.devanium.mojo", // MojoLauncher
            "com.mojang.minecraftpe", // Minecraft Bedrock
            "net.minecraft.launcher" // Official Minecraft Launcher
        )
    }
    
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager = context.packageManager
    
    private val _currentForegroundApp = MutableStateFlow<String?>(null)
    val currentForegroundApp: StateFlow<String?> = _currentForegroundApp.asStateFlow()
    
    private val _isTargetAppActive = MutableStateFlow(false)
    val isTargetAppActive: StateFlow<Boolean> = _isTargetAppActive.asStateFlow()
    
    private val _isRecordingActive = MutableStateFlow(false)
    val isRecordingActive: StateFlow<Boolean> = _isRecordingActive.asStateFlow()
    
    private val _isDiscordActive = MutableStateFlow(false)
    val isDiscordActive: StateFlow<Boolean> = _isDiscordActive.asStateFlow()
    
    private var targetPackageName: String? = null
    private var detectionJob: kotlinx.coroutines.Job? = null
    
    /**
     * Set the target application to monitor
     */
    fun setTargetApp(packageName: String?) {
        targetPackageName = packageName
        checkIfTargetAppActive()
    }
    
    /**
     * Get list of all installed gaming apps
     */
    fun getInstalledGames(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()
        
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
            intent.addCategory("android.intent.category.LAUNCHER")
            
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            }
            
            for (resolveInfo in resolveInfos) {
                val activityInfo = resolveInfo.activityInfo
                val packageName = activityInfo.packageName
                
                // Try to determine if it's a game
                val isGame = isLikelyGame(packageName)
                
                if (isGame || MINECRAFT_PACKAGES.contains(packageName)) {
                    val appName = activityInfo.loadLabel(packageManager).toString()
                    val appIcon = activityInfo.loadIcon(packageManager)
                    
                    apps.add(AppInfo(packageName, appName, appIcon, isGame))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed games", e)
        }
        
        return apps.sortedBy { it.appName.lowercase() }
    }
    
    /**
     * Check if a package is likely a game
     */
    private fun isLikelyGame(packageName: String): Boolean {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            }
            
            // Check if app has game category
            (appInfo.category == android.content.pm.ApplicationInfo.CATEGORY_GAME) ||
            // Check metadata for game flags
            (appInfo.metaData?.containsKey("game") == true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update foreground app detection using UsageStats
     */
    fun updateForegroundApp() {
        if (usageStatsManager == null) return
        
        try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 10000 // Last 10 seconds
            
            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            
            var lastForegroundApp: String? = null
            var lastForegroundTime = 0L
            
            while (usageEvents.hasNextEvent()) {
                val event = UsageEvents.Event()
                usageEvents.getNextEvent(event)
                
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (event.timeStamp > lastForegroundTime) {
                        lastForegroundTime = event.timeStamp
                        lastForegroundApp = event.packageName
                    }
                }
            }
            
            _currentForegroundApp.value = lastForegroundApp
            checkIfTargetAppActive()
            checkRecordingStatus()
            checkDiscordStatus()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating foreground app", e)
        }
    }
    
    /**
     * Check if target app is currently active
     */
    private fun checkIfTargetAppActive() {
        val currentApp = _currentForegroundApp.value
        _isTargetAppActive.value = targetPackageName != null && targetPackageName == currentApp
    }
    
    /**
     * Check if screen recording is active
     */
    private fun checkRecordingStatus() {
        val currentApp = _currentForegroundApp.value
        _isRecordingActive.value = currentApp != null && RECORDING_APPS.contains(currentApp)
    }
    
    /**
     * Check if Discord is active
     */
    private fun checkDiscordStatus() {
        val currentApp = _currentForegroundApp.value
        _isDiscordActive.value = currentApp == DISCORD_PACKAGE
    }
    
    /**
     * Get app info for a specific package
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = packageManager.getApplicationIcon(appInfo)
            
            AppInfo(packageName, appName, appIcon, isLikelyGame(packageName))
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Data class for app information
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?,
    val isGame: Boolean
)
