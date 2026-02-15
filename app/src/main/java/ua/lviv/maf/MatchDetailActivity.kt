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

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // 1. ОТРИМУЄМО ДАНІ
        val matchId = intent.getStringExtra("id") ?: ""
        
        val homeTeamIdStr = intent.getStringExtra("home_team_id") ?: "0"
        val awayTeamIdStr = intent.getStringExtra("away_team_id") ?: "0"
        
        val homeTeamId = homeTeamIdStr.toIntOrNull() ?: 0
        val awayTeamId = awayTeamIdStr.toIntOrNull() ?: 0

        val team1Name = intent.getStringExtra("team1") ?: "Команда 1"
        val team2Name = intent.getStringExtra("team2") ?: "Команда 2"
        val logo1 = intent.getStringExtra("logo1")
        val logo2 = intent.getStringExtra("logo2")
        val score = intent.getStringExtra("score") ?: ""
        val league = intent.getStringExtra("league") ?: ""
        val stage = intent.getStringExtra("stage") ?: ""
        val stadium = intent.getStringExtra("stadium") ?: ""
        val referee = intent.getStringExtra("referee") ?: ""

        // 2. ЗНАХОДИМО VIEW ДЛЯ КОМАНД
        val tvTeam1: TextView = findViewById(R.id.tvDetailTeam1)
        val ivLogo1: ImageView = findViewById(R.id.ivDetailLogo1)
        val tvTeam2: TextView = findViewById(R.id.tvDetailTeam2)
        val ivLogo2: ImageView = findViewById(R.id.ivDetailLogo2)

        // 3. ЗАПОВНЮЄМО ДАНІ
        tvTeam1.text = team1Name
        tvTeam2.text = team2Name
        findViewById<TextView>(R.id.tvDetailLeague).text = league.uppercase()
        
        val tvStage: TextView = findViewById(R.id.tvStageName)
        tvStage.text = stage
        tvStage.visibility = if (stage.isNotEmpty()) View.VISIBLE else View.GONE

        val tvStadium: TextView = findViewById(R.id.tvDetailStadium)
        tvStadium.text = stadium
        tvStadium.visibility = if (stadium.isNotEmpty()) View.VISIBLE else View.GONE

        val tvScore: TextView = findViewById(R.id.tvDetailScore)
        val tvTime: TextView = findViewById(R.id.tvDetailDateTime)

        if (score.contains(" : ")) {
            tvScore.text = score
            tvScore.textSize = 38f
            tvScore.setTextColor(Color.WHITE)
            tvTime.text = intent.getStringExtra("date") ?: ""
        } else {
            tvScore.text = "VS"
            tvScore.textSize = 32f
            tvScore.setTextColor(Color.parseColor("#BCBCBC"))
            tvTime.text = score
        }

        val tvReferee: TextView = findViewById(R.id.tvDetailReferee)
        if (referee.isNotEmpty()) {
            tvReferee.text = "Арбітр: $referee"
            tvReferee.visibility = View.VISIBLE
        } else {
            tvReferee.visibility = View.GONE
        }

        Glide.with(this).load(logo1?.replace("http://", "https://")).into(ivLogo1)
        Glide.with(this).load(logo2?.replace("http://", "https://")).into(ivLogo2)

        // --- 4. НАВІГАЦІЯ ---
        fun openTeamDetails(id: Int, name: String) {
            if (id != 0) {
                try {
                    val intent = Intent(this, TeamPlayersActivity::class.java)
                    intent.putExtra("team_id", id)
                    intent.putExtra("team_name", name)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Помилка: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(this, "ID команди не знайдено", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        val homeClickListener = View.OnClickListener { openTeamDetails(homeTeamId, team1Name) }
        tvTeam1.setOnClickListener(homeClickListener)
        ivLogo1.setOnClickListener(homeClickListener)

        val awayClickListener = View.OnClickListener { openTeamDetails(awayTeamId, team2Name) }
        tvTeam2.setOnClickListener(awayClickListener)
        ivLogo2.setOnClickListener(awayClickListener)

        // 5. ТАБИ (ОСЬ ТУТ ОСНОВНА ПРАВКА)
        val tabs: TabLayout = findViewById(R.id.detailTabs)
        val viewPager: ViewPager2 = findViewById(R.id.detailViewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    // Передаємо homeTeamIdStr у TimelineFragment
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
