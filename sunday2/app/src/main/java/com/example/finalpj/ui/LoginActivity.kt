package com.example.finalpj.ui

import UserManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import android.content.SharedPreferences
import android.util.Log

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val inputId = findViewById<EditText>(R.id.editId)
        val inputPw = findViewById<EditText>(R.id.editPw)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val welcomeMsg = findViewById<TextView>(R.id.txtWelcome)
        val formLayout = findViewById<LinearLayout>(R.id.loginForm)
        val signupBtn = findViewById<Button>(R.id.btnSignup)

        loginBtn.setOnClickListener {
            val id = inputId.text.toString()
            val pw = inputPw.text.toString()

            val sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE)
            val savedId = sharedPreferences.getString("id", "")
            val savedPw = sharedPreferences.getString("pw", "")
            val savedName = sharedPreferences.getString("name", "")

            if (id == savedId && pw == savedPw) {
                // UserManager 값 확인 (디버깅용)
                val userManager = UserManager()
                val age = userManager.getUserAge(this)
                val isPregnant = userManager.isUserPregnant(this)
                Log.d("LoginActivity", "User age: $age, isPregnant: $isPregnant")
                
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("userId", id)
                intent.putExtra("userName", savedName)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "ID 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        signupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}