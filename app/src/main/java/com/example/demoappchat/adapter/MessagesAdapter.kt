package com.example.demoappchat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demoappchat.R
import com.example.demoappchat.model.Message
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MessagesAdapter(
    originalMessages: List<Message>,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messages: List<Message> = originalMessages
    private var count1 = 0
    private var count2 = 0

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout_receive: LinearLayout = itemView.findViewById(R.id.layout_receive)
        val layout_sent: LinearLayout = itemView.findViewById(R.id.layout_sent)
        val txtMessSent: TextView = itemView.findViewById(R.id.txtMessSent)
        val txtTimeSent: TextView = itemView.findViewById(R.id.txtTimeSent)
        val txtMessReceive: TextView = itemView.findViewById(R.id.txtMessReceive)
        val txtTimeReceive: TextView = itemView.findViewById(R.id.txtTimeReceive)
        val txtDateChat: TextView = itemView.findViewById(R.id.txtDateChat)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessagesAdapter.MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessagesAdapter.MessageViewHolder, position: Int) {
        val currentItem = messages[position] // Lay tin nhan trong danh sach
        val previousItem =
            if (position > 0) messages[position - 1] else null // Lay tin nhan o vi tri truoc vi tri hien tai
        val firstItem = messages[0] // Lay tin nhan dau tien trong danh sach

        // Lay trang thai tin nhan (gui/nhan)
        val status = currentItem.direc

        val originalFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        // Lay thoi gian nhan tin vi tri hien tai
        val timeCurrent = originalFormat.parse(currentItem.time)
        // Lay thoi gian nhan tin vi tri truoc
        val timePrevious = if (previousItem != null) {
            originalFormat.parse(previousItem.time)
        } else {
            originalFormat.parse(currentItem.time)
        }
        // Lay thoi gian nhan tin dau tien trong danh sach
        val timeFirst = originalFormat.parse(firstItem.time)

        // Hien thi thoi gian nhan tin
        val timeChat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeChat.timeZone = TimeZone.getTimeZone("GMT")
        val formattedTime = timeCurrent?.let { timeChat.format(it) }

        // Hien thi ngay nhan tin
        val dateChat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        dateChat.timeZone = TimeZone.getTimeZone("GMT")
        val formattedDate = timeCurrent?.let { dateChat.format(it) }

        if (status == 0) { // Gui tin nhan
            holder.layout_receive.visibility = View.GONE
            holder.layout_sent.visibility = View.VISIBLE
            holder.txtMessSent.text = currentItem.data
            holder.txtTimeSent.text = formattedTime
        } else { // Nhan tin nhan
            holder.layout_receive.visibility = View.VISIBLE
            holder.layout_sent.visibility = View.GONE
            holder.txtMessReceive.text = currentItem.data
            holder.txtTimeReceive.text = formattedTime
        }

        // Xu ly su kien click vao tin nhan gui de hien thi ngay nhan tin
        holder.txtMessSent.setOnClickListener {
            count1++
            if (count1 % 2 == 1) {
                holder.txtDateChat.visibility = View.VISIBLE
                holder.txtDateChat.text = formattedDate
            } else {
                holder.txtDateChat.visibility = View.GONE
            }
        }

        // Xu ly su kien click vao tin nhan den de hien thi ngay nhan tin
        holder.txtMessReceive.setOnClickListener {
            count2++
            if (count2 % 2 == 1) {
                holder.txtDateChat.visibility = View.VISIBLE
                holder.txtDateChat.text = formattedDate
            } else {
                holder.txtDateChat.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }
}