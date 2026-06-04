package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.data.model.Artisan
import com.example.maalem.data.model.Citizen
import com.example.maalem.databinding.FragmentCitizenHomeBinding
import com.example.maalem.domain.repository.CategoryRepository
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = requireContext().packageName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCitizenHomeBinding.bind(view)

        setupMap()
        setupRecyclerView()
        loadCategories()
        observeState()

        viewModel.loadHome(null)
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(13.0)
    }

    private fun setupRecyclerView() {
        adapter = ArtisanAdapter(
            onClick = { artisan ->
                viewModel.selectArtisan(artisan)
            },
            onViewProfile = { artisan ->                   // ← nouveau
                val fragment = ViewArtisanProfileFragment.newInstance(artisan.uid)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

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
                    "Erreur lors du chargement des spécialités",
                    Snackbar.LENGTH_LONG
                ).show()

                setupCategories(emptyList())
            }
        }
    }

    private fun setupCategories(categoriesFromFirebase: List<String>) {
        binding.llCategories.removeAllViews()

        val chipAll = Chip(requireContext()).apply {
            text = "Tous"
            isCheckable = true
            isChecked = true

            setOnClickListener {
                viewModel.loadHome(null)
            }
        }

        binding.llCategories.addView(chipAll)

        categoriesFromFirebase.forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true

                setOnClickListener {
                    viewModel.loadHome(category)
                }
            }

            binding.llCategories.addView(chip)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading

                when (state) {
                    is CitizenUiState.HomeLoaded -> {
                        binding.tvWelcome.text = "Bonjour ${state.citizen.name}"

                        val nearestArtisans = state.nearestArtisans.map { it.artisan }
                        adapter.submitList(nearestArtisans)

                        showMap(
                            citizen = state.citizen,
                            nearestArtisans = state.nearestArtisans,
                            selectedArtisan = state.selectedArtisan
                        )
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

    private fun showMap(
        citizen: Citizen,
        nearestArtisans: List<NearbyArtisan>,
        selectedArtisan: Artisan?
    ) {
        val map = binding.mapView

        map.overlays.clear()

        val citizenPoint = GeoPoint(citizen.latitude, citizen.longitude)

        val allPoints = mutableListOf<GeoPoint>()
        allPoints.add(citizenPoint)

        // Marker citoyen
        val citizenMarker = Marker(map).apply {
            position = citizenPoint
            title = "Votre position"
            snippet = citizen.locationName.ifBlank { citizen.address }
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        map.overlays.add(citizenMarker)

        // Markers artisans
        nearestArtisans.forEach { nearby ->
            val artisan = nearby.artisan
            val artisanPoint = GeoPoint(artisan.latitude, artisan.longitude)
            allPoints.add(artisanPoint)

            val marker = Marker(map).apply {
                position = artisanPoint
                title = artisan.name
                snippet = "${artisan.specialty} • %.2f km".format(nearby.distanceKm)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                setOnMarkerClickListener { clickedMarker, _ ->
                    clickedMarker.showInfoWindow()

                    // Sélectionner l’artisan sans quitter la page
                    viewModel.selectArtisan(artisan)

                    Snackbar.make(
                        binding.root,
                        "${artisan.name} sélectionné",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    true
                }
            }

            if (selectedArtisan?.uid == artisan.uid) {
                marker.showInfoWindow()
            }

            map.overlays.add(marker)
        }

        // Zoom automatique pour afficher citoyen + artisans
        if (allPoints.size > 1) {
            val north = allPoints.maxOf { it.latitude }
            val south = allPoints.minOf { it.latitude }
            val east = allPoints.maxOf { it.longitude }
            val west = allPoints.minOf { it.longitude }

            val boundingBox = org.osmdroid.util.BoundingBox(
                north,
                east,
                south,
                west
            )

            map.post {
                map.zoomToBoundingBox(boundingBox, true, 80)
            }
        } else {
            map.controller.setZoom(14.0)
            map.controller.setCenter(citizenPoint)
        }

        map.invalidate()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDetach()
        _binding = null
    }
}