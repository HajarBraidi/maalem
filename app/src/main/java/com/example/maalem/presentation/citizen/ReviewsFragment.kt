package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentReviewsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewsFragment : Fragment(R.layout.fragment_reviews) {

    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenHomeViewModel by viewModels()
    private lateinit var adapter: ReviewAdapter

    companion object {
        fun newInstance(artisanId: String, artisanName: String): ReviewsFragment {
            return ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putString("artisanId", artisanId)
                    putString("artisanName", artisanName)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReviewsBinding.bind(view)

        val artisanId = arguments?.getString("artisanId") ?: ""
        val artisanName = arguments?.getString("artisanName") ?: ""

        binding.tvArtisanNameHeader.text = artisanName

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Setup RecyclerView
        adapter = ReviewAdapter()
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        // Charger les avis
        viewModel.loadReviews(artisanId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading

                when (state) {
                    is CitizenUiState.ReviewsLoaded -> {
                        val reviews = state.reviews

                        // État vide
                        binding.layoutEmpty.isVisible = reviews.isEmpty()
                        binding.rvReviews.isVisible = reviews.isNotEmpty()

                        adapter.submitList(reviews)

                        // Moyenne dans le header
                        if (reviews.isNotEmpty()) {
                            val avg = reviews.map { it.rating }.average().toFloat()
                            val starsOn5 = avg / 2f
                            binding.ratingBarHeader.rating = starsOn5
                            binding.tvHeaderAverage.text =
                                "%.1f/10 • %d avis".format(avg, reviews.size)
                        } else {
                            binding.ratingBarHeader.rating = 0f
                            binding.tvHeaderAverage.text = "Aucun avis"
                        }
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