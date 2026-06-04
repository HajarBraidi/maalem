package com.example.maalem.presentation.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentPendingArtisansBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PendingArtisansFragment : Fragment(R.layout.fragment_pending_artisans) {

    private var _binding: FragmentPendingArtisansBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: PendingArtisanAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPendingArtisansBinding.bind(view)

        setupRecyclerView()
        observeState()

        // Charger les artisans en attente
        viewModel.loadPendingArtisans()
    }

    private fun setupRecyclerView() {
        adapter = PendingArtisanAdapter(
            onApprove = { artisan ->
                // Confirmation avant validation
                AlertDialog.Builder(requireContext())
                    .setTitle("Valider l'artisan")
                    .setMessage("Voulez-vous valider ${artisan.name} ?")
                    .setPositiveButton("Oui") { _, _ ->
                        viewModel.validateArtisan(artisan.uid, true)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            },
            onReject = { artisan ->
                // Confirmation avant refus
                AlertDialog.Builder(requireContext())
                    .setTitle("Refuser l'artisan")
                    .setMessage("Voulez-vous refuser ${artisan.name} ?")
                    .setPositiveButton("Oui") { _, _ ->
                        viewModel.validateArtisan(artisan.uid, false)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }
        )

        binding.rvPending.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPending.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Afficher/cacher le loading
                binding.progressBar.isVisible = state is AdminUiState.Loading

                when (state) {
                    is AdminUiState.PendingArtisansLoaded -> {
                        adapter.submitList(state.artisans)
                        // Afficher message si liste vide
                        binding.layoutEmpty.isVisible = state.artisans.isEmpty()
                        binding.rvPending.isVisible = state.artisans.isNotEmpty()
                    }
                    is AdminUiState.ActionSuccess -> {
                        Snackbar.make(
                            binding.root,
                            " Action effectuée avec succès !",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is AdminUiState.Error -> {
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
    override fun onResume() {
        super.onResume()
        viewModel.loadPendingArtisans()
    }
}