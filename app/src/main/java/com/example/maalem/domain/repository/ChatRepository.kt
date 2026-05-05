package com.example.maalem.domain.repository

import com.example.maalem.data.model.Conversation
import com.example.maalem.data.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getConversationId(uid1: String, uid2: String): String
    suspend fun sendMessage(conversationId: String, message: Message): Result<Unit>
    fun getMessages(conversationId: String): Flow<List<Message>>
    fun getConversations(userId: String): Flow<List<Conversation>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(conversationId: String, userId: String)
}