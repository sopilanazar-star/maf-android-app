package ua.lviv.maf

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val circularText: CircularTextView = findViewById(R.id.circularText)
        val logoMaf = findViewById<android.widget.ImageView>(R.id.logoMaf)

        // Текст навколо логотипу
        circularText.setText("Миколаївська асоціація футболу")

        // 1) Анімація появи букв по колу (1.8 сек)
        circularText.startLetterByLetterAnimation(duration = 1800L)

        // 2) "Дихання" логотипу — легка пульсація
        val scaleUpX = ObjectAnimator.ofFloat(logoMaf, "scaleX", 1f, 1.08f)
        val scaleUpY = ObjectAnimator.ofFloat(logoMaf, "scaleY", 1f, 1.08f)

        scaleUpX.duration = 800L
        scaleUpY.duration = 800L

        // Повторюємо анімацію туди-назад безкінечно
        scaleUpX.repeatMode = ObjectAnimator.REVERSE
        scaleUpY.repeatMode = ObjectAnimator.REVERSE
        scaleUpX.repeatCount = ObjectAnimator.INFINITE
        scaleUpY.repeatCount = ObjectAnimator.INFINITE

        val logoSet = AnimatorSet()
        logoSet.playTogether(scaleUpX, scaleUpY)
        logoSet.start()

        // 3) Через 3 секунди переходимо в MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000L)
    }
}
