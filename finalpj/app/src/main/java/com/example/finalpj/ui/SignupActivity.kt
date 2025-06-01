package com.example.finalpj.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nextBtn = findViewById<Button>(R.id.btnSignupNext)
        nextBtn.setOnClickListener {
            // 1단계 입력값 검증 등 필요하면 여기서 처리

            // 2단계 레이아웃으로 전환
            setContentView(R.layout.activity_signup2)

            // 2단계 뷰들 새로 findViewById로 찾기
            val emailEdit = findViewById<EditText>(R.id.editEmail)
            val birthEdit = findViewById<EditText>(R.id.editBirth)
            val chkPregnant = findViewById<CheckBox>(R.id.chkPregnant)
            val chkAdAgree = findViewById<CheckBox>(R.id.chkAdAgree)
            val chkAgree2 = findViewById<CheckBox>(R.id.chkAgree2)
            val nameEdit = findViewById<EditText>(R.id.editName)
            val finishBtn = findViewById<Button>(R.id.btnSignupFinish)

            finishBtn.setOnClickListener {
                // 2단계 입력값 처리 및 회원가입 완료 로직
                val email = emailEdit.text.toString()
                val birth = birthEdit.text.toString()
                val isPregnant = chkPregnant.isChecked
                val adAgree = chkAdAgree.isChecked
                val agree2 = chkAgree2.isChecked
                val name = nameEdit.text.toString()

                // 예시: 필수 동의 체크 안 했을 때 안내
                if (!agree2) {
                    Toast.makeText(this, "개인정보 수집 동의는 필수입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 회원가입 완료 처리 (예: DB 저장, 화면 이동 등)
                Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                // 로그인 화면으로 이동
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }
}
