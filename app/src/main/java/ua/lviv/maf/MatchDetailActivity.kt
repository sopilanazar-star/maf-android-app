package ua.lviv.maf

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MatchDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Встановлюємо XML макет, який ми створили раніше
        setContentView(R.layout.activity_match_detail)

        // 1. Кнопка "Назад" у верхньому лівому куті
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Закриває цю сторінку і повертає до списку
        }

        // 2. Отримуємо дані, які передав нам TournamentAdapter
        val team1 = intent.getStringExtra("team1") ?: "Команда 1"
        val team2 = intent.getStringExtra("team2") ?: "Команда 2"
        val logo1 = intent.getStringExtra("logo1")
        val logo2 = intent.getStringExtra("logo2")
        val score = intent.getStringExtra("score") ?: "vs"
        val league = intent.getStringExtra("league") ?: ""
        val stage = intent.getStringExtra("stage") ?: ""
        val date = intent.getStringExtra("date") ?: ""

        // 3. Знаходимо в’юшки (TextView та ImageView) і заповнюємо їх
        findViewById<TextView>(R.id.tvDetailTeam1).text = team1
        findViewById<TextView>(R.id.tvDetailTeam2).text = team2
        findViewById<TextView>(R.id.tvDetailScore).text = score
        findViewById<TextView>(R.id.tvDetailLeague).text = league
        findViewById<TextView>(R.id.tvDetailDateTime).text = date
        
        // Відображаємо етап, якщо він є
        val tvStage: TextView = findViewById(R.id.tvStageName) // Переконайся, що такий ID є в XML
        if (stage.isNotEmpty()) {
            tvStage.text = stage
        }

        // 4. Завантажуємо логотипи через Glide
        val ivLogo1: ImageView = findViewById(R.id.ivDetailLogo1)
        val ivLogo2: ImageView = findViewById(R.id.ivDetailLogo2)

        Glide.with(this).load(logo1).placeholder(R.drawable.ic_league_default).into(ivLogo1)
        Glide.with(this).load(logo2).placeholder(R.drawable.ic_league_default).into(ivLogo2)
    }
}
