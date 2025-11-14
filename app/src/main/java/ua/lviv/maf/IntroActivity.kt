package ua.lviv.maf

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val ball = findViewById<ImageView>(R.id.ballView)

        // стартова позиція – м'яч трохи за краєм зліва
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        ball.translationX = -screenWidth / 2f
        ball.rotation = -20f

        // анімація руху
        val move = ObjectAnimator.ofFloat(
            ball,
            "translationX",
            ball.translationX,
            0f
        ).apply {
            duration = 1500          // ~1.5 c
            interpolator = AccelerateDecelerateInterpolator()
        }

        // анімація обертання
        val rotate = ObjectAnimator.ofFloat(
            ball,
            "rotation",
            -20f,
            720f                        // 2 повних обороти
        ).apply {
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
        }

        // після закінчення руху – відкриваємо основний екран
        move.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startMain()
            }
        })

        move.start()
        rotate.start()
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
