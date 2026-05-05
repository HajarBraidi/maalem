package com.example.maalem.presentation.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maalem.R
import com.example.maalem.databinding.FragmentChatMessagesBinding
import com.example.maalem.presentation.artisan.ArtisanHomeActivity
import com.example.maalem.presentation.citizen.CitizenHomeActivity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatMessagesFragment : Fragment(R.layout.fragment_chat_messages) {

    private var _binding: FragmentChatMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    @Inject lateinit var firestore: FirebaseFirestore

    private var otherId = ""
    private var otherName = ""
    private var myName = ""

    companion object {
        fun newInstance(otherId: String, otherName: String): ChatMessagesFragment {
            return ChatMessagesFragment().apply {
                arguments = Bundle().apply {
                    putString("otherId", otherId)
                    putString("otherName", otherName)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatMessagesBinding.bind(view)

        otherId = arguments?.getString("otherId") ?: ""
        otherName = arguments?.getString("otherName") ?: ""

        binding.tvOtherName.text = otherName
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Adapter
        adapter = MessageAdapter(viewModel.currentUserId)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Messages depuis le bas
        }
        binding.rvMessages.adapter = adapter

        // Charger mon nom
        firestore.collection("users").document(viewModel.currentUserId).get()
            .addOnSuccessListener { doc ->
                myName = doc.getString("name") ?: ""
                // Démarrer écoute messages
                val conversationId = viewModel.getConversationId(otherId)
                viewModel.listenToMessages(conversationId)
                viewModel.markAsRead(conversationId)
            }


        // Envoyer message
        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString()
            if (content.isNotBlank()) {
                viewModel.sendMessage(
                    receiverId = otherId,
                    senderName = myName,
                    content = content
                )
                binding.etMessage.text?.clear()
            }
        }

        // Observer les messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ChatUiState.MessagesLoaded -> {
                        adapter.submitList(state.messages)
                        // Scroll vers le dernier message
                        if (state.messages.isNotEmpty()) {
                            binding.rvMessages.scrollToPosition(state.messages.size - 1)
                        }
                        // Marquer comme lu
                        val conversationId = viewModel.getConversationId(otherId)
                        viewModel.markAsRead(conversationId)
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

    //  Déterminer le bon container
    private fun getContainerId(): Int {
        return when (activity) {
            is ArtisanHomeActivity -> R.id.artisanFragmentContainer
            is CitizenHomeActivity -> R.id.fragment_container
            else -> R.id.fragment_container
        }
    }
}