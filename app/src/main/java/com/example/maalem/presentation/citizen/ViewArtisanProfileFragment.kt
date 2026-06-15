package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.databinding.FragmentViewArtisanProfileBinding
import com.example.maalem.presentation.chat.ChatMessagesFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewArtisanProfileFragment : Fragment(R.layout.fragment_view_artisan_profile) {

    private var _binding: FragmentViewArtisanProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenHomeViewModel by viewModels()

    // Garder l'artisan courant pour le passer au BottomSheet
    private var currentArtisanId: String = ""
    private var currentArtisanName: String = ""

    companion object {
        fun newInstance(artisanId: String): ViewArtisanProfileFragment {
            return ViewArtisanProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("artisanId", artisanId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentViewArtisanProfileBinding.bind(view)

        val artisanId = arguments?.getString("artisanId") ?: ""
        currentArtisanId = artisanId

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.loadArtisanProfile(artisanId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading

                when (state) {
                    is CitizenUiState.ArtisanLoaded -> {
                        val a = state.artisan
                        currentArtisanName = a.name

                        binding.tvName.text = a.name
                        binding.tvSpecialty.text = " ${a.specialty}"
                        binding.tvEmail.text = a.email
                        binding.tvPhone.text = a.phone.ifEmpty { "Non renseigné" }
                        binding.tvCity.text = a.city.ifEmpty { "Non renseignée" }
                        binding.tvBio.text = a.bio.ifEmpty { "Aucune bio" }

                        // ★ Afficher la note moyenne (sur 10 → convertie en 5 étoiles)
                        if (a.reviewCount > 0) {
                            val starsOn5 = (a.averageRating / 2.0).toFloat()
                            binding.ratingBarAverage.rating = starsOn5
                            binding.tvAverageRating.text =
                                "%.1f/10 • %d avis".format(a.averageRating, a.reviewCount)
                        } else {
                            binding.ratingBarAverage.rating = 0f
                            binding.tvAverageRating.text = "Aucun avis"
                        }

                        // Bouton message
                        binding.btnStartChat.setOnClickListener {
                            val fragment = ChatMessagesFragment.newInstance(
                                otherId = a.uid,
                                otherName = a.name
                            )
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }

                        // ★ Bouton Voir les avis
                        binding.btnSeeReviews.setOnClickListener {
                            val fragment = ReviewsFragment.newInstance(
                                artisanId = a.uid,
                                artisanName = a.name
                            )
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }

                        // ★ Bouton Laisser un avis
                        binding.btnLeaveReview.setOnClickListener {
                            val sheet = LeaveReviewBottomSheet.newInstance(
                                artisanId = a.uid,
                                artisanName = a.name
                            )
                            sheet.show(parentFragmentManager, "leave_review")
                        }
                    }

                    is CitizenUiState.ReviewSubmitted -> {
                        Snackbar.make(binding.root, "Avis envoyé !", Snackbar.LENGTH_SHORT).show()
                        // Recharger le profil pour mettre à jour la note
                        viewModel.loadArtisanProfile(currentArtisanId)
                    }

                    is CitizenUiState.AlreadyReviewed -> {
                        Snackbar.make(
                            binding.root,
                            "Vous avez déjà laissé un avis pour cet artisan",
                            Snackbar.LENGTH_LONG
                        ).show()
                        viewModel.loadArtisanProfile(currentArtisanId)
                    }

                    is CitizenUiState.Error -> {
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}