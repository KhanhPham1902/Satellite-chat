package com.example.demoappchat.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demoappchat.model.Message
import com.example.demoappchat.model.MessageData
import com.example.demoappchat.api.MsgApiService
import com.example.demoappchat.R
import com.example.demoappchat.adapter.MessagesAdapter
import com.example.demoappchat.api.RetrofitClient
import com.example.demoappchat.model.TimeRange
import com.example.demoappchat.databinding.ActivityMessageBinding
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class MessageActivity : AppCompatActivity() {

    private lateinit var apiService: MsgApiService
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageAdapter: MessagesAdapter
    private var originalMessages: List<Message> = emptyList() // Danh sách tin nhắn gốc
    private lateinit var username : String
    private lateinit var password : String
    private val sharedPrefInfo = "LOGIN_INFO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initRetrofit() // Gọi hàm để khởi tạo Retrofit
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessagesAdapter(originalMessages, binding.recyclerView)
        binding.recyclerView.adapter = messageAdapter

        readMessages()
        initUIEventHandlers()
        displayDataFromIntent()

        // An ban phim ao khi nhan vi tri bat ki tren man hinh
        binding.recyclerView.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            hideKeyboard()
            false
        })
    }

    private fun initRetrofit() {
        apiService = RetrofitClient.instance
    }

    private fun displayDataFromIntent() {
        val deviceName = intent.getStringExtra("devicename")
        val avatar = intent.getIntExtra("avatar", R.drawable.user)
        binding.userName.text = deviceName
        binding.profileImage.setImageResource(avatar)
    }

    private fun initUIEventHandlers() {
        binding.btnSend.setOnClickListener {
            val message = binding.textSend.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.textSend.setText("")
                // Ẩn bàn phím sau khi gửi tin nhắn
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.textSend.windowToken, 0)
            }
        }
    }

    private fun sendMessage(message: String) {
        val deviceName = intent.getStringExtra("devicename") ?: "000000000"
        val currentTime = System.currentTimeMillis().toString()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedTime = simpleDateFormat.format(Date(currentTime.toLong()))

        val messageData = MessageData(data = message, device = deviceName, time = formattedTime)

        apiService.sendMessage(username, password, messageData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    readMessages()
                } else {

                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MessageActivity, "Gửi tin nhắn không thành công", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Truy cập SharedPreferences
        val sharedPreferences = getSharedPreferences(sharedPrefInfo, Context.MODE_PRIVATE)
        username = sharedPreferences.getString("USERNAME", "").toString()
        password = sharedPreferences.getString("PASSWORD", "").toString()
    }

    private fun readMessages() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val currentTime = LocalDateTime.now()
        val startTime = currentTime.minus(100, ChronoUnit.DAYS)
        val startTimeString = startTime.format(formatter)
        val endTimeString = currentTime.format(formatter)
        val timeRange = TimeRange(startTimeString, endTimeString)
        // Gửi yêu cầu đọc tin nhắn thông qua API Retrofit
        Log.d("TAG", "$username,$password,$timeRange")

        RetrofitClient.instance.getUserMessages(username, password, timeRange).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let { body ->
                        try {
                            val jsonString = body.string()
                            val deviceName = intent.getStringExtra("devicename") ?: "000000000"
                            val messages = parseJsonToMessages(jsonString).filter { it.device == deviceName }
                            messageAdapter.updateMessages(messages)

                            Log.d("TAG", "Đọc tin nhắn thành công")
                        } catch (e: JSONException) {
                            Log.e("TAG", "Lỗi khi phân tích JSON", e)
                        }
                    }
                } else {
                    // Xử lý lỗi khi đọc tin nhắn không thành công
                    Log.d("TAG","đọc tin nhắn không thành công")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Xử lý lỗi khi gọi API không thành công
                Log.d("TAG","Gọi API không thành công")
                Log.e("TAG", "Không thể gọi API", t)
            }
        })
    }

    private fun parseJsonToMessages(jsonString: String): List<Message> {
        val jsonArray = JSONArray(jsonString)
        val messages = mutableListOf<Message>()
        for (i in 0 until jsonArray.length()) {
            val messageArray = jsonArray.getJSONArray(i)
            val id = messageArray.getInt(0)
            val data = messageArray.getString(1)
            val user_id = messageArray.getInt(2)
            val device = messageArray.getString(3)
            val time = messageArray.getString(4)
            val direc = messageArray.getInt(5)
            val state = messageArray.getString(6)
            val message = Message(id, data, user_id, device, time, direc, state)
            messages.add(message)
        }
        return messages
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //An ban phim ao
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }
}
