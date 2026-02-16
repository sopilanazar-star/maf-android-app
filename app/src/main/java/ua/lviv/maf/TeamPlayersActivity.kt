package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TeamPlayersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_players) // Переконайся, що XML файл існує (код нижче)

        val teamIdInt = intent.getIntExtra("team_id", 0)
        val teamId = if (teamIdInt != 0) teamIdInt.toString() else intent.getStringExtra("team_id") ?: ""
        val teamName = intent.getStringExtra("team_name") ?: "Команда"

        // Налаштування заголовка
        findViewById<TextView>(R.id.tvTeamName).text = teamName
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Підключаємо адаптер вкладок
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> TeamSquadFragment.newInstance(teamId) // Вкладка Склад
                    else -> TeamMatchesFragment.newInstance(teamId) // Вкладка Матчі
                }
            }
        }

        // Назви вкладок
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "СКЛАД" else "МАТЧІ"
        }.attach()
    }
}
