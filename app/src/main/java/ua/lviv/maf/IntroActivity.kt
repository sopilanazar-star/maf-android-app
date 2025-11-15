package ua.lviv.maf

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logo: ImageView = findViewById(R.id.logoMaf)
        val circularText: CircularTextView = findViewById(R.id.circularText)

        // ✅ ТУТ ВАЖЛИВО: ВИКЛИКАЄМО setTexts, А НЕ setText
        circularText.setTexts(
            top = "Миколаївська",
            bottom = "асоціація футболу"
        )

        // Анімація букв по колу
        circularText.startLetterByLetterAnimation(duration = 3000L)

        // Пульсація логотипу
        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 1.08f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 1.08f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Легкий 3D-нахил
        val tilt = ObjectAnimator.ofFloat(logo, "rotationY", -4f, 4f).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        scaleX.start()
        scaleY.start()
        tilt.start()

        // Перехід у MainActivity після інтро
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000L)
    }
}
