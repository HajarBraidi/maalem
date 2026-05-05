package com.example.maalem.data.repository

import android.util.Log
import com.example.maalem.data.model.Conversation
import com.example.maalem.data.model.Message
import com.example.maalem.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun getConversationId(uid1: String, uid2: String): String =
        listOf(uid1, uid2).sorted().joinToString("_")

    override suspend fun sendMessage(
        conversationId: String,
        message: Message
    ): Result<Unit> {
        return try {
            val convRef = firestore.collection("conversations").document(conversationId)
            val msgRef = convRef.collection("messages").document()
            val msgWithId = message.copy(id = msgRef.id)
            msgRef.set(msgWithId).await()
            convRef.set(mapOf(
                "id" to conversationId,
                "participantIds" to listOf(message.senderId, message.receiverId),
                "lastMessage" to message.content,
                "lastMessageTime" to message.timestamp
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> =
        callbackFlow {
            val listener = firestore
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "getMessages error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val messages = snapshot?.documents?.map { doc ->
                        Message(
                            id = doc.getString("id") ?: doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            senderName = doc.getString("senderName") ?: "",
                            receiverId = doc.getString("receiverId") ?: "",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    } ?: emptyList()
                    trySend(messages)
                }
            awaitClose { listener.remove() }
        }.catch { e ->
            Log.e("ChatRepository", "getMessages flow error", e)
            emit(emptyList())
        }

    override fun getConversations(userId: String): Flow<List<Conversation>> =
        callbackFlow {
            if (userId.isEmpty()) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }
            val listener = firestore
                .collection("conversations")
                .whereArrayContains("participantIds", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "getConversations error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val conversations = snapshot?.documents?.map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        Conversation(
                            id = doc.getString("id") ?: doc.id,
                            participantIds = doc.get("participantIds") as? List<String> ?: emptyList(),
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTime = doc.getLong("lastMessageTime") ?: 0L
                        )
                    } ?: emptyList()
                    trySend(conversations.sortedByDescending { it.lastMessageTime })
                }
            awaitClose { listener.remove() }
        }.catch { e ->
            Log.e("ChatRepository", "getConversations flow error", e)
            emit(emptyList())
        }

    // ✅ FIX PRINCIPAL : ne pas close(error) → trySend(0) pour éviter le crash
    override fun getUnreadCount(userId: String): Flow<Int> =
        callbackFlow {
            if (userId.isEmpty()) {
                trySend(0)
                close()
                return@callbackFlow
            }
            val listener = firestore
                .collectionGroup("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatRepository", "getUnreadCount error", error)
                        trySend(0)  // ← NE PAS close(error), juste envoyer 0
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents?.size ?: 0)
                }
            awaitClose { listener.remove() }
        }.catch { e ->
            Log.e("ChatRepository", "getUnreadCount flow error", e)
            emit(0)
        }

    override suspend fun markAsRead(conversationId: String, userId: String) {
        try {
            val unread = firestore
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("isRead", false)
                .get().await()

            val batch = firestore.batch()
            unread.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("ChatRepository", "markAsRead error", e)
        }
    }
}