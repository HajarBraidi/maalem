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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArtisanRequestsFragment : Fragment() {

    private val viewModel: ArtisanHomeViewModel by viewModels()
    private lateinit var adapter: RequestAdapter

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

        adapter = RequestAdapter(emptyList()) { request ->
            val dialog = SendOfferDialog(request.id) { price, delay, message ->
                viewModel.sendOffer(
                    requestId = request.id,
                    price = price,
                    delay = delay,
                    message = message,
                    artisanName = "",  // rempli plus tard via profil
                    artisanPhone = ""
                )
            }
            dialog.show(parentFragmentManager, "SendOfferDialog")
        }

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

        viewModel.loadAvailableRequests()
    }
}