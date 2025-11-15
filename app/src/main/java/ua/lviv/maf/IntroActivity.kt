package ua.lviv.maf

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        // важливо: щоб не мигав білий фон
        setTheme(R.style.Theme_MAFFootball)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logo: ImageView = findViewById(R.id.logoMaf)
        val dot1: View = findViewById(R.id.dot1)
        val dot2: View = findViewById(R.id.dot2)
        val dot3: View = findViewById(R.id.dot3)

        startLogoAnimation(logo)
        startDotsAnimation(dot1, dot2, dot3)

        // 5 секунд інтро, потім перехід у MainActivity
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            // прибираємо стандартну анімацію, щоб не було білого миготіння
            overridePendingTransition(0, 0)
            finish()
        }, 5000L)
    }

    private fun startLogoAnimation(logo: ImageView) {
        // пульсація + легке похитування (ефект 3D/живого логотипа)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.08f)
        val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, -3f, 3f)

        ObjectAnimator.ofPropertyValuesHolder(logo, scaleX, scaleY, rotation).apply {
            duration = 900
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun startDotsAnimation(dot1: View, dot2: View, dot3: View) {
        val dots = listOf(dot1, dot2, dot3)

        dots.forEachIndexed { index, view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1f).apply {
                duration = 500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                startDelay = index * 150L   // хвиля зліва направо
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }
}
