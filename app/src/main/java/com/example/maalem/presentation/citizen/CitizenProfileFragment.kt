package com.example.maalem.presentation.citizen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.maalem.R
import com.example.maalem.databinding.FragmentCitizenProfileBinding
import com.example.maalem.presentation.auth.AuthActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch



@AndroidEntryPoint
class CitizenProfileFragment : Fragment(R.layout.fragment_citizen_profile) {

    private var _binding: FragmentCitizenProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenProfileViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Glide.with(this).load(it).circleCrop().into(binding.ivAvatar)
            viewModel.uploadPhoto(it, requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCitizenProfileBinding.bind(view)

        binding.ivChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            viewModel.updateProfile(
                name = binding.etName.text.toString(),
                phone = binding.etPhone.text.toString(),
                address = binding.etAddress.text.toString()
            )
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is ProfileState.Loading

                when (state) {
                    is ProfileState.Loaded -> {
                        val citizen = state.citizen
                        binding.etName.setText(citizen.name)
                        binding.etEmail.setText(citizen.email)
                        binding.etPhone.setText(citizen.phone)
                        binding.etAddress.setText(citizen.address)
                        binding.tvNameHeader.text = citizen.name
                        binding.tvEmailHeader.text = citizen.email

                        if (citizen.photoUrl.startsWith("data:image")) {
                            val base64 = citizen.photoUrl.substringAfter("base64,")
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            Glide.with(this@CitizenProfileFragment)
                                .load(bytes)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .into(binding.ivAvatar)
                        } else if (citizen.photoUrl.isNotEmpty()) {
                            Glide.with(this@CitizenProfileFragment)
                                .load(citizen.photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .into(binding.ivAvatar)
                        }
                    }

                    is ProfileState.Updated -> {
                        Snackbar.make(binding.root, "✅ Profil mis à jour !", Snackbar.LENGTH_SHORT).show()
                        viewModel.loadProfile()
                    }

                    is ProfileState.LoggedOut -> {
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    is ProfileState.Error -> {
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
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