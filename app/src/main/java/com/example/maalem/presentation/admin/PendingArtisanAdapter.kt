package com.example.maalem.presentation.admin

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
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
            loadCinPhoto(this, artisan.cinPhotoBase64)

            btnApprove.setOnClickListener { onApprove(artisan) }
            btnReject.setOnClickListener { onReject(artisan) }
        }
    }
    private fun loadCinPhoto(binding: ItemPendingArtisanBinding, cinBase64: String) {
        with(binding) {
            if (cinBase64.isEmpty()) {
                // Pas de photo
                pbCinLoading.visibility = View.GONE
                ivCin.visibility = View.GONE
                tvNoCin.visibility = View.VISIBLE
                return
            }
            Thread {
                try {
                    val pureBase64 = cinBase64
                        .removePrefix("data:image/jpeg;base64,")
                        .replace("\n", "").trim()
                    val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    // Retourner sur le thread principal pour l'affichage
                    binding.ivCin.post {
                        binding.pbCinLoading.visibility = View.GONE
                        if (bitmap != null) {
                            binding.ivCin.setImageBitmap(bitmap)
                            binding.ivCin.visibility = View.VISIBLE
                        } else {
                            binding.tvNoCin.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    binding.ivCin.post {
                        binding.pbCinLoading.visibility = View.GONE
                        binding.tvNoCin.visibility = View.VISIBLE
                    }
                }
            }.start()

            // Afficher le loading
            pbCinLoading.visibility = View.VISIBLE
            ivCin.visibility = View.GONE
            tvNoCin.visibility = View.GONE

            try {
                // Supprimer le préfixe "data:image/jpeg;base64,"
                val pureBase64 = cinBase64
                    .removePrefix("data:image/jpeg;base64,")
                    .replace("\n", "")  // ← important : Base64.DEFAULT ajoute des \n
                    .trim()

                val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                pbCinLoading.visibility = View.GONE

                if (bitmap != null) {
                    ivCin.setImageBitmap(bitmap)
                    ivCin.visibility = View.VISIBLE
                    tvNoCin.visibility = View.GONE
                } else {
                    ivCin.visibility = View.GONE
                    tvNoCin.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                pbCinLoading.visibility = View.GONE
                ivCin.visibility = View.GONE
                tvNoCin.visibility = View.VISIBLE
            }
        }
    }
    companion object DiffCallback : DiffUtil.ItemCallback<Artisan>() {
        override fun areItemsTheSame(a: Artisan, b: Artisan) = a.uid == b.uid
        override fun areContentsTheSame(a: Artisan, b: Artisan) = a == b
    }
}