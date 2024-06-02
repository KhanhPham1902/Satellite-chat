package com.example.demoappchat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DeviceAdapter(
    var deviceList: List<String>,
    var messagesByDevice: Map<String, List<Message>>
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val deviceName = deviceList[position]
        val messages = messagesByDevice[deviceName] ?: emptyList()
        holder.bind(deviceName, messages)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    fun setFilteredList(filteredList: List<String>) {
        this.deviceList = filteredList
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceNameTextView: TextView = itemView.findViewById(R.id.deviceName)
        private val txtLastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        private val txtLastTime: TextView = itemView.findViewById(R.id.txtLastTime)
        private val txtSentOrReceived: TextView = itemView.findViewById(R.id.txtSentOrReceived)
        init {
            // Thêm lắng nghe sự kiện khi itemView được nhấn
            itemView.setOnClickListener {
                // Lấy vị trí của item được nhấn
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Lấy tên thiết bị và tin nhắn tương ứng
                    val deviceName = deviceList[position]
                    val deviceMessages = messagesByDevice[deviceName] ?: emptyList()

                    // Tạo Intent để chuyển sang MessageActivity và truyền dữ liệu cần thiết
                    val intent = Intent(itemView.context, MessageActivity::class.java)
                    intent.putExtra("devicename", deviceName)
                    intent.putExtra("deviceMessages", ArrayList(deviceMessages))
                    // Chuyển sang MessageActivity
                    itemView.context.startActivity(intent)
                }
            }
        }

        fun bind(deviceName: String, messages: List<Message>) {
            // Lay ten thiet bi
            deviceNameTextView.text = deviceName

            // Lay tin nhan cuoi cung
            val lastMessage = messages.lastOrNull()?.data
            txtLastMessage.text = lastMessage

            // Lay thoi gian nhan/ gui tin nhan cuoi cung
            val lastTime = messages.lastOrNull()?.time
            val sdfInput = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val timeBefore = sdfInput.parse(lastTime)
            if(timeBefore != null){
                val sdfOutput = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdfOutput.timeZone = TimeZone.getTimeZone("GMT")
                val timeFormated = sdfOutput.format(timeBefore)
                txtLastTime.text = timeFormated
            }

            // Hien thi trang thai tin nhan gui/ nhan
            val isSent = messages.lastOrNull()?.direc
            if(isSent==0){
                txtSentOrReceived.visibility = View.VISIBLE
            }else{
                txtSentOrReceived.visibility = View.GONE
            }
        }
    }
}
