package ua.lviv.maf

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

        // Текст навколо логотипу
        circularText.setText("Миколаївська асоціація футболу")

        // Запускаємо побуквенну анімацію (3 секунди)
        circularText.startLetterByLetterAnimation(duration = 3000L) {
            // після завершення анімації можна щось ще зробити, якщо треба
        }

        // Через трохи довше ніж анімація – відкриваємо головний екран
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3500L)
    }
}
