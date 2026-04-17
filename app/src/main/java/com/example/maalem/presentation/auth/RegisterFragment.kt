package com.example.maalem.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.data.model.UserRole
import com.example.maalem.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()
    private var selectedRole = UserRole.CITIZEN

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        // Changement de rôle → affiche/cache les champs
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_citizen -> {
                    selectedRole = UserRole.CITIZEN
                    binding.layoutCitizen.isVisible = true
                    binding.layoutArtisan.isVisible = false
                }
                R.id.rb_artisan -> {
                    selectedRole = UserRole.ARTISAN
                    binding.layoutCitizen.isVisible = false
                    binding.layoutArtisan.isVisible = true
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                name = binding.etName.text.toString(),
                email = binding.etEmail.text.toString(),
                phone = binding.etPhone.text.toString(),
                password = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString(),
                role = selectedRole,
                specialty = binding.etSpecialty.text.toString(),
                city = binding.etCity.text.toString(),
                bio = binding.etBio.text.toString(),
                address = binding.etAddress.text.toString()
            )
        }

        binding.tvLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is RegisterState.Loading
                binding.btnRegister.isEnabled = state !is RegisterState.Loading

                when (state) {
                    is RegisterState.Success -> {
                        Snackbar.make(
                            binding.root,
                            if (selectedRole == UserRole.ARTISAN)
                                "✅ Compte créé ! En attente de validation."
                            else
                                "✅ Compte créé ! Vous pouvez vous connecter.",
                            Snackbar.LENGTH_LONG
                        ).show()
                        parentFragmentManager.popBackStack()
                    }
                    is RegisterState.Error -> Snackbar.make(
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