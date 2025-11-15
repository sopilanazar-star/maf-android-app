package ua.lviv.maf

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logo: ImageView = findViewById(R.id.logoMaf)
        val ball: View = findViewById(R.id.ballView)
        val title: TextView = findViewById(R.id.titleText)

        // --- ПУЛЬСУЮЧИЙ ЛОГОТИП ПО ЦЕНТРУ ---
        val scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 1f, 1.1f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        val scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 1f, 1.1f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        scaleX.start()
        scaleY.start()

        val fullText = "Миколаївська асоціація футболу"

        // Чекаємо, поки порахується ширина контейнера
        ball.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                ball.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val parent = ball.parent as View
                val parentWidth = parent.width.toFloat()

                val startX = -ball.width.toFloat()
                val endX = parentWidth + ball.width.toFloat()

                ball.translationX = startX

                val duration = 3000L

                // Анімація м'яча
                val ballAnim = ObjectAnimator.ofFloat(ball, View.TRANSLATION_X, startX, endX).apply {
                    this.duration = duration
                    interpolator = AccelerateDecelerateInterpolator()
                }

                // Текст «друкарською машинкою»
                title.text = ""
                title.alpha = 1f
                val charDelay = duration / fullText.length.coerceAtLeast(1)

                fullText.forEachIndexed { index, _ ->
                    handler.postDelayed({
                        title.text = fullText.substring(0, index + 1)
                    }, index * charDelay)
                }

                ballAnim.start()

                // Після інтро — в MainActivity
                handler.postDelayed({
                    startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                    finish()
                }, duration + 500)
            }
        })
    }
}
