package com.ultraboost.engine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultraboost.engine.databinding.ActivityAppSelectionBinding
import com.ultraboost.engine.detection.AppDetectionEngine
import com.ultraboost.engine.utils.PreferenceManager

/**
 * App Selection Activity - Select games to optimize
 */
class AppSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var appDetectionEngine: AppDetectionEngine
    private lateinit var appAdapter: AppListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Game"
        
        appDetectionEngine = AppDetectionEngine(this)
        setupRecyclerView()
        loadInstalledApps()
    }
    
    private fun setupRecyclerView() {
        appAdapter = AppListAdapter(
            onAppSelected = { appInfo ->
                PreferenceManager.selectedAppPackage = appInfo.packageName
                
                if (PreferenceManager.isFavoriteApp(appInfo.packageName)) {
                    PreferenceManager.removeFavoriteApp(appInfo.packageName)
                } else {
                    PreferenceManager.addFavoriteApp(appInfo.packageName)
                }
                
                appAdapter.notifyDataSetChanged()
            },
            isFavorite = { packageName ->
                PreferenceManager.isFavoriteApp(packageName)
            }
        )
        
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewApps.adapter = appAdapter
    }
    
    private fun loadInstalledApps() {
        val apps = appDetectionEngine.getInstalledGames()
        appAdapter.submitList(apps)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
