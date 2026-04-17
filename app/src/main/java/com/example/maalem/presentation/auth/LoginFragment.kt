package com.example.maalem.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.data.model.UserRole
import com.example.maalem.databinding.FragmentLoginBinding
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
        // TODO: navigation selon le rôle
        // CITIZEN  → CitizenHomeFragment
        // ARTISAN  → ArtisanHomeFragment
        // ADMIN    → AdminHomeFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}