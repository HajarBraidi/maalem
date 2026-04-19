package com.example.maalem.presentation.citizen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Artisan
import com.example.maalem.databinding.ItemArtisanBinding

class ArtisanAdapter(
    private val onClick: (Artisan) -> Unit
) : ListAdapter<Artisan, ArtisanAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemArtisanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArtisanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artisan = getItem(position)
        with(holder.binding) {
            tvName.text = artisan.name
            tvSpecialty.text = artisan.specialty
            tvCity.text = "📍 ${artisan.city}"
            root.setOnClickListener { onClick(artisan) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Artisan>() {
        override fun areItemsTheSame(a: Artisan, b: Artisan) = a.uid == b.uid
        override fun areContentsTheSame(a: Artisan, b: Artisan) = a == b
    }
}