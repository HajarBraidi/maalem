package com.example.maalem.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Conversation
import com.example.maalem.databinding.ItemConversationBinding
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val currentUserId: String,
    private val participantNames: Map<String, String> = emptyMap(),
    private val unreadCounts: Map<String, Int> = emptyMap(),
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemConversationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conv = getItem(position)
        with(holder.binding) {

            // Nom de l'autre participant
            val otherId = conv.participantIds.firstOrNull { it != currentUserId } ?: ""
            tvOtherName.text = participantNames[otherId] ?: "Utilisateur"
            tvLastMessage.text = conv.lastMessage

            // Heure
            if (conv.lastMessageTime > 0) {
                tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(conv.lastMessageTime))
            }

            // ✅ Badge messages non lus
            val unread = unreadCounts[conv.id] ?: 0
            if (unread > 0) {
                tvUnreadBadge.isVisible = true
                tvUnreadBadge.text = if (unread > 9) "9+" else unread.toString()
            } else {
                tvUnreadBadge.isVisible = false
            }

            root.setOnClickListener { onClick(conv) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(a: Conversation, b: Conversation) = a.id == b.id
        override fun areContentsTheSame(a: Conversation, b: Conversation) = a == b
    }
}