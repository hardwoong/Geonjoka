package com.example.finalpj.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import UserManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editBirthdate: EditText
    private lateinit var editDesc: EditText
    private lateinit var radioPregnancy: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userManager = UserManager()
        
        // View 초기화
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editBirthdate = findViewById(R.id.editBirthdate)
        editDesc = findViewById(R.id.editDesc)
        radioPregnancy = findViewById(R.id.radioPregnancy)
        
        // 현재 사용자 정보 불러오기
        val currentName = userManager.getUserName(this)
        val currentEmail = userManager.getUserEmail(this)
        val currentBirthdate = userManager.getUserBirthdate(this)
        val currentDesc = userManager.getUserDesc(this)
        val isPregnant = userManager.isUserPregnant(this)
        
        editName.setText(currentName)
        editEmail.setText(currentEmail)
        editBirthdate.setText(currentBirthdate)
        editDesc.setText(currentDesc)
        
        // 임신 여부 라디오 버튼 설정
        if (isPregnant) {
            findViewById<RadioButton>(R.id.radioPregnant).isChecked = true
        } else {
            findViewById<RadioButton>(R.id.radioNotPregnant).isChecked = true
        }
        
        // 저장 버튼 클릭 리스너
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val newName = editName.text.toString()
            val newEmail = editEmail.text.toString()
            val newBirthdate = editBirthdate.text.toString()
            val newDesc = editDesc.text.toString()
            val isPregnant = findViewById<RadioButton>(R.id.radioPregnant).isChecked
            
            if (newName.isBlank()) {
                Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (newEmail.isBlank()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (newBirthdate.isBlank()) {
                Toast.makeText(this, "생년월일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 사용자 정보 저장
            userManager.saveUserName(this, newName)
            userManager.saveUserEmail(this, newEmail)
            userManager.saveUserBirthdate(this, newBirthdate)
            userManager.saveUserDesc(this, newDesc)
            userManager.saveUserPregnancyStatus(this, isPregnant)
            
            Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
            
            // 마이페이지로 돌아가기
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        // 취소 버튼 클릭 리스너
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }
    }
} 