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

    private val categories = listOf(
        "Plombier", "Électricien", "Maçon",
        "Peintre", "Menuisier", "Carreleur"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateRequestBinding.bind(view)

        // Setup dropdown catégories
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.etCategory.setAdapter(adapter)

        binding.btnSubmit.setOnClickListener { submitRequest() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                binding.btnSubmit.isEnabled = state !is CitizenUiState.Loading
                when (state) {
                    is CitizenUiState.RequestSent -> {
                        Snackbar.make(
                            binding.root,
                            "✅ Demande envoyée avec succès !",
                            Snackbar.LENGTH_LONG
                        ).show()
                        clearForm()
                    }
                    is CitizenUiState.Error -> Snackbar.make(
                        binding.root, state.message, Snackbar.LENGTH_LONG
                    ).show()
                    else -> {}
                }
            }
        }
    }

    private fun submitRequest() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                viewModel.createRequest(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    category = binding.etCategory.text.toString(),
                    city = binding.etCity.text.toString(),
                    citizenName = name
                )
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