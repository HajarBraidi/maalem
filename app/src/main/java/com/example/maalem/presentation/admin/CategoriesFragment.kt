package com.example.maalem.presentation.admin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentCategoriesBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoriesBinding.bind(view)

        setupRecyclerView()
        setupFab()
        observeState()

        viewModel.loadCategories()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCategories()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter { category ->
            // Dialog confirmation suppression
            AlertDialog.Builder(requireContext())
                .setTitle("Supprimer la catégorie")
                .setMessage("Voulez-vous supprimer \"$category\" ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.deleteCategory(category)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // Dialog pour ajouter une catégorie
            val input = EditText(requireContext()).apply {
                hint = "Ex: Plombier, Électricien..."
                setPadding(48, 32, 48, 32)
            }
            AlertDialog.Builder(requireContext())
                .setTitle("➕ Nouvelle catégorie")
                .setView(input)
                .setPositiveButton("Ajouter") { _, _ ->
                    val category = input.text.toString().trim()
                    if (category.isNotEmpty()) {
                        viewModel.addCategory(category)
                    } else {
                        Snackbar.make(
                            binding.root,
                            "⚠️ Veuillez entrer un nom",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is AdminUiState.Loading

                when (state) {
                    is AdminUiState.CategoriesLoaded -> {
                        adapter.submitList(state.categories)
                        binding.layoutEmpty.isVisible = state.categories.isEmpty()
                        binding.rvCategories.isVisible = state.categories.isNotEmpty()
                    }
                    is AdminUiState.ActionSuccess -> {
                        Snackbar.make(
                            binding.root,
                            "✅ Catégorie mise à jour !",
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
}