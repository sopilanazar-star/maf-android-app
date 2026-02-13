package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TeamPlayersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Поки що просто виведемо назву команди, щоб перевірити, чи працює перехід
        val teamName = intent.getStringExtra("team_name") ?: "Команда"
        
        val textView = TextView(this).apply {
            text = "Гравці команди: $teamName\n(Екран у розробці)"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.parseColor("#1A1D23"))
        }
        
        setContentView(textView)
    }
}
