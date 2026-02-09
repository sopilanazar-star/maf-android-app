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

    private lateinit var contentFrame: FrameLayout
    private lateinit var headerTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        // 1. Створюємо фон-градієнт прямо в коді
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )

        val root = RelativeLayout(this).apply {
            background = gradient
        }

        // 2. ВЕРХНЯ ПАНЕЛЬ (HEADER)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 220)
            setPadding(40, 90, 40, 20)
        }

        // Спінер турнірів (Зліва)
        val spinner = Spinner(this).apply {
            val items = arrayOf("Вища ліга", "Перша ліга", "Кубок", "Футзал")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
            layoutParams = RelativeLayout.LayoutParams(450, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Кнопка профілю (Справа)
        val authBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(110, 110).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { Toast.makeText(context, "Вхід...", Toast.LENGTH_SHORT).show() }
        }

        header.addView(spinner)
        header.addView(authBtn)

        // 3. НИЖНЯ НАВІГАЦІЯ
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            try {
                inflateMenu(R.menu.bottom_nav_menu)
            } catch (e: Exception) {
                // Захист від вильоту, якщо меню не знайдено
            }
            
            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        // 4. КОНТЕНТ (По центру)
        contentFrame = FrameLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(-1, -1).apply {
                addRule(RelativeLayout.BELOW, header.id)
                addRule(RelativeLayout.ABOVE, nav.id)
            }
        }

        root.addView(header)
        root.addView(contentFrame)
        root.addView(nav)

        setContentView(root)

        // Обробка натискань
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_more -> Toast.makeText(this, "Вкладка Більше", Toast.LENGTH_SHORT).show()
                R.id.nav_news -> Toast.makeText(this, "Новини МАФ", Toast.LENGTH_SHORT).show()
                // Додай інші кейси за аналогією
            }
            true
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
