package com.example.finalpj.ui

import UserManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.finalpj.R

class MyPageActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private lateinit var txtUserName: TextView
    private lateinit var txtGreeting: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        userManager = UserManager()
        txtUserName = findViewById(R.id.txtUserName)
        txtGreeting = findViewById(R.id.txtGreeting)

        // 사용자 이름 설정
        val userName = userManager.getUserName(this)
        val userDesc = userManager.getUserDesc(this)
        txtUserName.text = "${userName}님"
        txtGreeting.text = "${userName}님, 오늘도 건강히 카페인 수혈해요!"
        findViewById<TextView>(R.id.txtUserDesc).text = userDesc

        // 네브바 상태 변경
        findViewById<ImageView>(R.id.imgMy).setImageResource(R.drawable.nav_my)
        findViewById<TextView>(R.id.txtMy).setTextColor(Color.parseColor("#B03E2C"))
        findViewById<ImageView>(R.id.imgHome).setImageResource(R.drawable.nav_no_home)
        findViewById<TextView>(R.id.txtHome).setTextColor(Color.parseColor("#888888"))
        findViewById<ImageView>(R.id.imgRecord).setImageResource(R.drawable.nav_no_report)
        findViewById<TextView>(R.id.txtRecord).setTextColor(Color.parseColor("#888888"))
        findViewById<ImageView>(R.id.imgRcmd).setImageResource(R.drawable.nav_no_rcmd)
        findViewById<TextView>(R.id.txtRcmd).setTextColor(Color.parseColor("#888888"))

        // 네브바 이동
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
        findViewById<LinearLayout>(R.id.nav_rcmd).setOnClickListener {
            val intent = Intent(this, RecommendActivity::class.java)
            startActivity(intent)
            finish()
        }
        // 마이페이지는 현재 화면이므로 nav_my는 동작 없음

        // 버튼 클릭 리스너
        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // 로그아웃 확인 다이얼로그
            AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    // 로그아웃 처리
                    userManager.logout(this)
                    // 로그인 화면으로 이동
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        findViewById<Button>(R.id.btnWithdraw).setOnClickListener {
            // 회원탈퇴 확인 다이얼로그
            AlertDialog.Builder(this)
                .setTitle("회원탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?\n탈퇴 시 모든 정보가 삭제되며 복구할 수 없습니다.")
                .setPositiveButton("확인") { _, _ ->
                    // 회원탈퇴 처리
                    userManager.withdraw(this)
                    Toast.makeText(this, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    // 회원가입 화면으로 이동
                    val intent = Intent(this, SignupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    }
}
