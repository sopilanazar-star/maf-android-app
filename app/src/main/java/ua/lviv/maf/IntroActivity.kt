package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // показуємо наш layout зі стадіоном
        setContentView(R.layout.activity_intro)

        // через 2 секунди відкриваємо основну активність з WebView
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()  // щоб по "Назад" не повертатися на інтро
        }, 2000L)
    }
}
