package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
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
        
        // 1. ПОВНИЙ ЕКРАН
        hideSystemUI()

        // 2. СТВОРЮЄМО ГРАДІЄНТ ПРОГРАМНО (щоб не вилітало через відсутній XML)
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )

        val rootLayout = RelativeLayout(this).apply {
            background = gradientDrawable
        }

        // 3. ВЕРХНЯ ПАНЕЛЬ (HEADER)
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 200)
            setPadding(40, 80, 40, 20)
        }

        // Випадаючий список турнірів (Зліва)
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
            layoutParams = RelativeLayout.LayoutParams(100, 100).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
            setOnClickListener { Toast.makeText(context, "Вхід у систему...", Toast.LENGTH_SHORT).show() }
        }

        header.addView(spinner)
        header.addView(authBtn)

        // 4. НИЖНЯ НАВІГАЦІЯ
        val bottomNav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            // Переконайся, що файл res/menu/bottom_nav_menu.xml ІСНУЄ!
            try {
                inflateMenu(R.menu.bottom_nav_menu)
            } catch (e: Exception) {
                // Якщо меню немає, додаток не вилетить, а просто покаже пусту панель
            }
            setBackgroundColor(Color.WHITE)
            
            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        // 5. КОНТЕНТНА ОБЛАСТЬ
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

        // ЛОГІКА ПЕРЕМИКАННЯ
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_more -> showMessage("Більше")
                R.id.nav_home -> showMessage("Новини")
                R.id.nav_tables -> showMessage("Таблиці")
                R.id.nav_matches -> showMessage("Матчі")
            }
            true
        }
    }

    private fun showMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
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
