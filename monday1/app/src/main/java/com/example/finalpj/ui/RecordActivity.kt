package com.example.finalpj.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import java.text.SimpleDateFormat
import java.util.*
import UserManager

class RecordActivity : AppCompatActivity() {
    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        userManager = UserManager()
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val txtCaffeineRecord = findViewById<TextView>(R.id.txtCaffeineRecord)
        val editCaffeine = findViewById<EditText>(R.id.editCaffeine)
        val btnAddCaffeine = findViewById<Button>(R.id.btnAddCaffeine)

        // 초기 날짜 설정 및 오늘의 카페인 섭취량 표시
        updateCaffeineDisplay(selectedDate, txtCaffeineRecord)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month+1, dayOfMonth)
            updateCaffeineDisplay(selectedDate, txtCaffeineRecord)
        }

        // 카페인 추가 버튼 클릭 리스너
        btnAddCaffeine.setOnClickListener {
            val caffeineInput = editCaffeine.text.toString()
            if (caffeineInput.isNotEmpty()) {
                val caffeineAmount = caffeineInput.toInt()
                userManager.addCaffeineIntake(this, caffeineAmount)
                
                // 입력 필드 초기화
                editCaffeine.text.clear()
                
                // 화면 업데이트
                updateCaffeineDisplay(selectedDate, txtCaffeineRecord)
                
                Toast.makeText(this, "${caffeineAmount}mg 추가되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "카페인량을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 기록 아이콘만 활성화, 나머지는 비활성화 이미지로
        findViewById<ImageView>(R.id.imgRecord).setImageResource(R.drawable.nav_report)
        findViewById<TextView>(R.id.txtRecord).setTextColor(Color.parseColor("#B03E2C"))

        findViewById<ImageView>(R.id.imgHome).setImageResource(R.drawable.nav_no_home)
        findViewById<TextView>(R.id.txtHome).setTextColor(Color.parseColor("#888888"))
        findViewById<ImageView>(R.id.imgRcmd).setImageResource(R.drawable.nav_no_rcmd)
        findViewById<TextView>(R.id.txtRcmd).setTextColor(Color.parseColor("#888888"))
        findViewById<ImageView>(R.id.imgMy).setImageResource(R.drawable.nav_no_my)
        findViewById<TextView>(R.id.txtMy).setTextColor(Color.parseColor("#888888"))

        // 네브바 클릭 시 화면 이동
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<LinearLayout>(R.id.nav_rcmd).setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<LinearLayout>(R.id.nav_my).setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateCaffeineDisplay(date: String, txtCaffeineRecord: TextView) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val caffeine = if (date == today) {
            userManager.getTodayCaffeineIntake(this)
        } else {
            // 날짜 형식 변환 (yyyy-MM-dd -> yyyyMMdd)
            val recordDate = date.replace("-", "")
            userManager.getCaffeineRecord(this, recordDate)
        }
        txtCaffeineRecord.text = "$date: ${caffeine}mg 섭취"
    }
}
