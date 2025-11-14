package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val fullTitle = "МИКОЛАЇВСЬКА АСОЦІАЦІЯ ФУТБОЛУ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logoView = findViewById<ImageView>(R.id.logoView)
        val titleView = findViewById<TextView>(R.id.titleView)

        // Стартові значення логотипа (трохи менший і прозорий)
        logoView.scaleX = 0.7f
        logoView.scaleY = 0.7f
        logoView.alpha = 0f

        // Плавне появлення + збільшення герба
        logoView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                startTyping(titleView)
            }
            .start()
    }

    private fun startTyping(titleView: TextView) {
        titleView.text = ""
        val delayPerChar = 70L   // мс на одну букву

        // Друкуємо фразу по буквах
        for (i in fullTitle.indices) {
            handler.postDelayed({
                titleView.text = fullTitle.substring(0, i + 1)
            }, delayPerChar * i)
        }

        // Після завершення друку – переходимо в основний екран
        val totalDuration = delayPerChar * fullTitle.length + 600L
        handler.postDelayed({
            openMain()
        }, totalDuration)
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
