package com.example.finalpj.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val userId = intent.getStringExtra("userId") ?: "사용자"
        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        txtWelcome.text = "$userId 님 환영합니다!"

    }
}