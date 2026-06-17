package com.example.maalem.presentation.artisan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.R
import com.example.maalem.data.model.AppLocation
import com.example.maalem.domain.repository.ArtisanRepository
import com.example.maalem.domain.repository.AuthRepository
import com.example.maalem.domain.repository.LocationRepository
import com.example.maalem.domain.usecase.UpdateArtisanProfileUseCase
import com.example.maalem.presentation.citizen.ReviewAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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
    @Inject lateinit var locationRepository: LocationRepository

    // ★ NOUVEAU
    private val viewModel: ArtisanHomeViewModel by viewModels()
    private lateinit var reviewAdapter: ReviewAdapter

    // Liste des villes (chargée depuis config/locations)
    private var cities: List<AppLocation> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_artisan_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vues existantes
        val tvEmail = view.findViewById<TextView>(R.id.tvArtisanEmail)
        val tvStatus = view.findViewById<TextView>(R.id.tvValidationStatus)
        val etName = view.findViewById<TextInputEditText>(R.id.etArtisanName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etArtisanPhone)
        val etSpecialty = view.findViewById<TextInputEditText>(R.id.etArtisanSpecialty)
        val etCity = view.findViewById<MaterialAutoCompleteTextView>(R.id.etArtisanCity)
        val etBio = view.findViewById<TextInputEditText>(R.id.etArtisanBio)
        val btnSave = view.findViewById<Button>(R.id.btnSaveArtisanProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnArtisanLogout)

        // ★ NOUVEAU — vues avis
        val ratingBarAverage = view.findViewById<RatingBar>(R.id.rating_bar_my_average)
        val tvAverageRating = view.findViewById<TextView>(R.id.tv_my_average_rating)
        val tvReviewCountBadge = view.findViewById<TextView>(R.id.tv_review_count_badge)
        val rvMyReviews = view.findViewById<RecyclerView>(R.id.rv_my_reviews)
        val tvNoReviews = view.findViewById<TextView>(R.id.tv_no_reviews)

        // ★ Setup RecyclerView avis
        reviewAdapter = ReviewAdapter()
        rvMyReviews.layoutManager = LinearLayoutManager(requireContext())
        rvMyReviews.adapter = reviewAdapter

        // Charger les villes puis le profil (l'adapter du dropdown doit être prêt
        // avant de positionner la ville actuelle de l'artisan)
        viewLifecycleOwner.lifecycleScope.launch {
            // 1) Charger les villes pour le dropdown
            locationRepository.getCities().onSuccess { list ->
                cities = list
                etCity.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        list.map { it.name }
                    )
                )
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    "Erreur lors du chargement des villes",
                    Toast.LENGTH_LONG
                ).show()
            }

            // 2) Charger le profil (code existant)
            val uid = auth.currentUser?.uid ?: return@launch
            artisanRepository.getArtisanProfile(uid).fold(
                onSuccess = { artisan ->
                    tvEmail.text = artisan.email
                    etName.setText(artisan.name)
                    etPhone.setText(artisan.phone)
                    etSpecialty.setText(artisan.specialty)
                    // setText(..., false) : affiche la valeur sans filtrer la liste du dropdown
                    etCity.setText(artisan.city, false)
                    etBio.setText(artisan.bio)
                    tvStatus.text = if (artisan.isValidated) "✓ Compte validé"
                    else "⏳ En attente de validation admin"
                    tvStatus.setTextColor(
                        if (artisan.isValidated)
                            android.graphics.Color.parseColor("#2E7D32")
                        else
                            android.graphics.Color.parseColor("#F57C00")
                    )
                },
                onFailure = {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }
            )
        }

        // ★ Observer l'état pour les avis
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ArtisanUiState.ReviewsLoaded -> {
                        val reviews = state.reviews

                        if (reviews.isEmpty()) {
                            tvNoReviews.isVisible = true
                            rvMyReviews.isVisible = false
                            tvReviewCountBadge.isVisible = false
                            ratingBarAverage.rating = 0f
                            tvAverageRating.text = "Aucun avis pour le moment"
                        } else {
                            tvNoReviews.isVisible = false
                            rvMyReviews.isVisible = true
                            tvReviewCountBadge.isVisible = true

                            // Étoiles moyenne (sur 10 → sur 5)
                            ratingBarAverage.rating = state.averageRating / 2f
                            tvAverageRating.text =
                                "%.1f / 10 en moyenne".format(state.averageRating)
                            tvReviewCountBadge.text = "${state.reviewCount} avis"

                            reviewAdapter.submitList(reviews)
                        }
                    }

                    is ArtisanUiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
        }

        // ★ Charger les avis
        viewModel.loadMyReviews()

        // Bouton enregistrer (code existant)
        btnSave.setOnClickListener {
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            val selectedCity = etCity.text.toString().trim()

            // Vérifier que la ville fait bien partie de la liste de l'admin
            if (cities.none { it.name.equals(selectedCity, ignoreCase = true) }) {
                Toast.makeText(
                    requireContext(),
                    "Veuillez choisir une ville valide dans la liste",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                updateUseCase(
                    artisanId = uid,
                    name = etName.text.toString().trim(),
                    phone = etPhone.text.toString().trim(),
                    specialty = etSpecialty.text.toString().trim(),
                    city = selectedCity,
                    bio = etBio.text.toString().trim()
                ).fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Profil mis à jour ✓", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onFailure = {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }

        // Bouton déconnexion (code existant)
        btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                authRepository.logout()
                val intent = android.content.Intent(
                    requireContext(),
                    com.example.maalem.presentation.auth.AuthActivity::class.java
                )
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}