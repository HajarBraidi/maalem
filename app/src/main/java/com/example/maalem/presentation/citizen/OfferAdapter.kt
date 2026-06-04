package com.example.maalem.presentation.citizen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Offer
import com.example.maalem.data.model.OfferStatus
import com.example.maalem.databinding.ItemOfferBinding
import com.example.maalem.databinding.ItemOffersBinding

class OfferAdapter(
    private val onAccept: (Offer) -> Unit,
    private val onReject: (Offer) -> Unit,
    private val onViewArtisan: (Offer) -> Unit,
    private val onChat: (Offer) -> Unit

) : ListAdapter<Offer, OfferAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemOffersBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOffersBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val offer = getItem(position)
        with(holder.binding) {
            tvArtisanName.text = offer.artisanName
            tvPrice.text = "${offer.price} MAD"
            tvDelay.text = offer.delay
            tvMessage.text = offer.message
            btnChatArtisan.setOnClickListener { onChat(offer) }

            //  Badge + boutons selon statut
            when (OfferStatus.fromString(offer.status)) {
                OfferStatus.PENDING -> {
                    tvOfferStatus.text = "En attente"
                    tvOfferStatus.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_orange_dark)
                    )
                    layoutAcceptReject.isVisible = true
                }
                OfferStatus.ACCEPTED -> {
                    tvOfferStatus.text = "Acceptée"
                    tvOfferStatus.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
                    )
                    layoutAcceptReject.isVisible = false
                }
                OfferStatus.REJECTED -> {
                    tvOfferStatus.text = "Refusée"
                    tvOfferStatus.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                    )
                    layoutAcceptReject.isVisible = false
                }
            }

            btnViewArtisan.setOnClickListener { onViewArtisan(offer) }
            btnAccept.setOnClickListener { onAccept(offer) }
            btnReject.setOnClickListener { onReject(offer) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Offer>() {
        override fun areItemsTheSame(a: Offer, b: Offer) = a.id == b.id
        override fun areContentsTheSame(a: Offer, b: Offer) = a == b
    }
}