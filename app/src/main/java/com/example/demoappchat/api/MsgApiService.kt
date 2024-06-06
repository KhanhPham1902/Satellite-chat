package com.example.demoappchat.api

import com.example.demoappchat.model.AppConfig
import com.example.demoappchat.model.LoginInfo
import com.example.demoappchat.model.LoginResponse
import com.example.demoappchat.model.MessageData
import com.example.demoappchat.model.TimeRange
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MsgApiService {

    @POST("send-message")
    fun sendMessage(
        @Header("username") username: String,
        @Header("password") password: String,
        @Body messageData: MessageData
    ): Call<Void>

    @POST("user/messages")
    fun getUserMessages(
        @Header("username") username: String,
        @Header("password") password: String,
        @Body timeRange: TimeRange
    ): Call<ResponseBody>

    @POST("login")
    fun login(
        @Body requestBody: LoginInfo
    ): Call<LoginResponse>
}
object RetrofitClient {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: MsgApiService by lazy {
        retrofit.create(MsgApiService::class.java)
    }
}
