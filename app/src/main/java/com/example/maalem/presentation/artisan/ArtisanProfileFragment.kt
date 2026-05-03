package com.example.maalem.presentation.artisan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.domain.repository.ArtisanRepository
import com.example.maalem.domain.repository.AuthRepository
import com.example.maalem.domain.usecase.UpdateArtisanProfileUseCase
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArtisanProfileFragment : Fragment() {

    @Inject lateinit var artisanRepository: ArtisanRepository
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var updateUseCase: UpdateArtisanProfileUseCase
    @Inject lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_artisan_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvEmail = view.findViewById<TextView>(R.id.tvArtisanEmail)
        val tvStatus = view.findViewById<TextView>(R.id.tvValidationStatus)
        val etName = view.findViewById<TextInputEditText>(R.id.etArtisanName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etArtisanPhone)
        val etSpecialty = view.findViewById<TextInputEditText>(R.id.etArtisanSpecialty)
        val etCity = view.findViewById<TextInputEditText>(R.id.etArtisanCity)
        val etBio = view.findViewById<TextInputEditText>(R.id.etArtisanBio)
        val btnSave = view.findViewById<Button>(R.id.btnSaveArtisanProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnArtisanLogout)

        // Charger le profil
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            artisanRepository.getArtisanProfile(uid).fold(
                onSuccess = { artisan ->
                    tvEmail.text = artisan.email
                    etName.setText(artisan.name)
                    etPhone.setText(artisan.phone)
                    etSpecialty.setText(artisan.specialty)
                    etCity.setText(artisan.city)
                    etBio.setText(artisan.bio)
                    tvStatus.text = if (artisan.isValidated) "✓ Compte validé"
                    else "⏳ En attente de validation admin"
                    tvStatus.setTextColor(
                        if (artisan.isValidated) android.graphics.Color.parseColor("#2E7D32")
                        else android.graphics.Color.parseColor("#F57C00")
                    )
                },
                onFailure = {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }
            )
        }

        // Bouton enregistrer
        btnSave.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                updateUseCase(
                    artisanId = uid,
                    name = etName.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    specialty = etSpecialty.text.toString().trim(),
                    city = etCity.text.toString().trim(),
                    bio = etBio.text.toString().trim()
                ).fold(
                    onSuccess = { Toast.makeText(requireContext(), "Profil mis à jour ✓", Toast.LENGTH_SHORT).show() },
                    onFailure = { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
                )
            }
        }

        // Bouton déconnexion
        btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                authRepository.logout()
                val intent = android.content.Intent(requireContext(),
                    com.example.maalem.presentation.auth.AuthActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}