package com.example.demoappchat.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.demoappchat.R


class ProfileFragment : Fragment() {
    private lateinit var root : View
    private lateinit var username : String
    private lateinit var password : String
    private val sharedPrefInfo = "LOGIN_INFO"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_profile, container, false)
        initViews()
        initUIEventHandlers()
        return root
    }

    private fun initUIEventHandlers() {

    }

    private fun initViews() {
        val sharedPreferences = requireContext().getSharedPreferences(sharedPrefInfo, Context.MODE_PRIVATE)
        username = sharedPreferences.getString("USERNAME", "").toString()
        password = sharedPreferences.getString("PASSWORD", "").toString()
        val usernamePf = root.findViewById<TextView>(R.id.userNamePf)
        usernamePf.text = username
    }

}