package com.ultraboost.engine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ultraboost.engine.databinding.ActivityProfileBinding
import com.ultraboost.engine.optimization.OptimizationMode
import com.ultraboost.engine.utils.PreferenceManager

/**
 * Profile Activity - Manage optimization profiles
 */
class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Optimization Profiles"
        
        setupProfiles()
    }
    
    private fun setupProfiles() {
        // Display all available modes
        val modes = OptimizationMode.getAllModes()
        
        for (mode in modes) {
            // Create profile cards dynamically or use RecyclerView
        }
        
        // Set current mode
        updateCurrentModeDisplay()
    }
    
    private fun updateCurrentModeDisplay() {
        val currentMode = PreferenceManager.optimizationMode
        // Update UI to show current mode
    }
    
    fun selectMode(mode: OptimizationMode) {
        PreferenceManager.optimizationMode = mode
        updateCurrentModeDisplay()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
