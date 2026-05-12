package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentCitizenHomeBinding
import com.example.maalem.domain.repository.CategoryRepository
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CitizenHomeFragment : Fragment(R.layout.fragment_citizen_home) {

    private var _binding: FragmentCitizenHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitizenHomeViewModel by viewModels()
    private lateinit var adapter: ArtisanAdapter

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var categoryRepository: CategoryRepository

    private var categories: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCitizenHomeBinding.bind(view)

        setupRecyclerView()
        loadCategories()
        loadUserName()
        observeState()

        // Charger tous les artisans au début
        viewModel.loadArtisans(null)
    }

    private fun setupRecyclerView() {
        adapter = ArtisanAdapter { artisan ->
            // TODO: ouvrir profil artisan
        }

        binding.rvArtisans.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArtisans.adapter = adapter
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.getCategories()

            result.onSuccess { list ->
                categories = list
                setupCategories(categories)
            }

            result.onFailure {
                Snackbar.make(
                    binding.root,
                    "Erreur lors du chargement des catégories",
                    Snackbar.LENGTH_LONG
                ).show()

                // Même si les catégories ne chargent pas, on garde le filtre Tous
                setupCategories(emptyList())
            }
        }
    }

    private fun setupCategories(categoriesFromFirebase: List<String>) {
        binding.llCategories.removeAllViews()

        // Chip Tous
        val chipAll = Chip(requireContext()).apply {
            text = "Tous"
            isCheckable = true
            isChecked = true

            setOnClickListener {
                viewModel.loadArtisans(null)
            }
        }

        binding.llCategories.addView(chipAll)

        // Catégories ajoutées par l'admin
        categoriesFromFirebase.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true

                setOnClickListener {
                    viewModel.loadArtisans(category)
                }
            }

            binding.llCategories.addView(chip)
        }
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Citoyen"
                binding.tvWelcome.text = "Bonjour $name"
            }
            .addOnFailureListener {
                binding.tvWelcome.text = "Bonjour"
            }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading

                when (state) {
                    is CitizenUiState.ArtisansLoaded -> {
                        adapter.submitList(state.artisans)
                    }

                    is CitizenUiState.Error -> {
                        Snackbar.make(
                            binding.root,
                            state.message,
                            Snackbar.LENGTH_LONG
                        ).show()
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