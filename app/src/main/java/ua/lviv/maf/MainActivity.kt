package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var contentFrame: FrameLayout
    private lateinit var headerTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. ПОВНИЙ ЕКРАН (Прибираємо системні панелі)
        hideSystemUI()

        // 2. СТВОРЮЄМО ГРАДІЄНТ ПРОГРАМНО
        // Навіть якщо bg_main_gradient.xml зламаний, цей код спрацює
        val mainGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )

        val rootLayout = RelativeLayout(this).apply {
            background = mainGradient
        }

        // 3. ВЕРХНЯ ПАНЕЛЬ (HEADER)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 200)
            setPadding(40, 80, 40, 20)
        }

        // Випадаючий список турнірів (Зліва)
        val tournamentSpinner = Spinner(this).apply {
            val items = arrayOf("Вища ліга", "Перша ліга", "Кубок", "Футзал")
            val spinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
            adapter = spinnerAdapter
            
            layoutParams = RelativeLayout.LayoutParams(450, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Кнопка авторизації (Справа)
        val authBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view) // Стандартна іконка профілю
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(110, 110).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { Toast.makeText(context, "Вхід у кабінет...", Toast.LENGTH_SHORT).show() }
        }

        header.addView(tournamentSpinner)
        header.addView(authBtn)

        // 4. НИЖНЯ НАВІГАЦІЯ
        val bottomNav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            // Спроба завантажити меню (якщо файл цілий)
            try {
                inflateMenu(R.menu.bottom_nav_menu)
            } catch (e: Exception) {
                // Якщо меню поламане, додаємо пункти вручну, щоб не було краху
                menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_today)
                menu.add(0, 2, 0, "Матчі").setIcon(android.R.drawable.ic_media_play)
                menu.add(0, 3, 0, "Таблиці").setIcon(android.R.drawable.ic_menu_sort_by_size)
                menu.add(0, 4, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)
            }

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        // 5. КОНТЕНТНА ОБЛАСТЬ (Займає весь простір між хедером і підвалом)
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

        // ЛОГІКА КЛІКІВ МЕНЮ
        bottomNav.setOnItemSelectedListener { item ->
            when (item.title) {
                "Новини" -> Toast.makeText(this, "Завантаження новин...", Toast.LENGTH_SHORT).show()
                "Матчі" -> Toast.makeText(this, "Сьогоднішні матчі", Toast.LENGTH_SHORT).show()
                "Більше" -> showMoreMenu()
            }
            true
        }
    }

    private fun showMoreMenu() {
        // Тут ми пізніше виведемо твою сітку плиток
        Toast.makeText(this, "Меню Більше відкрито", Toast.LENGTH_SHORT).show()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN 
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
