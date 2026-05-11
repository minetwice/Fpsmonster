package com.ultraboost.engine.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ultraboost.engine.databinding.ActivityMainBinding
import com.ultraboost.engine.service.OptimizationService
import com.ultraboost.engine.utils.PreferenceManager
import kotlinx.coroutines.launch

/**
 * Main Activity - UltraBoost Engine Home Screen
 */
class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_OVERLAY_PERMISSION = 1001
    }
    
    private lateinit var binding: ActivityMainBinding
    
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
        )
    } else {
        arrayOf(
            Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.FOREGROUND_SERVICE
        )
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            checkOverlayPermission()
        } else {
            // Show permission denied message
            showPermissionRequired()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    override fun onResume() {
        super.onResume()
        updateOptimizationStatus()
    }
    
    private fun setupUI() {
        // Boost Button
        binding.btnBoost.setOnClickListener {
            toggleOptimization()
        }
        
        // App Selection
        binding.cardSelectApp.setOnClickListener {
            startActivity(Intent(this, AppSelectionActivity::class.java))
        }
        
        // Profile Management
        binding.cardProfiles.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        // Settings
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Overlay Toggle
        binding.switchOverlay.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.overlayEnabled = isChecked
            if (isChecked) {
                checkOverlayPermission()
            }
        }
        
        // Update switch state
        binding.switchOverlay.isChecked = PreferenceManager.overlayEnabled
    }
    
    private fun checkPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            checkOverlayPermission()
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // Request overlay permission
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                // Overlay permission granted
            } else {
                // Overlay permission denied
                binding.switchOverlay.isChecked = false
            }
        }
    }
    
    private fun toggleOptimization() {
        val serviceIntent = Intent(this, OptimizationService::class.java)
        
        if (isOptimizationActive()) {
            // Stop optimization
            serviceIntent.action = OptimizationService.ACTION_STOP_OPTIMIZATION
            startForegroundService(serviceIntent)
            
            updateBoostButtonState(false)
        } else {
            // Start optimization
            serviceIntent.action = OptimizationService.ACTION_START_OPTIMIZATION
            serviceIntent.putExtra(OptimizationService.EXTRA_MODE, PreferenceManager.optimizationMode.name)
            startForegroundService(serviceIntent)
            
            updateBoostButtonState(true)
            
            // Request battery optimization exemption
            requestBatteryOptimizationExemption()
        }
    }
    
    private fun isOptimizationActive(): Boolean {
        // Check if service is running
        // This is a simplified check - in production, bind to the service
        return false
    }
    
    private fun updateBoostButtonState(isActive: Boolean) {
        binding.btnBoost.isSelected = isActive
        
        if (isActive) {
            binding.tvBoostStatus.text = "Optimization Active"
            binding.tvBoostSubstatus.text = PreferenceManager.optimizationMode.displayName
        } else {
            binding.tvBoostStatus.text = "Ready to Boost"
            binding.tvBoostSubstatus.text = "Tap to start optimization"
        }
    }
    
    private fun updateOptimizationStatus() {
        // Update UI with current optimization state
        lifecycleScope.launch {
            // This would observe the service state flow in production
            updateBoostButtonState(false)
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting battery optimization exemption", e)
        }
    }
    
    private fun showPermissionRequired() {
        binding.tvPermissionStatus.visibility = View.VISIBLE
        binding.tvPermissionStatus.text = "Some permissions are required for full functionality. Please grant them in settings."
    }
}
