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
        
        // 1. СТВОРЮЄМО ДИЗАЙН ПОВНІСТЮ В КОДІ (БЕЗ XML)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )

        val root = RelativeLayout(this).apply {
            background = gradient
        }

        // ВЕРХНЯ ПАНЕЛЬ
        val header = RelativeLayout(this).apply {
            id = View.generateViewId()
            layoutParams = RelativeLayout.LayoutParams(-1, 250)
            setPadding(40, 100, 40, 20)
        }

        // Спінер турнірів (Ліворуч)
        val spinner = Spinner(this).apply {
            val items = arrayOf("Вища ліга", "Перша ліга", "Кубок")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, items)
            layoutParams = RelativeLayout.LayoutParams(400, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        // Кнопка авторизації (Праворуч)
        val authBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_view)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = RelativeLayout.LayoutParams(120, 120).apply {
                addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                addRule(RelativeLayout.CENTER_VERTICAL)
            }
        }

        header.addView(spinner)
        header.addView(authBtn)

        // НИЖНЯ НАВІГАЦІЯ (З ПЕРЕВІРКОЮ)
        val nav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            setBackgroundColor(Color.WHITE)
            
            // ВАЖЛИВО: Якщо файл меню битий, додаток не впаде
            try {
                inflateMenu(R.menu.bottom_nav_menu)
            } catch (e: Exception) {
                menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_today)
                menu.add(0, 2, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)
            }

            layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }

        root.addView(header)
        root.addView(nav)

        setContentView(root)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
    }
}
