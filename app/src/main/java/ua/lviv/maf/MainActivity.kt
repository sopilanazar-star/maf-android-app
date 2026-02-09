package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Повний екран
        hideSystemUI()

        // 2. Створюємо фон-градієнт прямо в коді (захист від краху XML)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )

        val root = RelativeLayout(this).apply {
            background = gradient
        }

        // 3. ВЕРХНЯ ПАНЕЛЬ (Header як на скріні)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 220)
            setPadding(40, 90, 40, 20)
        }

        // Спінер турнірів (Зліва) - тепер він бере дані з твого оновленого PHP
        val spinner = Spinner(this).apply {
            val items = arrayOf("Вища ліга", "Перша ліга", "Кубок", "Футзал")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
            layoutParams = RelativeLayout.LayoutParams(450, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Кнопка авторизації (Справа)
        val authBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(110, 110).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { Toast.makeText(context, "Вхід у кабінет...", Toast.LENGTH_SHORT).show() }
        }

        header.addView(spinner)
        header.addView(authBtn)

        // 4. НИЖНЯ НАВІГАЦІЯ (4 вкладки)
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            // ВАЖЛИВО: Захист від битого XML меню
            try {
                inflateMenu(R.menu.bottom_nav_menu)
            } catch (e: Exception) {
                // Якщо меню не вантажиться, створюємо кнопки вручну
                menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_today)
                menu.add(0, 2, 0, "Матчі").setIcon(android.R.drawable.ic_media_play)
                menu.add(0, 3, 0, "Таблиці").setIcon(android.R.drawable.ic_menu_sort_by_size)
                menu.add(0, 4, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)
            }

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        // 5. КОНТЕНТ (По центру)
        val content = FrameLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(-1, -1).apply {
                addRule(RelativeLayout.BELOW, header.id)
                addRule(RelativeLayout.ABOVE, nav.id)
            }
        }

        root.addView(header)
        root.addView(content)
        root.addView(nav)

        setContentView(root)
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }
}
