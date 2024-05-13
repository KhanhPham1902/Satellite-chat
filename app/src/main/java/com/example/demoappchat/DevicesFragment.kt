package com.example.demoappchat

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DevicesFragment : Fragment() {
    private lateinit var root : View
    private val messagesByDevice: MutableMap<String, List<Message>> = mutableMapOf()
    private lateinit var username : String
    private lateinit var password : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_devices, container, false)
        initViews()
        getListDevice()

        return root
    }

    private fun getListDevice() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val currentTime = LocalDateTime.now()
        val startTime = currentTime.minus(3, ChronoUnit.DAYS)
        val startTimeString = startTime.format(formatter)
        val endTimeString = currentTime.format(formatter)
        val timeRange = TimeRange(startTimeString, endTimeString)

        // Gửi yêu cầu đọc tin nhắn thông qua API Retrofit
        RetrofitClient.instance.getUserMessages(username, password, timeRange)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let { body ->
                            try {
                                val jsonString = body.string()
                                val messages = parseJsonToMessages(jsonString)
                                Log.d("MESS",messages.toString())
                                // Tạo một HashSet để lưu trữ các thiết bị duy nhất
                                val uniqueDevices = HashSet<String>()

                                // Duyệt qua danh sách tin nhắn và trích xuất các thiết bị duy nhất
                                for (message in messages) {
                                    uniqueDevices.add(message.device)
                                }

                                // Cập nhật RecyclerView với danh sách các thiết bị duy nhất
                                updateDeviceList(uniqueDevices.toList())

                                Log.d("TAG", "Lấy danh sách thiết bị thành công")
                            } catch (e: JSONException) {
                                Log.e("TAG", "Lỗi khi phân tích JSON", e)
                            }
                        }
                    } else {
                        // Xử lý lỗi khi đọc tin nhắn không thành công
                        Log.d("TAG", "Lấy danh sách thiết bị không thành công")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // Xử lý lỗi khi gọi API không thành công
                    Log.e("TAG", "Không thể gọi API", t)
                }
            })
    }

    private fun initViews() {
        // Khởi tạo RecyclerView và các thiết lập khác
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Truy cập SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("login_info", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()
        password = sharedPreferences.getString("password", "").toString()
    }

    private fun updateDeviceList(devices: List<String>) {
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        val deviceAdapter = DeviceAdapter(devices,messagesByDevice)
        recyclerView.adapter = deviceAdapter


    }

    private fun parseJsonToMessages(jsonString: String): List<Message> {
        val jsonArray = JSONArray(jsonString)
        val messages = mutableListOf<Message>()
        for (i in 0 until jsonArray.length()) {
            val messageArray = jsonArray.getJSONArray(i)
            val id = messageArray.getInt(0)
            val data = messageArray.getString(1)
            val user_id = messageArray.getInt(2)
            val deviceName = messageArray.getString(3) // Đổi tên biến này thành deviceName
            val time = messageArray.getString(4)
            val direc = messageArray.getInt(5)
            val state = messageArray.getString(6)
            val message = Message(id, data, user_id, deviceName, time, direc, state)
            messages.add(message)
            // Add message to the corresponding device group
            if (messagesByDevice.containsKey(deviceName)) {
                val deviceMessages = messagesByDevice[deviceName]?.toMutableList()
                deviceMessages?.add(message)
                messagesByDevice[deviceName] = deviceMessages ?: listOf(message)
            } else {
                messagesByDevice[deviceName] = listOf(message)
            }
        }
        return messages
    }

}
