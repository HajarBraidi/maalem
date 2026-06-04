package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.databinding.BottomSheetLeaveReviewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LeaveReviewBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetLeaveReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenHomeViewModel by viewModels()

    companion object {
        fun newInstance(artisanId: String, artisanName: String): LeaveReviewBottomSheet {
            return LeaveReviewBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("artisanId", artisanId)
                    putString("artisanName", artisanName)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetLeaveReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val artisanId = arguments?.getString("artisanId") ?: ""
        val artisanName = arguments?.getString("artisanName") ?: ""

        binding.tvArtisanName.text = "Pour : $artisanName"

        // Slider → mise à jour étoiles + label en temps réel
        binding.sliderRating.addOnChangeListener { _, value, _ ->
            val starsOn5 = value / 2f
            binding.ratingBarPreview.rating = starsOn5
            binding.tvRatingValue.text = "${value.toInt()} / 10"
        }

        // Bouton Envoyer
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.sliderRating.value
            val comment = binding.etComment.text.toString().trim()

            if (comment.isEmpty()) {
                Snackbar.make(binding.root, "Veuillez écrire un commentaire", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            viewModel.submitReview(
                artisanId = artisanId,
                artisanName = artisanName,
                rating = rating,
                comment = comment
            )
        }

        // Bouton Annuler
        binding.btnCancel.setOnClickListener { dismiss() }

        // Observer l'état
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CitizenUiState.ReviewSubmitted -> dismiss()
                    is CitizenUiState.AlreadyReviewed -> dismiss()
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