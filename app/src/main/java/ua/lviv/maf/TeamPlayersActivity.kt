package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
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

        // Отримуємо ID та назву
        val idInt = intent.getIntExtra("team_id", 0)
        val teamId = if (idInt != 0) idInt.toString() else intent.getStringExtra("team_id") ?: ""
        val teamName = intent.getStringExtra("team_name") ?: "Команда"

        // --- БУДУЄМО UI КОДОМ (Щоб не створювати XML) ---
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1D23"))
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        // Верхня панель з кнопкою назад і назвою
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 40, 20, 20)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val btnBack = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert) // Або R.drawable.ic_back
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }

        val title = TextView(this).apply {
            text = teamName
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(30, 0, 0, 0)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        header.addView(btnBack)
        header.addView(title)

        // Таби (Вкладки)
        val tabLayout = TabLayout(this).apply {
            setBackgroundColor(Color.parseColor("#1A1D23"))
            setTabTextColors(Color.GRAY, Color.parseColor("#00E676"))
            setSelectedTabIndicatorColor(Color.parseColor("#00E676"))
        }

        // ViewPager (горталка сторінок)
        val viewPager = ViewPager2(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        root.addView(header)
        root.addView(tabLayout)
        root.addView(viewPager)
        setContentView(root)
        // --------------------------------------------------

        // Налаштування адаптера для табів
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> TeamSquadFragment.newInstance(teamId)   // 1. Гравці
                    else -> TeamMatchesFragment.newInstance(teamId) // 2. Матчі
                }
            }
        }

        // Зв'язуємо Таби і Пейджер
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "ГРАВЦІ" else "МАТЧІ"
        }.attach()
    }
}
