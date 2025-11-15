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
        val ball: ImageView = findViewById(R.id.ballView)
        val title: TextView = findViewById(R.id.titleText)

        // --- ПУЛЬСАЦІЯ ЛОГОТИПУ ---
        val scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 1f, 1.1f)
        val scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 1f, 1.1f)

        scaleX.duration = 800
        scaleY.duration = 800
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatMode = ValueAnimator.REVERSE
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE

        scaleX.start()
        scaleY.start()

        // Текст, який буде показуватись за м'ячем
        val fullText = "Миколаївська асоціація футболу"

        // --- АНІМАЦІЯ М'ЯЧА + НАПИСУ ---
        // Чекаємо, поки розміри layout будуть відомі
        ball.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                ball.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val parent = ball.parent as View
                val parentWidth = parent.width.toFloat()
                val startX = -ball.width.toFloat()
                val endX = parentWidth + ball.width.toFloat()

                // Початкове положення м'яча — зліва за екраном
                ball.translationX = startX

                val duration = 3000L

                // Анімація котіння м'яча
                val ballAnim = ObjectAnimator.ofFloat(ball, View.TRANSLATION_X, startX, endX).apply {
                    this.duration = duration
                    interpolator = AccelerateDecelerateInterpolator()
                }

                // Анімація тексту "друкарською машинкою"
                title.text = ""
                title.alpha = 1f

                val charDelay = duration / fullText.length.coerceAtLeast(1)

                fullText.forEachIndexed { index, _ ->
                    handler.postDelayed({
                        title.text = fullText.substring(0, index + 1)
                    }, index * charDelay)
                }

                // Запускаємо анімацію м'яча
                ballAnim.start()

                // Після завершення інтро — переходимо в MainActivity
                handler.postDelayed({
                    startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                    finish()
                }, duration + 500)
            }
        })
    }
}
