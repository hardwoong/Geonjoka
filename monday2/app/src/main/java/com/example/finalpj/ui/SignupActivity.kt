package com.example.finalpj.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import android.graphics.Color
import android.text.Editable
import java.util.Calendar
import android.app.DatePickerDialog
import androidx.core.content.ContextCompat

class SignupActivity : AppCompatActivity() {
    private lateinit var editId: EditText
    private lateinit var btnSignupNext: Button
    private lateinit var txtIdError: TextView
    private lateinit var txtPwError: TextView
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        userManager = UserManager(this)
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
            val email = findViewById<EditText>(R.id.editEmail).text.toString()

            if (userManager.isIdExists(id)) {
                Toast.makeText(this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pw != pwCheck) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2단계 레이아웃으로 전환
            setContentView(R.layout.activity_signup2)

            // 2단계 뷰들 새로 findViewById로 찾기
            val birthEdit = findViewById<EditText>(R.id.editBirth)
            val chkPregnant = findViewById<CheckBox>(R.id.chkPregnant)
            val chkAdAgree = findViewById<CheckBox>(R.id.chkAdAgree)
            val chkAgree2 = findViewById<CheckBox>(R.id.chkAgree2)
            val nameEdit = findViewById<EditText>(R.id.editName)
            val descriptionEdit = findViewById<EditText>(R.id.editDescription)
            val finishBtn = findViewById<Button>(R.id.btnSignupFinish)

            // 생년월일 EditText 클릭 시 DatePickerDialog 띄우기
            birthEdit.setOnClickListener {
                val cal = Calendar.getInstance()
                val birth = birthEdit.text.toString()
                // 기존 값이 있으면 해당 날짜로 초기화
                val regex = Regex("\\d{4}\\.\\d{2}\\.\\d{2}")
                if (birth.matches(regex)) {
                    val parts = birth.split(".")
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)
                val day = cal.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(this, { _, y, m, d ->
                    birthEdit.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
                }, year, month, day).show()
            }

            finishBtn.setOnClickListener {
                // 2단계 입력값 처리 및 회원가입 완료 로직
                val birth = birthEdit.text.toString()
                val isPregnant = chkPregnant.isChecked
                val adAgree = chkAdAgree.isChecked
                val agree2 = chkAgree2.isChecked
                val name = nameEdit.text.toString()
                val description = descriptionEdit.text.toString()

                if (!agree2) {
                    Toast.makeText(this, "개인정보 수집 동의는 필수입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 나이 계산
                val age = calculateAge(birth)
                // 권장 카페인양 계산 및 저장
                val recommendedCaffeine = userManager.calculateRecommendedCaffeine(age, isPregnant)
                // 회원가입
                userManager.registerUser(id, pw, name, email, birth, isPregnant, recommendedCaffeine, description)
                // 로그인 상태로 userId 저장
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                prefs.edit().putString("current_user_id", id).apply()
                // 회원가입 완료 처리
                Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
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
            btnSignupNext.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray))
            return
        }
        if (id.length < 4) {
            txtIdError.text = "아이디는 4자 이상이어야 합니다"
            txtIdError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            btnSignupNext.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray))
            return
        }
        if (userManager.isIdExists(id)) {
            txtIdError.text = "이미 사용 중인 아이디입니다"
            txtIdError.setTextColor(Color.RED)
            btnSignupNext.isEnabled = false
            btnSignupNext.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray))
        } else {
            txtIdError.text = "사용 가능한 아이디입니다"
            txtIdError.setTextColor(Color.GREEN)
            btnSignupNext.isEnabled = true
            btnSignupNext.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.main_brown))
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
        val birthYear = birth.split("-")[0].toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return currentYear - birthYear
    }
}
