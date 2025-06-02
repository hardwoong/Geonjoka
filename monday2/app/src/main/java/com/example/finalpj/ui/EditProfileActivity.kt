package com.example.finalpj.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import com.example.finalpj.ui.UserManager
import android.app.DatePickerDialog
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editBirthdate: EditText
    private lateinit var editDescription: EditText
    private lateinit var radioPregnancy: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userManager = UserManager(this)
        
        // View 초기화
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editBirthdate = findViewById(R.id.editBirthdate)
        editDescription = findViewById(R.id.editDesc)
        radioPregnancy = findViewById(R.id.radioPregnancy)
        
        // 현재 사용자 정보 불러오기 (DB 기반)
        val currentName = userManager.getUserName()
        val currentEmail = userManager.getUserEmail()
        val currentBirthdate = userManager.getUserBirthdate()
        val currentDescription = userManager.getUserDesc()
        val isPregnant = userManager.isUserPregnant()
        
        editName.setText(currentName)
        editEmail.setText(currentEmail)
        editBirthdate.setText(currentBirthdate)
        editDescription.setText(currentDescription)
        
        // 임신 여부 라디오 버튼 설정
        if (isPregnant) {
            findViewById<RadioButton>(R.id.radioPregnant).isChecked = true
        } else {
            findViewById<RadioButton>(R.id.radioNotPregnant).isChecked = true
        }
        
        // 생년월일 EditText 클릭 시 DatePickerDialog 띄우기
        editBirthdate.setOnClickListener {
            val cal = Calendar.getInstance()
            // 기존 값이 있으면 해당 날짜로 초기화
            val birth = editBirthdate.text.toString()
            if (birth.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                val parts = birth.split("-")
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                editBirthdate.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            }, year, month, day).show()
        }
        
        // 저장 버튼 클릭 리스너
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val newName = editName.text.toString()
            val newEmail = editEmail.text.toString()
            val newBirthdate = editBirthdate.text.toString()
            val newDescription = editDescription.text.toString()
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
            
            // 나이 계산
            val birthYear = newBirthdate.split("-")[0].toInt()
            val currentYear = java.time.LocalDate.now().year
            val age = currentYear - birthYear
            
            // 권장 카페인 계산
            val recommendedCaffeine = userManager.calculateRecommendedCaffeine(age, isPregnant)
            
            // 사용자 정보 저장 (DB 기반)
            userManager.updateUserInfo(newName, newEmail, newBirthdate, isPregnant, recommendedCaffeine, newDescription)
            
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