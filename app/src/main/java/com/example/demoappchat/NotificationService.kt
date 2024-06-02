package com.example.demoappchat

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.Request
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class NotificationService : Service() {

    private val CHANNEL_ID = "ForegroundServiceChannel"
    private val NOTIFICATION_ID = 123
    private lateinit var sharedPreferences: SharedPreferences
    private var userID: Int = 5
    private lateinit var mSocket: Socket

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("USERID", 5)
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "1 onStartCommand")

        // Kết nối đến server và lắng nghe thông điệp
        connectToServerAndListen()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        Log.d(TAG, "2 createNotificationChannel")

        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun connectToServerAndListen() {
        try {
            val opts = IO.Options().apply {
                reconnection = true
                timeout = 3000
                query = "user_id=$userID"
            }
            mSocket = IO.socket(AppConfig.BASE_URL, opts)

            mSocket.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Connected to server")
            }.on("new_message", onNewMessage)

            mSocket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (args.isNotEmpty()) {
            val data = args[0] as JSONObject
            val message = data.getString("content")
            val deviceId = data.getString("device_id")
            showNotification(deviceId, message)
        }
    }
    private fun showNotification(deviceId: String, message: String) {
        Log.d(TAG, "7 showNotification: $message")

        val notificationIntent = Intent(applicationContext, LoginActivity::class.java).apply {
            putExtra("device_id", deviceId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(deviceId)
            .setContentText(message)
            .setSmallIcon(R.drawable.user)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "8 onDestroy")
        mSocket.disconnect()
        mSocket.off("new_message", onNewMessage)
    }

    companion object {
        private const val TAG = "NotificationService"
    }
}
