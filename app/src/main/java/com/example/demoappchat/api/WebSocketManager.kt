package com.example.demoappchat.api

import android.content.Context
import android.util.Log
import com.example.demoappchat.model.AppConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject

class WebSocketManager(context: Context){

    private var socket: Socket? = null
    private val sharedPreferences = context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
    private val userID = sharedPreferences.getInt("USERID", 5)

    fun connectToWebSocket() {
        try {
            val opts = IO.Options().apply {
                reconnection = true
                timeout = 3000
                query = "user_id=$userID"
            }
            socket = IO.socket(AppConfig.BASE_URL, opts)
            Log.d("WebSocketManager", "Connecting to WebSocket...")

            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on("login_success", onLoginSuccess)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)

            socket?.connect()
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Error connecting to WebSocket: ${e.message}")
        }
    }

    fun sendMessage(message: String) {
        socket?.emit("your_event_name", message)
    }

    fun disconnectWebSocket() {
        socket?.disconnect()
    }

    private val onConnect = Emitter.Listener {
        Log.d("WebSocketManager", "Connected to server")
    }

    private val onDisconnect = Emitter.Listener {
        Log.d("WebSocketManager", "Disconnected from server")
    }

    private val onLoginSuccess = Emitter.Listener { args ->
        val data = args[0] as? JSONObject
        data?.let {
            try {
                val device_id = it.getString("device_id")
                val user_id = it.getString("user_id")
                Log.d("WebSocketManager", "User $user_id logged in with device $device_id")
            } catch (e: JSONException) {
                Log.e("WebSocketManager", "Error parsing login success data: ${e.message}")
            }
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = args[0] as Exception
        Log.e("WebSocketManager", "Connection error: ${error.message}")
    }

    private val onReconnectAttempt = Emitter.Listener {
        Log.d("WebSocketManager", "Reconnecting...")
    }
}
