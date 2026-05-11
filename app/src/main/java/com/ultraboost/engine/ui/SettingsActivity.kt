package com.ultraboost.engine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ultraboost.engine.databinding.ActivitySettingsBinding
import com.ultraboost.engine.utils.PreferenceManager

/**
 * Settings Activity - App configuration
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        setupSettings()
    }
    
    private fun setupSettings() {
        // Thermal threshold
        binding.seekbarThermalThreshold.progress = PreferenceManager.thermalThreshold
        
        binding.seekbarThermalThreshold.setOnSeekBarChangeListener(
            object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        PreferenceManager.thermalThreshold = progress
                        binding.tvThermalValue.text = "$progress°C"
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            }
        )
        
        binding.tvThermalValue.text = "${PreferenceManager.thermalThreshold}°C"
        
        // Auto-start toggle
        binding.switchAutoStart.isChecked = PreferenceManager.autoStart
        binding.switchAutoStart.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.autoStart = isChecked
        }
        
        // Overlay toggle
        binding.switchOverlayEnable.isChecked = PreferenceManager.overlayEnabled
        binding.switchOverlayEnable.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.overlayEnabled = isChecked
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
