package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MatchDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)

        // Кнопка Назад
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // --- 1. ОТРИМАННЯ ДАНИХ З INTENT ---
        val matchId = intent.getStringExtra("id") ?: ""
        
        // Отримуємо як текст (універсально)
        var homeIdTemp = intent.getStringExtra("home_team_id") ?: "0"
        if (homeIdTemp == "0" || homeIdTemp.isEmpty()) {
            homeIdTemp = intent.getIntExtra("home_team_id", 0).toString()
        }
        
        var awayIdTemp = intent.getStringExtra("away_team_id") ?: "0"
        if (awayIdTemp == "0" || awayIdTemp.isEmpty()) {
            awayIdTemp = intent.getIntExtra("away_team_id", 0).toString()
        }

        val homeTeamIdStr = homeIdTemp
        val awayTeamIdStr = awayIdTemp

        val team1Name = intent.getStringExtra("team1") ?: "Команда 1"
        val team2Name = intent.getStringExtra("team2") ?: "Команда 2"
        val logo1 = intent.getStringExtra("logo1")
        val logo2 = intent.getStringExtra("logo2")
        val score = intent.getStringExtra("score") ?: ""
        val league = intent.getStringExtra("league") ?: ""
        val stage = intent.getStringExtra("stage") ?: ""
        val stadium = intent.getStringExtra("stadium") ?: ""
        val referee = intent.getStringExtra("referee") ?: ""
        val fullDate = intent.getStringExtra("date") ?: ""
// --- ДОДАНО: Технічні дані ---
        val isTechnical = intent.getBooleanExtra("is_technical", false)
        val technicalReason = intent.getStringExtra("technical_reason") ?: ""
        // --- 2. ПРИВ'ЯЗКА VIEW ---
        val tvLeague: TextView = findViewById(R.id.tvDetailLeague)
        val tvStage: TextView = findViewById(R.id.tvStageName)
        val tvStadium: TextView = findViewById(R.id.tvDetailStadium)
        val tvDateTime: TextView = findViewById(R.id.tvDetailDateTime)
        val tvReferee: TextView = findViewById(R.id.tvDetailReferee)

        val tvTeam1: TextView = findViewById(R.id.tvDetailTeam1)
        val ivLogo1: ImageView = findViewById(R.id.ivDetailLogo1)
        val tvTeam2: TextView = findViewById(R.id.tvDetailTeam2)
        val ivLogo2: ImageView = findViewById(R.id.ivDetailLogo2)

        val tvScore: TextView = findViewById(R.id.tvDetailScore)
        val tvScorers: TextView = findViewById(R.id.tvScorers)
        val tvReason: TextView = findViewById(R.id.tvDetailReason)
        val tvAdditionalScore: TextView = findViewById(R.id.tvAdditionalScore)

        // --- 3. ЗАПОВНЕННЯ ДАНИМИ ---
        tvTeam1.text = team1Name
        tvTeam2.text = team2Name
        tvLeague.text = league.uppercase()

        if (stage.isNotEmpty()) {
            tvStage.text = stage
            tvStage.visibility = View.VISIBLE
        } else {
            tvStage.visibility = View.GONE
        }

        if (stadium.isNotEmpty()) {
            tvStadium.text = stadium
            tvStadium.visibility = View.VISIBLE
        } else {
            tvStadium.visibility = View.GONE
        }

        tvDateTime.text = fullDate

        if (score.contains(" : ") || score.contains("-")) {
            tvScore.text = score
            tvScore.textSize = 38f

            // --- ДОДАЙ ЦЕЙ БЛОК ДЛЯ ПЕНАЛЬТІ ---
            val penScore = intent.getStringExtra("pen_score") ?: ""
            if (penScore.isNotEmpty()) {
                tvAdditionalScore.text = "($penScore)"
                tvAdditionalScore.visibility = View.VISIBLE
            } else {
                tvAdditionalScore.visibility = View.GONE
            }
            // ----------------------------------

            // --- ДОДАНО: Логіка кольору та причини ---
            if (isTechnical) {
                tvScore.setTextColor(Color.parseColor("#E30613")) // Червоний

                if (technicalReason.isNotEmpty()) {
                    tvReason.visibility = View.VISIBLE
                    tvReason.text = technicalReason
                } else {
                    tvReason.visibility = View.GONE
                }
            } else {
                tvScore.setTextColor(Color.WHITE) // Звичайний білий
                tvReason.visibility = View.GONE
            }
            // ------------------------------------------

        } else {
            tvScore.text = "VS"
            tvScore.textSize = 32f
            tvScore.setTextColor(Color.parseColor("#BCBCBC"))
            tvReason.visibility = View.GONE
        }

        if (referee.isNotEmpty()) {
            tvReferee.text = "Арбітр: $referee"
            tvReferee.visibility = View.VISIBLE
        } else {
            tvReferee.visibility = View.GONE
        }
        
        tvScorers.visibility = View.GONE 

        Glide.with(this).load(logo1?.replace("http://", "https://")).into(ivLogo1)
        Glide.with(this).load(logo2?.replace("http://", "https://")).into(ivLogo2)

        // --- 4. НАВІГАЦІЯ (Виправлено: передаємо String-ID) ---
        fun openTeamDetails(id: String, name: String, logo: String?) {
            // Перевіряємо рядок на порожнечу або "0"
            if (id != "0" && id.isNotEmpty()) {
                try {
                    val intent = Intent(this, TeamPlayersActivity::class.java)
                    // ПЕРЕДАЄМО ЯК ТЕКСТ (String), як це робить турнірна таблиця
                    intent.putExtra("team_id", id)
                    intent.putExtra("team_name", name)
                    intent.putExtra("team_logo", logo)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Помилка: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(this, "Дані команди відсутні", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Викликаємо функцію, передаючи String-змінні
        val homeClickListener = View.OnClickListener {
            openTeamDetails(homeTeamIdStr, team1Name, logo1)
        }
        tvTeam1.setOnClickListener(homeClickListener)
        ivLogo1.setOnClickListener(homeClickListener)

        val awayClickListener = View.OnClickListener {
            openTeamDetails(awayTeamIdStr, team2Name, logo2)
        }
        tvTeam2.setOnClickListener(awayClickListener)
        ivLogo2.setOnClickListener(awayClickListener)

        // --- 5. ТАБИ ---
        val tabs: TabLayout = findViewById(R.id.detailTabs)
        val viewPager: ViewPager2 = findViewById(R.id.detailViewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> TimelineFragment.newInstance(matchId, homeTeamIdStr) 
                    else -> LineupsFragment.newInstance(matchId)
                }
            }
        }

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = if (position == 0) "Timeline" else "Склад"
        }.attach()
    }
}
