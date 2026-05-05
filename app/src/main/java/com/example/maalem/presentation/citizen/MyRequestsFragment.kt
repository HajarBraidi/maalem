package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentMyRequestsBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyRequestsFragment : Fragment(R.layout.fragment_my_requests) {

    private var _binding: FragmentMyRequestsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenHomeViewModel by viewModels()
    private lateinit var adapter: RequestAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyRequestsBinding.bind(view)
        setupRecyclerView()
        observeState()
        viewModel.loadMyRequests()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyRequests()
    }

    private fun setupRecyclerView() {
        adapter = RequestAdapter { request ->
            // ✅ Naviguer vers OffersFragment
            val fragment = OffersFragment.newInstance(request)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRequests.adapter = adapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                when (state) {
                    is CitizenUiState.RequestsLoaded -> {
                        adapter.submitList(state.requests)
                        binding.layoutEmpty.isVisible = state.requests.isEmpty()
                        binding.rvRequests.isVisible = state.requests.isNotEmpty()
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