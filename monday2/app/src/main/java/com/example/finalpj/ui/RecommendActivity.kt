package com.example.finalpj.ui

import com.example.finalpj.ui.UserManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RecommendActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var txtRecommend: TextView
    private lateinit var btnRetry: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend)

        userManager = UserManager(this)
        txtRecommend = findViewById(R.id.txtRecommend)
        btnRetry = findViewById(R.id.btnRetry)

        // 네브바 상태 변경
        findViewById<ImageView>(R.id.imgRcmd).setImageResource(R.drawable.nav_rcmd)
        findViewById<TextView>(R.id.txtRcmd).setTextColor(Color.parseColor("#B03E2C"))
        findViewById<ImageView>(R.id.imgHome).setImageResource(R.drawable.nav_no_home)
        findViewById<TextView>(R.id.txtHome).setTextColor(Color.parseColor("#888888"))
        findViewById<ImageView>(R.id.imgRecord).setImageResource(R.drawable.nav_no_report)
        findViewById<TextView>(R.id.txtRecord).setTextColor(Color.parseColor("#888888"))
        findViewById<ImageView>(R.id.imgMy).setImageResource(R.drawable.nav_no_my)
        findViewById<TextView>(R.id.txtMy).setTextColor(Color.parseColor("#888888"))

        btnRetry.setOnClickListener {
            recommendDrink()
        }

        // 앱 시작 시에도 추천
        recommendDrink()

        // 네브바 이동(RecordActivity 참고)
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<LinearLayout>(R.id.nav_record).setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
            finish()
        }
        findViewById<LinearLayout>(R.id.nav_my).setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<LinearLayout>(R.id.nav_rcmd).setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun recommendDrink() {
        val recommended = userManager.getRecommendedCaffeine()
        val todayIntake = userManager.getTodayCaffeineIntake()
        val remain = recommended - todayIntake

        if (remain <= 0) {
            txtRecommend.text = "오늘의 권장 카페인 섭취량을 이미 초과하였습니다.\n더 이상 음료를 추천할 수 없습니다."
            return
        }

        // AI에게 추천 요청
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = ""
                val url = URL("https://api.openai.com/v1/chat/completions")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.doOutput = true

                val prompt = """
                    사용자의 남은 카페인 권장량은 약 ${remain}mg입니다.
                    이 범위 내에서 마실 수 있는 한국에서 구하기 쉬운 커피/음료 1가지를 추천해 주세요.
                    반드시 다음 형식으로 답변해주세요:
                    "브랜드명 음료명(사이즈) - 약 XXmg"
                    예시: "스타벅스 아메리카노(Tall) - 약 150mg"
                    사이즈는 Tall, Grande, Venti, Regular, Large 등 정확한 사이즈를 포함해주세요.
                """.trimIndent()

                val messagesArray = JSONArray().apply {
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

                withContext(Dispatchers.Main) {
                    txtRecommend.text = "추천 음료: $content"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    txtRecommend.text = "추천을 가져오는 중 오류가 발생했습니다: ${e.message}"
                }
            }
        }
    }
}
