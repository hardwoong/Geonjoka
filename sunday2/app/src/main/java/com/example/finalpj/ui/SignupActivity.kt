package com.example.finalpj.ui

import UserManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import android.content.SharedPreferences
import android.graphics.Color
import android.text.Editable
import android.content.Context

class SignupActivity : AppCompatActivity() {
    private lateinit var editId: EditText
    private lateinit var btnSignupNext: Button
    private lateinit var txtIdError: TextView
    private lateinit var txtPwError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        editId = findViewById(R.id.editId)
        btnSignupNext = findViewById(R.id.btnSignupNext)
        txtIdError = findViewById(R.id.txtIdError)
        txtPwError = findViewById(R.id.txtPwError)

        // 아이디 입력 시 실시간 체크
        editId.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkIdAvailability()
            }
        })

        val editPw = findViewById<EditText>(R.id.editPw)
        val editPwCheck = findViewById<EditText>(R.id.editPwCheck)

        editPw.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPwValidity()
            }
        })
        editPwCheck.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkPwValidity()
            }
        })

        btnSignupNext.setOnClickListener {
            val id = editId.text.toString()
            val pw = findViewById<EditText>(R.id.editPw).text.toString()
            val pwCheck = findViewById<EditText>(R.id.editPwCheck).text.toString()

            if (isIdExists(this, id)) {
                Toast.makeText(this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw != pwCheck) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

                if (!agree2) {
                    Toast.makeText(this, "개인정보 수집 동의는 필수입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 나이 계산
                val age = calculateAge(birth)
                
                // 권장 카페인양 계산 및 저장
                val recommendedCaffeine = calculateRecommendedCaffeine(age, isPregnant)
                
                // 나머지 회원 정보는 기존대로 저장
                val sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("id", id)
                editor.putString("pw", pw)
                editor.putString("email", email)
                editor.putString("birth", birth)
                editor.putInt("recommendedCaffeine", recommendedCaffeine)
                editor.putString("name", name)
                editor.putBoolean("is_pregnant", isPregnant)
                editor.apply()

                // UserManager에도 임신여부 저장
                val userManager = UserManager()
                userManager.saveUserInfo(this, age, isPregnant)
                userManager.saveUserName(this, name)

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

    private fun checkIdAvailability() {
        val id = editId.text.toString()
        if (id.isEmpty()) {
            txtIdError.text = "아이디를 입력해주세요"
            txtIdError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            return
        }

        if (isIdExists(this, id)) {
            txtIdError.text = "이미 사용 중인 아이디입니다"
            txtIdError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
        } else {
            txtIdError.text = "사용 가능한 아이디입니다"
            txtIdError.setTextColor(Color.GREEN)
            btnSignupNext.isEnabled = true
        }
    }

    private fun checkPwValidity() {
        val pw = findViewById<EditText>(R.id.editPw).text.toString()
        val pwCheck = findViewById<EditText>(R.id.editPwCheck).text.toString()

        if (pw.isEmpty()) {
            txtPwError.text = "비밀번호를 입력해주세요"
            txtPwError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            return
        }
        if (pw.length < 6) {
            txtPwError.text = "비밀번호는 6자 이상이어야 합니다"
            txtPwError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            return
        }
        if (pwCheck.isNotEmpty() && pw != pwCheck) {
            txtPwError.text = "비밀번호가 일치하지 않습니다"
            txtPwError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            return
        }
        if (pw.isNotEmpty() && pw != pwCheck) {
            txtPwError.text = "비밀번호가 일치하지 않습니다"
            txtPwError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            return
        }
        txtPwError.text = "사용 가능한 비밀번호입니다"
        txtPwError.setTextColor(Color.GREEN)
        btnSignupNext.isEnabled = true
    }

    // 생년월일에서 나이 계산하는 함수
    private fun calculateAge(birth: String): Int {
        val birthYear = birth.split(".")[0].toInt()
        val currentYear = java.time.LocalDate.now().year
        return currentYear - birthYear
    }

    // 아이디 중복 체크 함수
    private fun isIdExists(context: Context, id: String): Boolean {
        val prefs = context.getSharedPreferences("UserInfo", MODE_PRIVATE)
        val savedId = prefs.getString("id", null)
        return savedId == id
    }

    // 권장 카페인 계산 함수
    private fun calculateRecommendedCaffeine(age: Int, isPregnant: Boolean): Int {
        return if (isPregnant) 200
        else if (age < 19) 100
        else 400
    }
}
