package com.example.maalem.presentation.artisan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.R
import com.example.maalem.presentation.chat.ChatMessagesFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ArtisanRequestsFragment : Fragment() {

    private val viewModel: ArtisanHomeViewModel by viewModels()
    private lateinit var adapter: RequestAdapter

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore

    private val appliedRequestIds = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_artisan_requests, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRequests)
        val progress = view.findViewById<ProgressBar>(R.id.requestsProgress)
        val empty = view.findViewById<TextView>(R.id.tvEmptyRequests)

        adapter = RequestAdapter(
            requests = emptyList(),
            appliedRequestIds = appliedRequestIds,
            onSendOfferClick = { request ->

                // Sécurité : si déjà postulé, ne pas ouvrir le dialog
                if (appliedRequestIds.contains(request.id)) {
                    Toast.makeText(requireContext(), "Vous avez déjà postulé à cette demande", Toast.LENGTH_SHORT).show()
                    return@RequestAdapter
                }

                val dialog = SendOfferDialog(request.id) { price, delay, message ->
                    viewModel.sendOffer(
                        requestId = request.id,
                        price = price,
                        delay = delay,
                        message = message,
                        artisanName = "",
                        artisanPhone = ""
                    )
                }
                dialog.show(parentFragmentManager, "SendOfferDialog")
            },
            onChatClick = { request ->
                val fragment = ChatMessagesFragment.newInstance(
                    otherId = request.citizenId,
                    otherName = request.citizenName
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.artisanFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ArtisanUiState.Loading -> {
                        progress.visibility = View.VISIBLE
                        empty.visibility = View.GONE
                    }

                    is ArtisanUiState.RequestsLoaded -> {
                        progress.visibility = View.GONE

                        if (state.requests.isEmpty()) {
                            empty.visibility = View.VISIBLE
                        } else {
                            empty.visibility = View.GONE
                            adapter.updateRequests(state.requests)
                        }
                    }

                    is ArtisanUiState.OfferSent -> {
                        progress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Offre envoyée ✓", Toast.LENGTH_SHORT).show()

                        // Après l'envoi, on recharge les offres déjà postulées
                        loadAppliedOffers()
                        viewModel.loadAvailableRequests()
                    }

                    is ArtisanUiState.Error -> {
                        progress.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }

                    else -> Unit
                }
            }
        }

        loadAppliedOffers()
        viewModel.loadAvailableRequests()
    }

    private fun loadAppliedOffers() {
        val artisanId = auth.currentUser?.uid ?: return

        firestore.collection("offers")
            .whereEqualTo("artisanId", artisanId)
            .get()
            .addOnSuccessListener { snapshot ->
                appliedRequestIds.clear()

                snapshot.documents.forEach { doc ->
                    val requestId = doc.getString("requestId")
                    if (!requestId.isNullOrBlank()) {
                        appliedRequestIds.add(requestId)
                    }
                }

                adapter.updateAppliedRequestIds(appliedRequestIds)
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Erreur lors du chargement des offres déjà envoyées",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}