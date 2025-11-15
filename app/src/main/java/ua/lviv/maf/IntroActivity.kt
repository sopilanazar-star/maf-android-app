package ua.lviv.maf

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val ball = findViewById<ImageView>(R.id.ballView)
        val text = findViewById<TextView>(R.id.mafText)
        val logo = findViewById<ImageView>(R.id.centerLogo)

        // ---------- 1️⃣ М'яч котиться ----------

        val move = ObjectAnimator.ofFloat(ball, "translationX", 0f, 900f).apply {
            duration = 2000
        }

        val rotate = ObjectAnimator.ofFloat(ball, "rotation", 0f, 1440f).apply {
            duration = 2000
        }

        val scaleX = ObjectAnimator.ofFloat(ball, "scaleX", 1f, 1.5f).apply { duration = 2000 }
        val scaleY = ObjectAnimator.ofFloat(ball, "scaleY", 1f, 1.5f).apply { duration = 2000 }

        val ballAnim = AnimatorSet().apply {
            playTogether(move, rotate, scaleX, scaleY)
            start()
        }

        // ---------- 2️⃣ Поява герба ----------

        val fadeLogo = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).apply {
            duration = 1200
            startDelay = 1800
        }
        fadeLogo.start()

        // ---------- 3️⃣ Поява тексту ----------

        val fadeText = ObjectAnimator.ofFloat(text, "alpha", 0f, 1f).apply {
            duration = 1200
            startDelay = 2300
        }
        fadeText.start()

        // ---------- 4️⃣ Перехід у основний додаток ----------

        text.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3600)
    }
}
