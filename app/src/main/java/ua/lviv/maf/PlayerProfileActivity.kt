package ua.lviv.maf

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PlayerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // 1. Знаходимо елементи
        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)
        val ivPlayerPhoto: ImageView = findViewById(R.id.ivPlayerPhoto)
        val tvName: TextView = findViewById(R.id.tvPlayerName)
        val tvTeam: TextView = findViewById(R.id.tvTeamName)
        val tvPosition: TextView = findViewById(R.id.tvPosition)
        val tvDob: TextView = findViewById(R.id.tvDob)

        // 2. ОТРИМУЄМО РЕАЛЬНІ ДАНІ (з Intent)
        // Якщо даних немає, буде "Невідомий гравець"
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Невідомий гравець"
        val playerPhotoUrl = intent.getStringExtra("PLAYER_PHOTO")
        val playerNumber = intent.getStringExtra("PLAYER_NUMBER") ?: ""
        val position = intent.getStringExtra("PLAYER_POSITION") ?: ""
        
        // Назву команди ми поки передаємо як "Гравець клубу" (або можна передати реальну)
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Гравець клубу"

        // Лого асоціації (завжди однакове)
        val logoUrl = "https://maf.org.ua/wp-content/uploads/2025/11/cropped-logo-maf-2025-1.png"

        // 3. Заповнюємо текст
        tvName.text = playerName
        tvTeam.text = teamName
        
        // Формуємо рядок позиції (наприклад: "Захисник • #12")
        val positionText = if (playerNumber.isNotEmpty()) "$position • #$playerNumber" else position
        tvPosition.text = positionText
        
        // Дату народження поки сховаємо, бо API її не передає у списку гравців
        tvDob.text = "" 

        // 4. Вантажимо КАРТИНКИ
        
        // а) Фото гравця
        if (!playerPhotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(playerPhotoUrl)
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_camera)
                .error(android.R.drawable.ic_menu_camera)
                .into(ivPlayerPhoto)
        } else {
            // Якщо фото немає, ставимо заглушку
            ivPlayerPhoto.setImageResource(android.R.drawable.ic_menu_camera)
        }

        // б) Водяний знак
        Glide.with(this)
            .load(logoUrl)
            .into(ivWatermark)

        // 5. Налаштовуємо вкладки
        setupTabs()
    }

    private fun setupTabs() {
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        val adapter = PlayerTabsAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "СТАТИСТИКА"
                1 -> tab.text = "МАТЧІ"
            }
        }.attach()
    }
}

class PlayerTabsAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        // Поки що повертаємо пусті екрани
        return Fragment() 
    }
}
