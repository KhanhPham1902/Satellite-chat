package com.example.demoappchat.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demoappchat.model.LoginInfo
import com.example.demoappchat.model.LoginResponse
import com.example.demoappchat.R
import com.example.demoappchat.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var imgHidePass: ImageButton
    private lateinit var layoutLogin: LinearLayout
    private val sharedPrefInfo = "LOGIN_INFO"
    private var count = 0

    // Lưu tên người dùng và mật khẩu vào SharedPreferences
    private fun saveCredentials(username: String, password: String) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefInfo, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("USERNAME", username)
        editor.putString("PASSWORD", password)
        editor.apply()
    }

    // Kiểm tra và tự động đăng nhập từ SharedPreferences
    private fun autoLogin() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefInfo, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", "")
        val password = sharedPreferences.getString("PASSWORD", "")
        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            loginUser(username, password)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        autoLogin()
        // Ánh xạ các view từ layout
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.btn_login)
        forgotPasswordTextView = findViewById(R.id.forgot_password)
        imgHidePass = findViewById(R.id.imgHidePass)
        layoutLogin = findViewById(R.id.layoutLogin)

        // Xử lý sự kiện khi nhấn nút Đăng nhập
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Gọi hàm đăng nhập
            loginUser(email, password)
            Log.d("TAG", "onCreate: $email,$password")
        }


        // Hien thi/ an mat khau
        imgHidePass.setOnClickListener(View.OnClickListener { v: View? ->
            count++
            val pass: String = passwordEditText.getText().toString()
            if (count % 2 == 1) { // Hien thi mat khau
                if (pass.isNotEmpty()) {
                    passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                    imgHidePass.setImageResource(R.drawable.visibility_off)
                    passwordEditText.setSelection(passwordEditText.text.length) // Dat lai vi tri con tro
                }
            } else { // An mat khau
                if (pass.isNotEmpty()) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    imgHidePass.setImageResource(R.drawable.visibility)
                    passwordEditText.setSelection(passwordEditText.text.length) // Dat lai vi tri con tro
                }
            }
        })

        // An ban phim ao khi nhan vi tri bat ki tren man hinh
        layoutLogin.setOnTouchListener(View.OnTouchListener { v: View?, event: MotionEvent? ->
            hideKeyboard()
            false
        })

        // Xử lý sự kiện khi nhấn vào Quên mật khẩu
        forgotPasswordTextView.setOnClickListener {
            // Xử lý logic cho việc quên mật khẩu
        }
    }

    private fun loginUser(username: String, password: String) {
        // Tạo request body từ thông tin người dùng
        val requestBody = LoginInfo(username, password)

        // Gọi API đăng nhập bằng Retrofit
        val call: Call<LoginResponse> = RetrofitClient.instance.login(requestBody)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.user_id
                    val sharedPreferences = getSharedPreferences(sharedPrefInfo, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    if (userId != null) {
                        editor.putInt("USERID", userId)
                    }
                    editor.apply()
                    saveCredentials(username,password)

                    Toast.makeText(this@LoginActivity, "Xin chào, $username!", Toast.LENGTH_SHORT).show()

                    // Chuyển đến màn hình chính
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    finish() // Kết thúc activity hiện tại
                } else {
                    // Đăng nhập thất bại
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Xử lý khi gặp lỗi kết nối hoặc lỗi server
                Toast.makeText(this@LoginActivity, "Failed to connect to server: ${t.message}", Toast.LENGTH_SHORT).show()
                // Log thông tin chi tiết về lỗi
                Log.e("LoginActivity", "Failed to connect to server", t)
            }
        })
    }

    //An ban phim ao
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }
}
