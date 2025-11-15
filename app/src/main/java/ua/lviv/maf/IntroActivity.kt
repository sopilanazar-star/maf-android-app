package ua.lviv.maf

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private val fullText = "МИКОЛАЇВСЬКА АСОЦІАЦІЯ ФУТБОЛУ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logoView: ImageView = findViewById(R.id.logoView)
        val mafText: TextView = findViewById(R.id.mafText)

        // 🔹 3D-пульсація логотипа
        startLogoAnimation(logoView)

        // 🔹 Побуквенна поява тексту знизу
        animateTextLetterByLetter(mafText, fullText, interval = 120L)

        // 🔹 Перехід у основний додаток після інтро (5 секунд)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000L)
    }

    // --- Пульсація та легкий 3D-нахил логотипа ---
    private fun startLogoAnimation(logo: ImageView) {
        // масштаб (пульсація)
        val scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.9f, 1.05f).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.9f, 1.05f).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // легкий 3D-нахил
        val tilt = ObjectAnimator.ofFloat(logo, "rotationY", -6f, 6f).apply {
            duration = 1600
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        scaleX.start()
        scaleY.start()
        tilt.start()
    }

    // --- Побуквенна поява тексту ---
    private fun animateTextLetterByLetter(
        textView: TextView,
        text: String,
        interval: Long = 100L
    ) {
        val handler = Handler(Looper.getMainLooper())
        var index = 0

        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    textView.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, interval)
                }
            }
        }

        handler.post(runnable)
    }
}
