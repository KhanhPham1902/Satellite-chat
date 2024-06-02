package com.example.demoappchat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MessageAdapter(
    private val originalMessages: List<Message>,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: List<Message> = originalMessages


    private val VIEW_TYPE_SENT = 0
    private val VIEW_TYPE_RECEIVED = 1

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showMessage: TextView = itemView.findViewById(R.id.show_message)
        val txttime: TextView = itemView.findViewById(R.id.txt_time)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showMessage: TextView = itemView.findViewById(R.id.show_message)
        val txttime: TextView = itemView.findViewById(R.id.txt_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item_right, parent, false)
            SentMessageViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.chat_item_left, parent, false)
            ReceivedMessageViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = messages[position]

        val originalFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        val time = originalFormat.parse(currentItem.time)

        val gmtFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        gmtFormat.timeZone = TimeZone.getTimeZone("GMT")
        val formattedTime = gmtFormat.format(time)

        if (holder.itemViewType == VIEW_TYPE_SENT) {
            val sentHolder = holder as SentMessageViewHolder
            sentHolder.showMessage.text = currentItem.data
            sentHolder.txttime.text = formattedTime
        } else {
            val receivedHolder = holder as ReceivedMessageViewHolder
            receivedHolder.showMessage.text = currentItem.data
            receivedHolder.txttime.text = formattedTime
        }
    }


    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = messages[position]
        return if (currentItem.direc == 0) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }
}
