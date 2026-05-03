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
class ArtisanOffersFragment : Fragment() {

    private val viewModel: ArtisanHomeViewModel by viewModels()
    private lateinit var adapter: OfferAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_artisan_offers, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvOffers)
        val progress = view.findViewById<ProgressBar>(R.id.offersProgress)
        val empty = view.findViewById<TextView>(R.id.tvEmptyOffers)

        adapter = OfferAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ArtisanUiState.Loading -> {
                        progress.visibility = View.VISIBLE
                        empty.visibility = View.GONE
                    }
                    is ArtisanUiState.OffersLoaded -> {
                        progress.visibility = View.GONE
                        if (state.offers.isEmpty()) {
                            empty.visibility = View.VISIBLE
                        } else {
                            empty.visibility = View.GONE
                            adapter.updateOffers(state.offers)
                        }
                    }
                    is ArtisanUiState.Error -> {
                        progress.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        viewModel.loadMyOffers()
    }
}