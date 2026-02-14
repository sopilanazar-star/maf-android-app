package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TeamPlayersActivity : AppCompatActivity() {

    private var teamId: Int = 0
    private var teamName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Отримуємо дані з Intent (ВАЖЛИВО: team_id тепер Int)
        teamId = intent.getIntExtra("team_id", 0)
        teamName = intent.getStringExtra("team_name") ?: "Команда"

        // 2. Створюємо інтерфейс (поки що програмно, як у тебе)
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val tvTitle = TextView(this).apply {
            text = teamName
            setTextColor(Color.WHITE)
            textSize = 24f
            setPadding(0, 40, 0, 10)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvStatus = TextView(this).apply {
            text = "ID команди: $teamId\nСклад гравців з'явиться тут після підключення API."
            setTextColor(Color.parseColor("#BCBCBC"))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(40, 20, 40, 0)
        }

        rootLayout.addView(tvTitle)
        rootLayout.addView(tvStatus)

        setContentView(rootLayout)

        // Тут ми пізніше викличемо loadPlayers(teamId)
    }

    // Зарезервуємо місце під функцію завантаження
    private fun loadPlayers(id: Int) {
        // Код для OkHttp буде тут
    }
}
