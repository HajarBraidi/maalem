package com.example.maalem.presentation.artisan

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.R
import com.example.maalem.data.model.Offer

class OfferAdapter(
    private var offers: List<Offer>
) : RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {

    class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPrice: TextView = view.findViewById(R.id.tvOfferPrice)
        val tvStatus: TextView = view.findViewById(R.id.tvOfferStatus)
        val tvDelay: TextView = view.findViewById(R.id.tvOfferDelay)
        val tvMessage: TextView = view.findViewById(R.id.tvOfferMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer = offers[position]
        holder.tvPrice.text = "${offer.price} DH"
        holder.tvDelay.text = "Délai : ${offer.delay}"
        holder.tvMessage.text = offer.message

        when (offer.status) {
            "pending" -> {
                holder.tvStatus.text = "En attente"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF9C4"))
            }
            "accepted" -> {
                holder.tvStatus.text = "Acceptée"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#C8E6C9"))
            }
            "rejected" -> {
                holder.tvStatus.text = "Refusée"
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FFCDD2"))
            }
        }
    }

    override fun getItemCount() = offers.size

    fun updateOffers(newOffers: List<Offer>) {
        offers = newOffers
        notifyDataSetChanged()
    }
}