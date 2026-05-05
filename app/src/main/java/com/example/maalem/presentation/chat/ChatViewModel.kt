package com.example.maalem.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maalem.data.model.Conversation
import com.example.maalem.data.model.Message
import com.example.maalem.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatUiState {
    object Idle : ChatUiState()
    object Loading : ChatUiState()
    data class MessagesLoaded(val messages: List<Message>) : ChatUiState()
    data class ConversationsLoaded(val conversations: List<Conversation>) : ChatUiState()
    object MessageSent : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val state: StateFlow<ChatUiState> = _state

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    val currentUserId get() = auth.currentUser?.uid ?: ""

    private var unreadJob: Job? = null

    init {
        startListeningUnread()
    }

    // ✅ Démarrer l'écoute — appelé au init et si besoin après reconnexion
    fun startListeningUnread() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) return

        unreadJob?.cancel()
        unreadJob = viewModelScope.launch {
            chatRepository.getUnreadCount(uid)
                .catch { e -> Log.e("ChatViewModel", "unreadCount error", e) }
                .collect { count -> _unreadCount.value = count }
        }
    }

    // ✅ Arrêter proprement AVANT signOut()
    fun stopListening() {
        unreadJob?.cancel()
        unreadJob = null
        _unreadCount.value = 0
    }

    fun listenToMessages(conversationId: String) = viewModelScope.launch {
        chatRepository.getMessages(conversationId)
            .catch { e ->
                Log.e("ChatViewModel", "getMessages error", e)
                _state.value = ChatUiState.Error(e.message ?: "Erreur messages")
            }
            .collect { messages ->
                _state.value = ChatUiState.MessagesLoaded(messages)
            }
    }

    fun listenToConversations() = viewModelScope.launch {
        chatRepository.getConversations(currentUserId)
            .catch { e ->
                Log.e("ChatViewModel", "getConversations error", e)
                _state.value = ChatUiState.Error(e.message ?: "Erreur conversations")
            }
            .collect { conversations ->
                _state.value = ChatUiState.ConversationsLoaded(conversations)
            }
    }

    fun sendMessage(
        receiverId: String,
        senderName: String,
        content: String
    ) = viewModelScope.launch {
        if (content.isBlank()) return@launch
        val conversationId = chatRepository.getConversationId(currentUserId, receiverId)
        val message = Message(
            senderId = currentUserId,
            senderName = senderName,
            receiverId = receiverId,
            content = content.trim(),
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        chatRepository.sendMessage(conversationId, message).fold(
            onSuccess = { _state.value = ChatUiState.MessageSent },
            onFailure = { _state.value = ChatUiState.Error(it.message ?: "Erreur envoi") }
        )
    }

    fun getConversationId(otherUserId: String) =
        chatRepository.getConversationId(currentUserId, otherUserId)

    fun markAsRead(conversationId: String) = viewModelScope.launch {
        chatRepository.markAsRead(conversationId, currentUserId)
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}