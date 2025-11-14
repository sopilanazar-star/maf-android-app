package ua.lviv.maf

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        startBallAnimation()
    }

    private fun startBallAnimation() {
        val ball = findViewById<ImageView>(R.id.ballView)

        // стартуємо трохи за лівим краєм
        ball.translationX = -300f

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        // рух м’яча зліва → вправо
        val moveAnim = ObjectAnimator.ofFloat(
            ball,
            "translationX",
            -300f,
            screenWidth + 300f
        ).apply {
            duration = 1500      // ~1,5 сек
            interpolator = LinearInterpolator()
        }

        // обертання м’яча
        val rotateAnim = ObjectAnimator.ofFloat(
            ball,
            "rotation",
            0f,
            1440f           // 4 повних оберти
        ).apply {
            duration = 1500
            interpolator = LinearInterpolator()
        }

        // запускаємо рух + обертання одночасно
        AnimatorSet().apply {
            playTogether(moveAnim, rotateAnim)
            start()
        }

        // після інтро відкриваємо основний екран
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1600)
    }
}
