package com.example.maalem.presentation.chat

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentConversationsBinding
import com.example.maalem.presentation.artisan.ArtisanHomeActivity
import com.example.maalem.presentation.citizen.CitizenHomeActivity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ConversationsFragment : Fragment(R.layout.fragment_conversations) {

    private var _binding: FragmentConversationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ConversationAdapter

    @Inject lateinit var firestore: FirebaseFirestore

    private val participantNames = mutableMapOf<String, String>()

    // Déterminer le bon container selon l'activité
    private fun getContainerId(): Int {
        return when (activity) {
            is ArtisanHomeActivity -> R.id.artisanFragmentContainer
            is CitizenHomeActivity -> R.id.fragment_container
            else -> R.id.fragment_container
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConversationsBinding.bind(view)

        adapter = ConversationAdapter(
            currentUserId = viewModel.currentUserId,
            participantNames = participantNames,
            onClick = { conversation ->
                val otherId = conversation.participantIds
                    .firstOrNull { it != viewModel.currentUserId } ?: ""
                val otherName = participantNames[otherId] ?: "Utilisateur"
                val fragment = ChatMessagesFragment.newInstance(otherId, otherName)

                // Utiliser le bon container
                parentFragmentManager.beginTransaction()
                    .replace(getContainerId(), fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvConversations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConversations.adapter = adapter

        viewModel.listenToConversations()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ChatUiState.ConversationsLoaded -> {
                        val conversations = state.conversations
                        binding.layoutEmpty.isVisible = conversations.isEmpty()
                        binding.rvConversations.isVisible = conversations.isNotEmpty()

                        conversations.forEach { conv ->
                            val otherId = conv.participantIds
                                .firstOrNull { it != viewModel.currentUserId } ?: ""
                            if (otherId.isNotEmpty() && !participantNames.containsKey(otherId)) {
                                firestore.collection("users").document(otherId).get()
                                    .addOnSuccessListener { doc ->
                                        participantNames[otherId] =
                                            doc.getString("name") ?: "Utilisateur"
                                        adapter.notifyDataSetChanged()
                                    }
                            }
                        }
                        adapter.submitList(conversations)
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