package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val ball = findViewById<ImageView>(R.id.ball)
        val titleText = findViewById<TextView>(R.id.titleText)
        val bySopila = findViewById<TextView>(R.id.bySopila)

        // Коли layout уже намалюється - запускаємо анімацію
        ball.post {
            val screenWidth = resources.displayMetrics.widthPixels.toFloat()

            // Стартова позиція м'яча (трохи за екраном зліва)
            ball.translationX = -ball.width.toFloat() * 1.5f

            // Кінець - трохи перед правим краєм
            val endX = screenWidth - ball.width.toFloat() * 0.5f

            // Анімація: рівномірно котиться ~1.5 сек
            ball.animate()
                .translationX(endX)
                .setDuration(1500)
                .withEndAction {
                    // Після зупинки м'яча показуємо текст
                    titleText.animate()
                        .alpha(1f)
                        .setDuration(700)
                        .start()

                    bySopila.animate()
                        .alpha(1f)
                        .setDuration(700)
                        .start()
                }
                .start()
        }

        // Через ~3 секунди відкриваємо головну активність з сайтом
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
