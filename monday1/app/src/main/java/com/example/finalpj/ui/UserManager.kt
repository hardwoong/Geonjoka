import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class UserManager {
    fun calculateRecommendedCaffeine(age: Int, isPregnant: Boolean): Int {
        // 예시: 나이와 임신 여부에 따른 권장 카페인 양 계산
        return when {
            isPregnant -> 200 // 임신부는 200mg
            age < 18 -> 100 // 18세 미만은 100mg
            else -> 400 // 그 외는 400mg
        }
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

    fun getUserAge(context: Context): Int {
        return getPrefs(context).getInt("user_age", 25) // 기본값 25세
    }

    fun isUserPregnant(context: Context): Boolean {
        return getPrefs(context).getBoolean("is_pregnant", false) // 기본값 false
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

    fun getUserName(context: Context): String {
        return getPrefs(context).getString("user_name", "사용자") ?: "사용자"
    }

    fun isIdExists(context: Context, id: String): Boolean {
        val prefs = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val savedId = prefs.getString("id", null)
        return savedId == id
    }

    fun saveUserDesc(context: Context, desc: String) {
        getPrefs(context).edit().apply {
            putString("user_desc", desc)
            apply()
        }
    }

    fun getUserDesc(context: Context): String {
        return getPrefs(context).getString("user_desc", "여름엔 뜨거운 겨울엔 차가운 음료") ?: "여름엔 뜨거운 겨울엔 차가운 음료"
    }

    fun saveUserEmail(context: Context, email: String) {
        getPrefs(context).edit().apply {
            putString("user_email", email)
            apply()
        }
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString("user_email", "") ?: ""
    }

    fun saveUserBirthdate(context: Context, birthdate: String) {
        getPrefs(context).edit().apply {
            putString("user_birthdate", birthdate)
            apply()
        }
    }

    fun getUserBirthdate(context: Context): String {
        return getPrefs(context).getString("user_birthdate", "") ?: ""
    }

    fun saveUserPregnancyStatus(context: Context, isPregnant: Boolean) {
        getPrefs(context).edit().apply {
            putBoolean("is_pregnant", isPregnant)
            apply()
        }
    }
}
