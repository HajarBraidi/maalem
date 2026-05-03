package com.example.maalem.presentation.artisan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.R
import com.example.maalem.data.model.Request

class RequestAdapter(
    private var requests: List<Request>,
    private val onSendOfferClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvRequestTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvRequestCategory)
        val tvDescription: TextView = view.findViewById(R.id.tvRequestDescription)
        val tvCity: TextView = view.findViewById(R.id.tvRequestCity)
        val tvCitizen: TextView = view.findViewById(R.id.tvRequestCitizen)
        val btnSendOffer: Button = view.findViewById(R.id.btnSendOffer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.tvTitle.text = request.title
        holder.tvCategory.text = request.category
        holder.tvDescription.text = request.description
        holder.tvCity.text = "📍 ${request.city}"
        holder.tvCitizen.text = "Par ${request.citizenName}"
        holder.btnSendOffer.setOnClickListener { onSendOfferClick(request) }
    }

    override fun getItemCount() = requests.size

    fun updateRequests(newRequests: List<Request>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}