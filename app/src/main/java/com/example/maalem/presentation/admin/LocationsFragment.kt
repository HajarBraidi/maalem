package com.example.maalem.presentation.admin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentLocationsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationsFragment : Fragment(R.layout.fragment_locations) {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: LocationAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationsBinding.bind(view)

        adapter = LocationAdapter { location ->
            AlertDialog.Builder(requireContext())
                .setTitle("Supprimer")
                .setMessage("Supprimer \"${location.name}\" ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.deleteLocation(location.id)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLocations.adapter = adapter

        binding.fabAddLocation.setOnClickListener { showAddDialog() }

        viewModel.loadLocations()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLocations()
    }

    private fun showAddDialog() {
        // Champs du dialog
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        val etName = EditText(requireContext()).apply { hint = "Nom de la ville" }
        val etLat = EditText(requireContext()).apply { hint = "Latitude (ex: 33.5731)" }
        val etLng = EditText(requireContext()).apply { hint = "Longitude (ex: -7.5898)" }

        layout.addView(etName)
        layout.addView(etLat)
        layout.addView(etLng)

        AlertDialog.Builder(requireContext())
            .setTitle(" Nouvelle localisation")
            .setView(layout)
            .setPositiveButton("Ajouter") { _, _ ->
                val name = etName.text.toString().trim()
                val lat = etLat.text.toString().toDoubleOrNull() ?: 0.0
                val lng = etLng.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) {
                    viewModel.addLocation(name, lat, lng)
                } else {
                    Snackbar.make(binding.root, "Nom requis", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is AdminUiState.Loading
                when (state) {
                    is AdminUiState.LocationsLoaded -> {
                        adapter.submitList(state.locations)
                        binding.layoutEmpty.isVisible = state.locations.isEmpty()
                        binding.rvLocations.isVisible = state.locations.isNotEmpty()
                    }
                    is AdminUiState.Error -> Snackbar.make(
                        binding.root, state.message, Snackbar.LENGTH_LONG
                    ).show()
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