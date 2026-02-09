package ua.lviv.maf

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var contentFrame: FrameLayout
    private lateinit var tournamentSpinner: Spinner
    private lateinit var profileBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        // Головний контейнер з градієнтом
        val rootLayout = RelativeLayout(this).apply {
            background = getDrawable(R.drawable.bg_main_gradient)
        }

        // --- ВЕРХНЯ ПАНЕЛЬ (HEADER) ---
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 180)
            setPadding(40, 60, 40, 0)
        }

        // Випадаючий список (Ліворуч)
        tournamentSpinner = Spinner(this).apply {
            val tournaments = arrayOf("Вища Ліга", "Перша Ліга", "Кубок", "Футзал")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, tournaments)
            layoutParams = RelativeLayout.LayoutParams(400, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Авторизація (Праворуч)
        profileBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view) // Зміниш на свою іконку профілю
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(100, 100).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { Toast.makeText(context, "Вхід...", Toast.LENGTH_SHORT).show() }
        }

        header.addView(tournamentSpinner)
        header.addView(profileBtn)

        // --- НИЖНЯ НАВІГАЦІЯ ---
        val bottomNav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            itemIconTintList = getColorStateList(R.color.nav_item_color) // Створимо пізніше
            itemTextColor = getColorStateList(R.color.nav_item_color)
            
            val params = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
            layoutParams = params
        }

        // --- КОНТЕНТ ---
        contentFrame = FrameLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(-1, -1).apply {
                addRule(RelativeLayout.BELOW, header.id)
                addRule(RelativeLayout.ABOVE, bottomNav.id)
            }
        }

        rootLayout.addView(header)
        rootLayout.addView(contentFrame)
        rootLayout.addView(bottomNav)

        setContentView(rootLayout)

        // Логіка кліків
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_news -> showNews()
                R.id.nav_matches -> showMatches()
                R.id.nav_tables -> showTables()
                R.id.nav_more -> showMoreMenu()
            }
            true
        }
    }

    private fun showNews() { /* Завантаження новин з WP */ }
    private fun showMatches() { /* Завантаження матчів */ }
    private fun showTables() { /* Завантаження таблиць */ }
    
    private fun showMoreMenu() {
        // Тут ми виведемо нашу сітку (Grid) з банами та історією
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}
