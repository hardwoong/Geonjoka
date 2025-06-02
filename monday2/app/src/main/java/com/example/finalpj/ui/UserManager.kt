package com.example.finalpj.ui

import android.content.Context
import android.content.SharedPreferences
import com.example.finalpj.db.AppDatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

// User 데이터 클래스 정의
data class User(
    val id: String,
    val name: String,
    val email: String,
    val birth: String,
    val isPregnant: Boolean,
    val recommendedCaffeine: Int,
    val description: String? = null
)

class UserManager(private val context: Context) {
    private val dbHelper = AppDatabaseHelper(context)

    // 현재 로그인한 userId를 SharedPreferences에서 가져옴
    private fun getCurrentUserId(): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("current_user_id", null)
    }

    // 권장 카페인 계산 (회원가입/정보수정 시 사용)
    fun calculateRecommendedCaffeine(age: Int, isPregnant: Boolean): Int {
        return when {
            isPregnant -> 200
            age < 18 -> 100
            else -> 400
        }
    }

    // 아이디 중복 체크 (DB)
    fun isIdExists(id: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id FROM User WHERE id=?", arrayOf(id))
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    // 회원가입 (User 테이블에 INSERT, description 컬럼이 있으면 함께 저장)
    fun registerUser(id: String, pw: String, name: String, email: String, birth: String, isPregnant: Boolean, recommendedCaffeine: Int, description: String? = null) {
        val db = dbHelper.writableDatabase
        try {
            if (description != null) {
                db.execSQL(
                    "INSERT INTO User (id, pw, name, email, birth, isPregnant, recommendedCaffeine, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(id, pw, name, email, birth, if (isPregnant) 1 else 0, recommendedCaffeine, description)
                )
            } else {
                db.execSQL(
                    "INSERT INTO User (id, pw, name, email, birth, isPregnant, recommendedCaffeine) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf(id, pw, name, email, birth, if (isPregnant) 1 else 0, recommendedCaffeine)
                )
            }
        } catch (_: Exception) {}
        db.close()
    }

    // 회원 정보 수정 (User 테이블에 UPDATE)
    fun updateUserInfo(name: String, email: String, birth: String, isPregnant: Boolean, recommendedCaffeine: Int, description: String) {
        val userId = getCurrentUserId() ?: return
        val db = dbHelper.writableDatabase
        db.execSQL(
            "UPDATE User SET name=?, email=?, birth=?, isPregnant=?, recommendedCaffeine=?, description=? WHERE id=?",
            arrayOf(name, email, birth, if (isPregnant) 1 else 0, recommendedCaffeine, description, userId)
        )
        db.close()
    }

    // 오늘의 카페인 섭취량 조회
    fun getTodayCaffeineIntake(): Int {
        val userId = getCurrentUserId() ?: return 0
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(intake) FROM CaffeineIntake WHERE userId=? AND date=?",
            arrayOf(userId, today)
        )
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }

    // 카페인 섭취 추가 (선택한 날짜)
    fun addCaffeineIntake(mg: Int, date: String) {
        val userId = getCurrentUserId() ?: return
        val db = dbHelper.writableDatabase
        db.execSQL(
            "INSERT INTO CaffeineIntake (userId, date, intake) VALUES (?, ?, ?)",
            arrayOf(userId, date.replace("-", ""), mg)
        )
        db.close()
    }

    // 특정 날짜의 카페인 섭취량 조회
    fun getCaffeineRecord(date: String): Int {
        val userId = getCurrentUserId() ?: return 0
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(intake) FROM CaffeineIntake WHERE userId=? AND date=?",
            arrayOf(userId, date)
        )
        var total = 0
        if (cursor.moveToFirst()) total = cursor.getInt(0)
        cursor.close()
        db.close()
        return total
    }

    // 권장 카페인량 조회
    fun getRecommendedCaffeine(): Int {
        val userId = getCurrentUserId() ?: return 400
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT recommendedCaffeine FROM User WHERE id=?",
            arrayOf(userId)
        )
        var value = 400
        if (cursor.moveToFirst()) value = cursor.getInt(0)
        cursor.close()
        db.close()
        return value
    }

    // 로그아웃 (userId 정보만 삭제)
    fun logout() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("current_user_id").apply()
    }

    // 회원탈퇴 (User, CaffeineIntake 모두 삭제)
    fun withdraw() {
        val userId = getCurrentUserId() ?: return
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM User WHERE id=?", arrayOf(userId))
        db.execSQL("DELETE FROM CaffeineIntake WHERE userId=?", arrayOf(userId))
        db.close()
        logout()
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun getTodayCaffeineIntake(context: Context): Int {
        resetCaffeineIntakeIfNewDay(context)
        return getPrefs(context).getInt("todayIntake", 0)
    }

    fun addCaffeineIntake(context: Context, mg: Int) {
        resetCaffeineIntakeIfNewDay(context)
        val prefs = getPrefs(context)
        val current = prefs.getInt("todayIntake", 0)
        prefs.edit().putInt("todayIntake", current + mg).apply()
    }

    fun resetCaffeineIntakeIfNewDay(context: Context) {
        val prefs = getPrefs(context)
        val lastDate = prefs.getString("lastDate", null)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        if (lastDate != today) {
            // 이전 날짜의 섭취량을 기록
            if (lastDate != null) {
                val lastIntake = prefs.getInt("todayIntake", 0)
                prefs.edit().putInt("caffeine_$lastDate", lastIntake).apply()
            }
            // 새로운 날짜로 초기화
            prefs.edit()
                .putInt("todayIntake", 0)
                .putString("lastDate", today)
                .apply()
        }
    }

    fun getCaffeineRecord(context: Context, date: String): Int {
        return getPrefs(context).getInt("caffeine_$date", 0)
    }

    fun saveUserInfo(context: Context, age: Int, isPregnant: Boolean) {
        getPrefs(context).edit().apply {
            putInt("user_age", age)
            putBoolean("is_pregnant", isPregnant)
            putInt("recommendedCaffeine", calculateRecommendedCaffeine(age, isPregnant))
            apply()
        }
    }

    fun getUserAge(): Int {
        val userId = getCurrentUserId() ?: return 25
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT birth FROM User WHERE id=?", arrayOf(userId))
        var age = 25
        if (cursor.moveToFirst()) {
            val birth = cursor.getString(0)
            try {
                val birthYear = birth.split("-")[0].toInt()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                age = currentYear - birthYear
            } catch (_: Exception) {}
        }
        cursor.close()
        db.close()
        return age
    }

    fun isUserPregnant(): Boolean {
        val userId = getCurrentUserId() ?: return false
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT isPregnant FROM User WHERE id=?", arrayOf(userId))
        var isPregnant = false
        if (cursor.moveToFirst()) isPregnant = cursor.getInt(0) == 1
        cursor.close()
        db.close()
        return isPregnant
    }

    fun getRecommendedCaffeine(context: Context): Int {
        return getPrefs(context).getInt("recommendedCaffeine", 400)
    }

    fun logout(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
    }

    fun withdraw(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    fun saveUserName(context: Context, name: String) {
        getPrefs(context).edit().apply {
            putString("user_name", name)
            apply()
        }
    }

    fun getUserName(): String {
        val userId = getCurrentUserId() ?: return "사용자"
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT name FROM User WHERE id=?", arrayOf(userId))
        var name = "사용자"
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        db.close()
        return name
    }

    fun getUserEmail(): String {
        val userId = getCurrentUserId() ?: return ""
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT email FROM User WHERE id=?", arrayOf(userId))
        var email = ""
        if (cursor.moveToFirst()) email = cursor.getString(0)
        cursor.close()
        db.close()
        return email
    }

    fun getUserBirthdate(): String {
        val userId = getCurrentUserId() ?: return ""
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT birth FROM User WHERE id=?", arrayOf(userId))
        var birth = ""
        if (cursor.moveToFirst()) birth = cursor.getString(0)
        cursor.close()
        db.close()
        return birth
    }

    fun getUserDesc(): String {
        val userId = getCurrentUserId() ?: return "여름엔 뜨거운 겨울엔 차가운 음료"
        val db = dbHelper.readableDatabase
        var description = "여름엔 뜨거운 겨울엔 차가운 음료"
        try {
            val cursor = db.rawQuery("SELECT description FROM User WHERE id=?", arrayOf(userId))
            if (cursor.moveToFirst()) description = cursor.getString(0)
            cursor.close()
        } catch (e: Exception) {
            // description 컬럼이 없으면 기본값 반환
        }
        db.close()
        return description
    }

    fun saveUserDesc(description: String) {
        val userId = getCurrentUserId() ?: return
        val db = dbHelper.writableDatabase
        try {
            db.execSQL("UPDATE User SET description=? WHERE id=?", arrayOf(description, userId))
        } catch (_: Exception) {}
        db.close()
    }

    fun saveUserEmail(context: Context, email: String) {
        getPrefs(context).edit().apply {
            putString("user_email", email)
            apply()
        }
    }

    fun saveUserBirthdate(context: Context, birthdate: String) {
        getPrefs(context).edit().apply {
            putString("user_birthdate", birthdate)
            apply()
        }
    }

    fun saveUserPregnancyStatus(context: Context, isPregnant: Boolean) {
        getPrefs(context).edit().apply {
            putBoolean("is_pregnant", isPregnant)
            apply()
        }
    }

    // 로그인 함수 추가
    fun login(id: String, pw: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, email, birth, isPregnant, recommendedCaffeine FROM User WHERE id=? AND pw=?",
            arrayOf(id, pw)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getString(0),
                name = cursor.getString(1),
                email = cursor.getString(2),
                birth = cursor.getString(3),
                isPregnant = cursor.getInt(4) == 1,
                recommendedCaffeine = cursor.getInt(5)
            )
            // 로그인 성공 시 현재 userId를 SharedPreferences에 저장
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("current_user_id", id).apply()
        }
        cursor.close()
        db.close()
        return user
    }
}
