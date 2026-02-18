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

        // 1. Знаходимо елементи (зв'язуємо код з макетом)
        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)
        val ivPlayerPhoto: ImageView = findViewById(R.id.ivPlayerPhoto)
        val tvName: TextView = findViewById(R.id.tvPlayerName)
        val tvTeam: TextView = findViewById(R.id.tvTeamName)
        val tvPosition: TextView = findViewById(R.id.tvPosition)
        val tvDob: TextView = findViewById(R.id.tvDob)

        // 2. ТЕСТОВІ ДАНІ (Щоб ми побачили картинку прямо зараз)
        val playerName = "Микола Матвієнко"
        val teamName = "ФК 'Темп' Гірське"
        val position = "Захисник"
        val dob = "02.05.1996 (29 років)"
        
        // Посилання на фото (тестові)
        val playerPhotoUrl = "https://ffl.org.ua/static/img/person_no_photo.jpg" // Або інше фото
        val logoUrl = "https://maf.org.ua/wp-content/uploads/2025/11/cropped-logo-maf-2025-1.png" // Лого МАФ (приблизне)

        // 3. Заповнюємо текст
        tvName.text = playerName
        tvTeam.text = teamName
        tvPosition.text = position
        tvDob.text = dob

        // 4. Вантажимо КАРТИНКИ через твій Glide
        
        // а) Фото гравця (робимо круглим)
        Glide.with(this)
            .load(playerPhotoUrl)
            .circleCrop() 
            .placeholder(android.R.drawable.ic_menu_camera)
            .into(ivPlayerPhoto)

        // б) Водяний знак (Логотип асоціації на фон)
        Glide.with(this)
            .load(logoUrl)
            .into(ivWatermark)

        // 5. Налаштовуємо вкладки
        setupTabs()
    }

    private fun setupTabs() {
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        // Підключаємо адаптер вкладок
        val adapter = PlayerTabsAdapter(this)
        viewPager.adapter = adapter

        // Підписуємо вкладки
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "СТАТИСТИКА"
                1 -> tab.text = "МАТЧІ"
            }
        }.attach()
    }
}

// Простий клас, щоб вкладки перемикалися (навіть якщо вони поки пусті)
class PlayerTabsAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return Fragment() // Повертає пустий білий екран для вкладок
    }
}
