package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val ball = findViewById<ImageView>(R.id.ball)
        val titleText = findViewById<TextView>(R.id.titleText)

        ball.post {
            val screenWidth = resources.displayMetrics.widthPixels.toFloat()
            ball.translationX = -ball.width * 2f
            val stopX = screenWidth - ball.width * 1.4f

            ball.animate()
                .translationX(stopX)
                .rotationBy(1080f)
                .setDuration(1500)
                .setInterpolator(LinearInterpolator())
                .withEndAction {
                    titleText.animate()
                        .alpha(1f)
                        .setDuration(700)
                        .start()
                }
                .start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
