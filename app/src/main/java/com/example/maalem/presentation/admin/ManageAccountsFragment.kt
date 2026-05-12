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
import com.example.maalem.databinding.FragmentManageAccountsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageAccountsFragment : Fragment(R.layout.fragment_manage_accounts) {

    private var _binding: FragmentManageAccountsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: UserAccountAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageAccountsBinding.bind(view)

        setupRecyclerView()
        setupTabs()
        observeState()

        // Charger les artisans par défaut
        viewModel.loadAllArtisans()
    }

    private fun setupRecyclerView() {
        adapter = UserAccountAdapter { user, isActive ->
            // Dialog de confirmation
            val action = if (isActive) "activer" else "désactiver"
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmer")
                .setMessage("Voulez-vous $action le compte de ${user.name} ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.toggleUser(user.uid, isActive)
                }
                .setNegativeButton("Annuler") { _, _ ->
                    // Recharger la liste pour reset le switch
                    if (binding.tabLayout.selectedTabPosition == 0)
                        viewModel.loadAllArtisans()
                    else
                        viewModel.loadAllCitizens()
                }
                .show()
        }

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    private fun setupTabs() {
        // Ajouter les onglets
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(" Artisans")
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(" Citoyens")
        )

        // Listener sur changement d'onglet
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> viewModel.loadAllArtisans()
                    1 -> viewModel.loadAllCitizens()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is AdminUiState.Loading

                when (state) {
                    is AdminUiState.ArtisansLoaded -> {
                        adapter.submitList(state.artisans)
                        binding.layoutEmpty.isVisible = state.artisans.isEmpty()
                        binding.rvUsers.isVisible = state.artisans.isNotEmpty()
                    }
                    is AdminUiState.CitizensLoaded -> {
                        adapter.submitList(state.citizens)
                        binding.layoutEmpty.isVisible = state.citizens.isEmpty()
                        binding.rvUsers.isVisible = state.citizens.isNotEmpty()
                    }
                    is AdminUiState.ActionSuccess -> {
                        Snackbar.make(
                            binding.root,
                            " Compte mis à jour !",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        // Recharger la liste active
                        if (binding.tabLayout.selectedTabPosition == 0)
                            viewModel.loadAllArtisans()
                        else
                            viewModel.loadAllCitizens()
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
    //  Recharger à chaque fois qu'on revient sur ce fragment
    override fun onResume() {
        super.onResume()
        if (binding.tabLayout.selectedTabPosition == 0)
            viewModel.loadAllArtisans()
        else
            viewModel.loadAllCitizens()
    }
}