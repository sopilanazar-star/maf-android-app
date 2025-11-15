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

        // Написи, як на емблемі
        circularText.setTopText("МИКОЛАЇВСЬКА")
        circularText.setBottomText("АСОЦІАЦІЯ ФУТБОЛУ")

        // Коли розміри логотипа відомі – підганяємо радіус кола і запускаємо текст
        logoMaf.post {
            val r = logoMaf.width / 2f + 60f   // відступ дуги від герба
            circularText.setRadiusPx(r)
            circularText.startLetterByLetterAnimation(5000L)
        }

        // === 3D-ефект для герба ===

        // Віддаляємо "камеру", щоб 3D-поворот виглядав природно
        logoMaf.cameraDistance = 8000f * resources.displayMetrics.density

        // Легке похитування по осі Y (псевдо-3D)
        val rotateY = ObjectAnimator.ofFloat(logoMaf, "rotationY", -10f, 10f).apply {
            duration = 2200L
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }

        // Легке "дихання" масштабу
        val scaleX = ObjectAnimator.ofFloat(logoMaf, "scaleX", 1f, 1.06f).apply {
            duration = 900L
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        val scaleY = ObjectAnimator.ofFloat(logoMaf, "scaleY", 1f, 1.06f).apply {
            duration = 900L
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }

        val logoSet = AnimatorSet()
        logoSet.playTogether(rotateY, scaleX, scaleY)
        logoSet.start()

        // Через 5 секунд переходимо в MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000L)
    }
}
