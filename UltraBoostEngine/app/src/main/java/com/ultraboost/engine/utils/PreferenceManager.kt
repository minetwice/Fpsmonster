package com.ultraboost.engine.utils

import android.content.Context
import android.content.SharedPreferences
import com.ultraboost.engine.optimization.OptimizationMode

/**
 * Preference Manager for storing app settings and profiles
 */
object PreferenceManager {

    private lateinit var prefs: SharedPreferences
    
    // Keys
    private const val PREFS_NAME = "ultraboost_prefs"
    private const val KEY_SELECTED_APP = "selected_app_package"
    private const val KEY_OPTIMIZATION_MODE = "optimization_mode"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
    private const val KEY_AUTO_START = "auto_start"
    private const val KEY_THERMAL_THRESHOLD = "thermal_threshold"
    private const val KEY_FAVORITES = "favorite_apps"
    
    // Default values
    const val DEFAULT_THERMAL_THRESHOLD = 45
    
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Selected App
    var selectedAppPackage: String?
        get() = prefs.getString(KEY_SELECTED_APP, null)
        set(value) = prefs.edit().putString(KEY_SELECTED_APP, value).apply()
    
    // Optimization Mode
    var optimizationMode: OptimizationMode
        get() {
            val modeName = prefs.getString(KEY_OPTIMIZATION_MODE, OptimizationMode.BALANCED.name) ?: OptimizationMode.BALANCED.name
            return try {
                OptimizationMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                OptimizationMode.BALANCED
            }
        }
        set(value) = prefs.edit().putString(KEY_OPTIMIZATION_MODE, value.name).apply()
    
    // Overlay Enabled
    var overlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, value).apply()
    
    // Auto Start on Boot
    var autoStart: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START, value).apply()
    
    // Thermal Threshold (Celsius)
    var thermalThreshold: Int
        get() = prefs.getInt(KEY_THERMAL_THRESHOLD, DEFAULT_THERMAL_THRESHOLD)
        set(value) = prefs.edit().putInt(KEY_THERMAL_THRESHOLD, value).apply()
    
    // Favorite Apps
    fun getFavoriteApps(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }
    
    fun addFavoriteApp(packageName: String) {
        val favorites = getFavoriteApps().toMutableSet()
        favorites.add(packageName)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }
    
    fun removeFavoriteApp(packageName: String) {
        val favorites = getFavoriteApps().toMutableSet()
        favorites.remove(packageName)
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }
    
    fun isFavoriteApp(packageName: String): Boolean {
        return getFavoriteApps().contains(packageName)
    }
    
    // Clear all preferences
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
