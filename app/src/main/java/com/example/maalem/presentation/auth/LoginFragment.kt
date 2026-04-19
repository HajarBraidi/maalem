package com.example.maalem.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.data.model.UserRole
import com.example.maalem.databinding.FragmentLoginBinding
import com.example.maalem.presentation.citizen.CitizenHomeActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is LoginState.Loading
                binding.btnLogin.isEnabled = state !is LoginState.Loading

                when (state) {
                    is LoginState.Success -> navigateByRole(state.role)
                    is LoginState.Error -> Snackbar.make(
                        binding.root, state.message, Snackbar.LENGTH_LONG
                    ).show()
                    else -> {}
                }
            }
        }
    }

    private fun navigateByRole(role: UserRole) {
        when (role) {
            UserRole.CITIZEN -> {
                val intent = Intent(requireContext(), CitizenHomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            UserRole.ARTISAN -> {
                // Pas encore développé
                Snackbar.make(
                    binding.root,
                    "⏳ Espace artisan en cours de développement",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            UserRole.ADMIN -> {
                // Pas encore développé
                Snackbar.make(
                    binding.root,
                    "⏳ Espace admin en cours de développement",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}