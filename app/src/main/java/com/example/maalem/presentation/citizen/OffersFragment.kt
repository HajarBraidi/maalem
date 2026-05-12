package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.data.model.Request
import com.example.maalem.databinding.FragmentOffersBinding
import com.example.maalem.presentation.chat.ChatMessagesFragment // ✅ Import ajouté
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OffersFragment : Fragment(R.layout.fragment_offers) {

    private var _binding: FragmentOffersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenHomeViewModel by viewModels()
    private lateinit var adapter: OfferAdapter
    private lateinit var currentRequest: Request

    companion object {
        fun newInstance(request: Request): OffersFragment {
            return OffersFragment().apply {
                arguments = Bundle().apply {
                    putString("requestId", request.id)
                    putString("requestTitle", request.title)
                    putString("requestStatus", request.status)
                    putString("citizenId", request.citizenId)
                    putString("citizenName", request.citizenName)
                    putString("category", request.category)
                    putString("city", request.city)
                    putString("description", request.description)
                    putLong("createdAt", request.createdAt)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOffersBinding.bind(view)

        currentRequest = Request(
            id = arguments?.getString("requestId") ?: "",
            title = arguments?.getString("requestTitle") ?: "",
            status = arguments?.getString("requestStatus") ?: "pending",
            citizenId = arguments?.getString("citizenId") ?: "",
            citizenName = arguments?.getString("citizenName") ?: "",
            category = arguments?.getString("category") ?: "",
            city = arguments?.getString("city") ?: "",
            description = arguments?.getString("description") ?: "",
            createdAt = arguments?.getLong("createdAt") ?: 0L
        )

        binding.tvRequestTitle.text = currentRequest.title
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        setupRecyclerView()
        observeState()
        viewModel.loadOffers(currentRequest.id)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOffers(currentRequest.id)
    }

    private fun setupRecyclerView() {
        adapter = OfferAdapter(
            onAccept = { offer ->
                AlertDialog.Builder(requireContext())
                    .setTitle("✅ Accepter l'offre")
                    .setMessage(
                        "Artisan : ${offer.artisanName}\n" +
                                "Prix : ${offer.price} MAD\n" +
                                "Délai : ${offer.delay}\n\n" +
                                "Confirmer ?"
                    )
                    .setPositiveButton("Oui") { _, _ ->
                        viewModel.acceptOffer(offer, currentRequest)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            },
            onReject = { offer ->
                AlertDialog.Builder(requireContext())
                    .setTitle("❌ Refuser l'offre")
                    .setMessage("Voulez-vous refuser l'offre de ${offer.artisanName} ?")
                    .setPositiveButton("Oui") { _, _ ->
                        viewModel.rejectOffer(offer.id)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            },
            onViewArtisan = { offer ->
                val fragment = ViewArtisanProfileFragment.newInstance(offer.artisanId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }, //  Virgule ajoutée ici
            onChat = { offer ->
                val fragment = ChatMessagesFragment.newInstance(
                    otherId = offer.artisanId,
                    otherName = offer.artisanName
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.rvOffers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOffers.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                when (state) {
                    is CitizenUiState.OffersLoaded -> {
                        adapter.submitList(state.offers)
                        binding.layoutEmpty.isVisible = state.offers.isEmpty()
                        binding.rvOffers.isVisible = state.offers.isNotEmpty()
                    }
                    is CitizenUiState.OfferAccepted -> {
                        Snackbar.make(
                            binding.root,
                            "✅ Offre acceptée avec succès !",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        viewModel.loadOffers(currentRequest.id)
                    }
                    is CitizenUiState.OfferRejected -> {
                        Snackbar.make(
                            binding.root,
                            "❌ Offre refusée",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        viewModel.loadOffers(currentRequest.id)
                    }
                    is CitizenUiState.Error -> Snackbar.make(
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