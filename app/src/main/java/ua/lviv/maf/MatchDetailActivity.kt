package ua.lviv.maf

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

        // 1. Кнопка "Назад"
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // 2. Отримуємо дані з Intent
        val matchId = intent.getStringExtra("id") ?: ""
        val team1 = intent.getStringExtra("team1") ?: "Команда 1"
        val team2 = intent.getStringExtra("team2") ?: "Команда 2"
        val logo1 = intent.getStringExtra("logo1")
        val logo2 = intent.getStringExtra("logo2")
        val score = intent.getStringExtra("score") ?: "vs"
        val league = intent.getStringExtra("league") ?: ""
        val stage = intent.getStringExtra("stage") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val stadium = intent.getStringExtra("stadium") ?: ""
        val referee = intent.getStringExtra("referee") ?: ""

        // 3. Заповнюємо Header
        findViewById<TextView>(R.id.tvDetailTeam1).text = team1
        findViewById<TextView>(R.id.tvDetailTeam2).text = team2
        findViewById<TextView>(R.id.tvDetailScore).text = score
        findViewById<TextView>(R.id.tvDetailLeague).text = league
        findViewById<TextView>(R.id.tvDetailDateTime).text = date
        
        // Етап (Тур)
        val tvStage: TextView = findViewById(R.id.tvStageName)
        tvStage.text = stage
        tvStage.visibility = if (stage.isNotEmpty()) View.VISIBLE else View.GONE

        // Стадіон
        val tvStadium: TextView = findViewById(R.id.tvDetailStadium)
        tvStadium.text = stadium
        tvStadium.visibility = if (stadium.isNotEmpty()) View.VISIBLE else View.GONE

        // Арбітр
        val tvReferee: TextView = findViewById(R.id.tvDetailReferee)
        if (referee.isNotEmpty()) {
            tvReferee.text = "Арбітр: $referee"
            tvReferee.visibility = View.VISIBLE
        } else {
            tvReferee.visibility = View.GONE
        }

        // Завантаження логотипів
        val ivLogo1: ImageView = findViewById(R.id.ivDetailLogo1)
        val ivLogo2: ImageView = findViewById(R.id.ivDetailLogo2)

        Glide.with(this).load(logo1).placeholder(android.R.drawable.ic_menu_gallery).into(ivLogo1)
        Glide.with(this).load(logo2).placeholder(android.R.drawable.ic_menu_gallery).into(ivLogo2)

        // 4. НАЛАШТУВАННЯ ТАБІВ (Timeline та Склад)
        val tabs: TabLayout = findViewById(R.id.detailTabs)
        val viewPager: ViewPager2 = findViewById(R.id.detailViewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    // Передаємо matchId у фрагменти, щоб вони знали, чиї дані качати
                    0 -> TimelineFragment.newInstance(matchId) 
                    else -> LineupsFragment.newInstance(matchId)
                }
            }
        }

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = if (position == 0) "Timeline" else "Склад"
        }.attach()
    }
}
