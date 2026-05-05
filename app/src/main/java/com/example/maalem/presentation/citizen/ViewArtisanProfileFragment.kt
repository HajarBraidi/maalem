package com.example.maalem.presentation.citizen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.maalem.R
import com.example.maalem.databinding.FragmentViewArtisanProfileBinding
import com.example.maalem.presentation.chat.ChatMessagesFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewArtisanProfileFragment : Fragment(R.layout.fragment_view_artisan_profile) {

    private var _binding: FragmentViewArtisanProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitizenHomeViewModel by viewModels()

    companion object {
        fun newInstance(artisanId: String): ViewArtisanProfileFragment {
            return ViewArtisanProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("artisanId", artisanId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentViewArtisanProfileBinding.bind(view)

        val artisanId = arguments?.getString("artisanId") ?: ""

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.loadArtisanProfile(artisanId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.progressBar.isVisible = state is CitizenUiState.Loading
                when (state) {
                    is CitizenUiState.ArtisanLoaded -> {
                        val a = state.artisan
                        binding.tvName.text = a.name
                        binding.tvSpecialty.text = "🔨 ${a.specialty}"
                        binding.tvEmail.text = a.email
                        binding.tvPhone.text = a.phone.ifEmpty { "Non renseigné" }
                        binding.tvCity.text = a.city.ifEmpty { "Non renseignée" }
                        binding.tvBio.text = a.bio.ifEmpty { "Aucune bio" }

                        // Bouton démarrer conversation
                        binding.btnStartChat.setOnClickListener {
                            val fragment = ChatMessagesFragment.newInstance(
                                otherId = a.uid,
                                otherName = a.name
                            )
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
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