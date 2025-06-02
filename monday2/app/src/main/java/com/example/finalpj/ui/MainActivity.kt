package com.example.finalpj.ui

import com.example.finalpj.ui.UserManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection

class MainActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var txtCaffeinePercent: TextView
    private lateinit var editDrinkDetail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userManager = UserManager(this)
        txtCaffeinePercent = findViewById(R.id.txtCaffeinePercent)
        editDrinkDetail = findViewById(R.id.editDrinkDetail)

        // 초기 카페인 섭취량 표시
        updateCaffeineDisplay()

        // 음료 입력 버튼 클릭 이벤트
        findViewById<Button>(R.id.btnAddDrink).setOnClickListener {
            val drinkDetail = editDrinkDetail.text.toString()
            if (drinkDetail.isNotEmpty()) {
                estimateCaffeineContent(drinkDetail)
            } else {
                Toast.makeText(this, "음료 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 네브바 클릭 이벤트
        findViewById<View>(R.id.nav_home).setOnClickListener {
            // 이미 홈이므로 아무 동작 X
        }
        findViewById<View>(R.id.nav_record).setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<View>(R.id.nav_rcmd).setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<View>(R.id.nav_my).setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateCaffeineDisplay() {
        val recommended = userManager.getRecommendedCaffeine()
        val todayIntake = userManager.getTodayCaffeineIntake()
        val percent = (todayIntake * 100 / recommended).coerceAtMost(100)
    
        txtCaffeinePercent.text = "${percent}% 섭취 (${todayIntake}/${recommended} mg)"
    
        // 여기서 beanGauge에 카페인 비율을 넘김!
        val beanGauge = findViewById<BeanGaugeView>(R.id.beanGauge)
beanGauge.caffeineRatio = todayIntake.toFloat() / recommended.toFloat()

    }
    

    private fun estimateCaffeineContent(drinkDetail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // OpenAI API 호출
                val apiKey = ""
                val url = URL("https://api.openai.com/v1/chat/completions")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.doOutput = true

                val prompt = """
                    다음 음료의 대략적인 카페인 함량을 mg 단위로 추정해주세요. 
                    숫자만 반환해주세요. 예: 100
                    음료: $drinkDetail
                """.trimIndent()

                val messagesArray = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                val jsonInput = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", messagesArray)
                    put("temperature", 0.7)
                }

                connection.outputStream.use { os ->
                    os.write(jsonInput.toString().toByteArray())
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)
                val content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                // 숫자만 추출
                val caffeineAmount = content.filter { it.isDigit() }.toIntOrNull() ?: 0

                withContext(Dispatchers.Main) {
                    if (caffeineAmount > 0) {
                        // 오늘 날짜를 yyyy-MM-dd 형식으로 구함
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        userManager.addCaffeineIntake(caffeineAmount, today)
                        updateCaffeineDisplay()
                        editDrinkDetail.text.clear()
                        Toast.makeText(this@MainActivity, 
                            "추정 카페인 함량: ${caffeineAmount}mg가 추가되었습니다.", 
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, 
                            "카페인 함량을 추정할 수 없습니다.", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, 
                        "오류가 발생했습니다: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}