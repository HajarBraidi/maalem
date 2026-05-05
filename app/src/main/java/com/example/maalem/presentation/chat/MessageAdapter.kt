package com.example.maalem.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maalem.data.model.Message
import com.example.maalem.databinding.ItemMessageReceivedBinding
import com.example.maalem.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2

        object DiffCallback : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(a: Message, b: Message) = a.id == b.id
            override fun areContentsTheSame(a: Message, b: Message) = a == b
        }
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            SentVH(ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedVH(ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
        when (holder) {
            is SentVH -> {
                holder.binding.tvMessage.text = msg.content
                holder.binding.tvTime.text = time
            }
            is ReceivedVH -> {
                holder.binding.tvMessage.text = msg.content
                holder.binding.tvTime.text = time
                holder.binding.tvSenderName.text = msg.senderName
            }
        }
    }

    class SentVH(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedVH(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)
}