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
import com.google.android.material.chip.Chip
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

    private val categories = listOf(
        "Tous", "Plombier", "Électricien", "Maçon",
        "Peintre", "Menuisier", "Carreleur"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCitizenHomeBinding.bind(view)

        setupRecyclerView()
        setupCategories()
        loadUserName()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = ArtisanAdapter { artisan ->
            // TODO: ouvrir profil artisan
        }
        binding.rvArtisans.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArtisans.adapter = adapter
    }

    private fun setupCategories() {
        categories.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setOnClickListener {
                    val filter = if (category == "Tous") null else category
                    viewModel.loadArtisans(filter)
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
                binding.tvWelcome.text = "Bonjour $name 👋"
            }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                when (state) {
                    is CitizenUiState.ArtisansLoaded -> adapter.submitList(state.artisans)
                    is CitizenUiState.Error -> { /* Snackbar */ }
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