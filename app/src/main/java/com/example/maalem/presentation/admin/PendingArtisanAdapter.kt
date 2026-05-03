package com.example.maalem.presentation.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Artisan
import com.example.maalem.databinding.ItemPendingArtisanBinding
import java.text.SimpleDateFormat
import java.util.*

class PendingArtisanAdapter(
    private val onApprove: (Artisan) -> Unit, //Cette classe reçoit deux actions en paramètres :onApprove ,onReject
    private val onReject: (Artisan) -> Unit
) : ListAdapter<Artisan, PendingArtisanAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemPendingArtisanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingArtisanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artisan = getItem(position)
        with(holder.binding) {
            tvName.text = artisan.name
            tvEmail.text = artisan.email
            tvPhone.text = artisan.phone.ifEmpty { "Non renseigné" }
            tvSpecialty.text = artisan.specialty.ifEmpty { "Non renseignée" }
            tvCity.text = artisan.city.ifEmpty { "Non renseignée" }
            tvBio.text = artisan.bio.ifEmpty { "Aucune bio" }

            // Formater la date
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(artisan.createdAt))
            tvCreatedAt.text = date

            btnApprove.setOnClickListener { onApprove(artisan) }
            btnReject.setOnClickListener { onReject(artisan) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Artisan>() {
        override fun areItemsTheSame(a: Artisan, b: Artisan) = a.uid == b.uid
        override fun areContentsTheSame(a: Artisan, b: Artisan) = a == b
    }
}