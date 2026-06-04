package com.example.maalem.presentation.citizen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Request
import com.example.maalem.data.model.RequestStatus
import com.example.maalem.databinding.ItemRequestBinding
import com.example.maalem.databinding.ItemRequestsBinding
import java.text.SimpleDateFormat
import java.util.*

class RequestAdapter(
    private val onViewOffers: (Request) -> Unit
) : ListAdapter<Request, RequestAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemRequestsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRequestsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = getItem(position)
        with(holder.binding) {

            tvTitle.text = request.title
            tvCategory.text = request.category
            tvCity.text = request.city
            tvDescription.text = request.description
            tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(request.createdAt))

            //  Badge statut + bouton selon statut
            when (RequestStatus.fromString(request.status)) {
                RequestStatus.PENDING -> {
                    tvStatus.text = " En attente"
                    tvStatus.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_orange_dark)
                    )
                    // Bouton visible seulement si en attente
                    btnViewOffers.isVisible = true
                    btnViewOffers.setOnClickListener { onViewOffers(request) }
                }
                RequestStatus.ACCEPTED -> {
                    tvStatus.text = " Acceptée"
                    tvStatus.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
                    )
                    //  Bouton caché si acceptée
                    btnViewOffers.isVisible = false
                }
                RequestStatus.COMPLETED -> {
                    tvStatus.text = " Terminée"
                    tvStatus.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_blue_dark)
                    )
                    //  Bouton caché si terminée
                    btnViewOffers.isVisible = false
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Request>() {
        override fun areItemsTheSame(a: Request, b: Request) = a.id == b.id
        override fun areContentsTheSame(a: Request, b: Request) = a == b
    }
}