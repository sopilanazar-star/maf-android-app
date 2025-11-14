package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var titleView: TextView

    // Повний текст, який друкуємо по букві
    private val fullText = "МИКОЛАЇВСЬКА АСОЦІАЦІЯ ФУТБОЛУ"
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        titleView = findViewById(R.id.titleView)

        // Запускаємо анімацію "друку" тексту
        typeNextChar()

        // Через ~3.5 секунди переходимо на головний екран
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3500)
    }

    private fun typeNextChar() {
        if (index > fullText.length) return

        titleView.text = fullText.substring(0, index)
        index++

        // Швидкість набору: 70 мс на символ
        titleView.postDelayed({ typeNextChar() }, 70)
    }
}
