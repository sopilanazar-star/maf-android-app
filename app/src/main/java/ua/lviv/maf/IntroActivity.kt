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
    private var introFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Щоб не було білого миготіння перед інтро
        setTheme(R.style.Theme_MAFFootball)

        // Fullscreen + ховаємо статусбар і навігацію
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logo: ImageView = findViewById(R.id.logo)
        val dot1: View = findViewById(R.id.dot1)
        val dot2: View = findViewById(R.id.dot2)
        val dot3: View = findViewById(R.id.dot3)

        startLogoAnimation(logo)
        startDotsAnimation(dot1, dot2, dot3)

        // 5 секунд інтро – потім вхід у додаток без білого екрану
        handler.postDelayed({
            goToMain()
        }, 5000L)
    }

    private fun goToMain() {
        if (introFinished) return
        introFinished = true
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(0, 0)
        finish()
    }

    private fun startLogoAnimation(logo: ImageView) {
        // 3D-ефект: пульсація + легке похитування
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.08f)
        val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, -4f, 4f)

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
                startDelay = index * 150L  // хвиля зліва направо
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Щоб не було витоків і подвійних переходів
        handler.removeCallbacksAndMessages(null)
    }
}
