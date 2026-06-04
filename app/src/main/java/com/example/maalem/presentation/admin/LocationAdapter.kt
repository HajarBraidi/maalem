package com.example.maalem.presentation.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.AppLocation
import com.example.maalem.databinding.ItemLocationBinding

class LocationAdapter(
    private val onDelete: (AppLocation) -> Unit
) : ListAdapter<AppLocation, LocationAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = getItem(position)
        holder.binding.tvLocationName.text = " ${location.name}"
        holder.binding.tvLocationCoords.text =
            "Lat: ${location.latitude}, Lng: ${location.longitude}"
        holder.binding.btnDeleteLocation.setOnClickListener { onDelete(location) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AppLocation>() {
        override fun areItemsTheSame(a: AppLocation, b: AppLocation) = a.id == b.id
        override fun areContentsTheSame(a: AppLocation, b: AppLocation) = a == b
    }
}