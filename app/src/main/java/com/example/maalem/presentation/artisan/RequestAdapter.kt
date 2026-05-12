package com.example.maalem.presentation.artisan

import android.content.res.ColorStateList
import android.graphics.Color
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
    private var appliedRequestIds: Set<String> = emptySet(),
    private val onSendOfferClick: (Request) -> Unit,
    private val onChatClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvRequestTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvRequestCategory)
        val tvDescription: TextView = view.findViewById(R.id.tvRequestDescription)
        val tvCity: TextView = view.findViewById(R.id.tvRequestCity)
        val tvCitizen: TextView = view.findViewById(R.id.tvRequestCitizen)
        val btnSendOffer: Button = view.findViewById(R.id.btnSendOffer)
        val btnChatCitizen: Button = view.findViewById(R.id.btn_chat_citizen)
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

        val alreadyApplied = appliedRequestIds.contains(request.id)

        if (alreadyApplied) {
            holder.btnSendOffer.text = "Déjà postulé"
            holder.btnSendOffer.isEnabled = false
            holder.btnSendOffer.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#E5E7EB"))
            holder.btnSendOffer.setTextColor(Color.parseColor("#6B7280"))
            holder.btnSendOffer.setOnClickListener(null)
        } else {
            holder.btnSendOffer.text = "Envoyer une offre"
            holder.btnSendOffer.isEnabled = true
            holder.btnSendOffer.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#5B4FCF"))
            holder.btnSendOffer.setTextColor(Color.WHITE)
            holder.btnSendOffer.setOnClickListener {
                onSendOfferClick(request)
            }
        }

        holder.btnChatCitizen.setOnClickListener {
            onChatClick(request)
        }
    }

    override fun getItemCount() = requests.size

    fun updateRequests(newRequests: List<Request>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    fun updateAppliedRequestIds(newAppliedRequestIds: Set<String>) {
        appliedRequestIds = newAppliedRequestIds
        notifyDataSetChanged()
    }
}