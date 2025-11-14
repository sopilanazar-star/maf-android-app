package ua.lviv.maf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Зв'язуємося з елементами, щоб переконатися що layout підтягується
        val ballView: ImageView = findViewById(R.id.ballView)
        val titleView: TextView = findViewById(R.id.titleView)
        val byView: TextView = findViewById(R.id.byView)

        // Через 2 секунди відкриваємо головну активність
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
