package ua.lviv.maf

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PlayerDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_detail)

        // 1. Отримуємо дані з Intent
        val playerName = intent.getStringExtra("name") ?: "Невідомий гравець"
        val teamName = intent.getStringExtra("team_name") ?: ""
        val position = intent.getStringExtra("position") ?: "Гравець"
        val photoUrl = intent.getStringExtra("photo") ?: ""
        val birthDate = intent.getStringExtra("birth_date") ?: ""
        val teamLogo = intent.getStringExtra("team_logo") ?: ""
        
        // ID гравця для запитів статистики
        val playerId = intent.getStringExtra("player_id") ?: "0"

        // 2. Заповнюємо Шапку (Header)
        findViewById<TextView>(R.id.tvPlayerName).text = playerName
        findViewById<TextView>(R.id.tvTeamName).text = teamName
        findViewById<TextView>(R.id.tvPosition).text = position
        findViewById<TextView>(R.id.tvBirthDate).text = birthDate
        
        val ivPhoto = findViewById<ImageView>(R.id.ivPlayerPhoto)
        val ivTeamLogo = findViewById<ImageView>(R.id.ivTeamLogoSmall)
        
        Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_player_placeholder).into(ivPhoto)
        Glide.with(this).load(teamLogo).into(ivTeamLogo)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // 3. Налаштовуємо Tabs та ViewPager
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = PlayerPagerAdapter(this, playerId, position)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, index ->
            tab.text = when (index) {
                0 -> "СТАТИСТИКА"
                1 -> "МАТЧІ"
                else -> ""
            }
        }.attach()
    }

    // Адаптер для перемикання вкладок
    class PlayerPagerAdapter(
        fa: FragmentActivity, 
        private val playerId: String, 
        private val position: String
    ) : FragmentStateAdapter(fa) {
        
        override fun getItemCount(): Int = 2

        override fun createFragment(index: Int): Fragment {
            return when (index) {
                0 -> PlayerStatsFragment.newInstance(playerId, position) // Передаємо позицію для логіки (Воротар/Гравець)
                1 -> PlayerMatchesFragment.newInstance(playerId)
                else -> Fragment()
            }
        }
    }
}
