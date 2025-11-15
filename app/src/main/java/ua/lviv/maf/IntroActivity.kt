package ua.lviv.maf

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val circularText: CircularTextView = findViewById(R.id.circularText)
        val logoMaf: ImageView = findViewById(R.id.logoMaf)

        // Тексти для верхньої і нижньої дуги — як на емблемі
        circularText.setTopText("МИКОЛАЇВСЬКА")
        circularText.setBottomText("АСОЦІАЦІЯ ФУТБОЛУ")

        // Коли герб уже проміряний, задаємо радіус і стартуємо анімацію букв
        logoMaf.post {
            val r = logoMaf.width / 2f + 40f   // відступ від герба
            circularText.setRadiusPx(r)
            circularText.startLetterByLetterAnimation(3000L)
        }

        // Легка пульсація герба (можеш прибрати, якщо не подобається)
        val scaleUpX = ObjectAnimator.ofFloat(logoMaf, "scaleX", 1f, 1.06f)
        val scaleUpY = ObjectAnimator.ofFloat(logoMaf, "scaleY", 1f, 1.06f)
        scaleUpX.duration = 900L
        scaleUpY.duration = 900L
        scaleUpX.repeatMode = ObjectAnimator.REVERSE
        scaleUpY.repeatMode = ObjectAnimator.REVERSE
        scaleUpX.repeatCount = ObjectAnimator.INFINITE
        scaleUpY.repeatCount = ObjectAnimator.INFINITE

        val logoSet = AnimatorSet()
        logoSet.playTogether(scaleUpX, scaleUpY)
        logoSet.start()

        // Через 5 сек переходимо в головний екран
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000L)
    }
}
