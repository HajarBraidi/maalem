package com.example.maalem.presentation.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.User
import com.example.maalem.databinding.ItemUserAccountBinding

class UserAccountAdapter(
    private val onToggle: (User, Boolean) -> Unit
) : ListAdapter<User, UserAccountAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemUserAccountBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserAccountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        with(holder.binding) {

            tvName.text = user.name
            tvEmail.text = user.email
            tvPhone.text = user.phone.ifEmpty { "Non renseigné" }

            // Badge rôle avec couleur
            when (user.role) {
                "artisan" -> {
                    tvRoleBadge.text = "🔨 Artisan"
                    tvRoleBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_orange_dark)
                    )
                    // Afficher champs spécifiques artisan
                    val artisan = user as? Artisan
                    layoutSpecialty.isVisible = true
                    layoutCity.isVisible = true
                    tvSpecialty.text = artisan?.specialty ?: ""
                    tvCity.text = artisan?.city ?: ""
                }
                "citizen" -> {
                    tvRoleBadge.text = "👤 Citoyen"
                    tvRoleBadge.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_blue_dark)
                    )
                    layoutSpecialty.isVisible = false
                    layoutCity.isVisible = false
                }
            }

            // Statut compte
            if (user.isActive) {
                tvStatus.text = "🟢 Compte actif"
                tvStatus.setTextColor(
                    ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
                )
            } else {
                tvStatus.text = "🔴 Compte désactivé"
                tvStatus.setTextColor(
                    ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                )
            }

            // Switch — éviter le déclenchement lors du bind
            switchActive.setOnCheckedChangeListener(null)
            switchActive.isChecked = user.isActive
            switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggle(user, isChecked)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(a: User, b: User) = a.uid == b.uid
        override fun areContentsTheSame(a: User, b: User) =
            a.uid == b.uid && a.isActive == b.isActive
    }
}