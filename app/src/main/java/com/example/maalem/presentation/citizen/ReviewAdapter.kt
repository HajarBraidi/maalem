package com.example.maalem.presentation.citizen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Review
import com.example.maalem.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = getItem(position)
        with(holder.binding) {

            // Initiale dans l'avatar
            tvAvatar.text = review.citizenName
                .firstOrNull()?.uppercaseChar()?.toString() ?: "?"

            // Nom citoyen
            tvCitizenName.text = review.citizenName

            // Date formatée
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
            tvDate.text = sdf.format(Date(review.createdAt))

            // Badge note /10
            tvRatingBadge.text = "★ ${review.rating.toInt()}/10"

            // Étoiles (sur 10 → sur 5)
            ratingBarReview.rating = review.rating / 2f

            // Commentaire
            tvComment.text = review.comment
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(a: Review, b: Review) = a.id == b.id
        override fun areContentsTheSame(a: Review, b: Review) = a == b
    }
}