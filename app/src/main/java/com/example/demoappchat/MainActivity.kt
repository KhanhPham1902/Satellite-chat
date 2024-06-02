package com.example.demoappchat

import ViewPagerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var userName: TextView
    private val sharedPrefInfo = "LOGIN_INFO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Get views from layout
        profileImage = findViewById(R.id.profileImage)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager2)
        userName = findViewById(R.id.userName)

        // Set avatar and username
        val username = intent.getStringExtra("USERNAME")

        profileImage.setImageResource(R.drawable.user)
        userName.text = username
        // Set up ViewPager and TabLayout
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPagerAdapter.addFragment(ChatsFragment(), "Hải trình")
        viewPagerAdapter.addFragment(DevicesFragment(), "Nhắn tin")
        viewPagerAdapter.addFragment(ProfileFragment(), "Tính cước")
        viewPager2.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = viewPagerAdapter.getPageTitle(position)
        }.attach()

        // Kich hoat dich vu thong bao
        startNotificationService()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                // Xử lý sự kiện đăng xuất
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startNotificationService() {
        val serviceIntent = Intent(this, NotificationService::class.java)
        startService(serviceIntent)
        val webSocketManager = WebSocketManager(this)
        webSocketManager.connectToWebSocket()
    }

    private fun logout() {
        // Xoá SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefInfo, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Đóng MainActivity
        finish()

        // Mở LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}
