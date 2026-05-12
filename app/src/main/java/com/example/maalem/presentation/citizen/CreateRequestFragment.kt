package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.databinding.FragmentCreateRequestBinding
import com.example.maalem.domain.repository.CategoryRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreateRequestFragment : Fragment(R.layout.fragment_create_request) {

    private var _binding: FragmentCreateRequestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitizenHomeViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var categoryRepository: CategoryRepository

    private var categories: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateRequestBinding.bind(view)

        loadCategories()

        binding.btnSubmit.setOnClickListener {
            submitRequest()
        }

        observeState()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = categoryRepository.getCategories()

            result.onSuccess { list ->
                categories = list

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )

                binding.etCategory.setAdapter(adapter)
            }

            result.onFailure {
                Snackbar.make(
                    binding.root,
                    "Erreur lors du chargement des catégories",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                binding.btnSubmit.isEnabled = state !is CitizenUiState.Loading

                when (state) {
                    is CitizenUiState.RequestSent -> {
                        Snackbar.make(
                            binding.root,
                            "Demande envoyée avec succès !",
                            Snackbar.LENGTH_LONG
                        ).show()
                        clearForm()
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

    private fun submitRequest() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val city = binding.etCity.text.toString().trim()

        if (title.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez entrer un titre", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez entrer une description", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez choisir une catégorie", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (!categories.contains(category)) {
            Snackbar.make(binding.root, "Veuillez choisir une catégorie valide", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (city.isEmpty()) {
            Snackbar.make(binding.root, "Veuillez entrer la ville", Snackbar.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""

                viewModel.createRequest(
                    title = title,
                    description = description,
                    category = category,
                    city = city,
                    citizenName = name
                )
            }
            .addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    "Impossible de récupérer les informations utilisateur",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etCategory.text?.clear()
        binding.etCity.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}