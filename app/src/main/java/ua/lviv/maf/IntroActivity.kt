package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val circularText: CircularTextView = findViewById(R.id.circularText)

        // Один текст навколо герба
        circularText.setText("Миколаївська асоціація футболу")

        // Анімація появи букв
        circularText.startLetterByLetterAnimation(duration = 3000L)

        // Перехід у головну активність після інтро
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3500L)
    }
}
