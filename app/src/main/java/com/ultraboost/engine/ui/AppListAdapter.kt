package com.ultraboost.engine.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultraboost.engine.databinding.ItemAppBinding
import com.ultraboost.engine.detection.AppInfo

/**
 * RecyclerView Adapter for app list
 */
class AppListAdapter(
    private val onAppSelected: (AppInfo) -> Unit,
    private val isFavorite: (String) -> Boolean
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo) {
            binding.apply {
                tvAppName.text = appInfo.appName
                tvPackageName.text = appInfo.packageName
                ivAppIcon.setImageDrawable(appInfo.icon)
                
                val favorite = isFavorite(appInfo.packageName)
                btnFavorite.isSelected = favorite
                
                root.setOnClickListener {
                    onAppSelected(appInfo)
                }
                
                btnFavorite.setOnClickListener {
                    onAppSelected(appInfo)
                }
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
