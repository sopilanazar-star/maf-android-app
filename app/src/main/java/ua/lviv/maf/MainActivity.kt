package ua.lviv.maf

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Повний екран (без статус-бару)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }

        // 2. Фон (Градієнт) - створюємо прямо в коді
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor("#007c3d"), Color.parseColor("#004d26"))
        )

        val root = RelativeLayout(this).apply {
            background = gradient
        }

        // 3. Текст-заглушка замість Спінера
        val title = TextView(this).apply {
            id = View.generateViewId()
            text = "МАФ 2026"
            setTextColor(Color.WHITE)
            textSize = 22f
            setPadding(50, 100, 0, 0)
        }
        root.addView(title)

        // 4. НИЖНЯ НАВІГАЦІЯ (БЕЗПЕЧНА)
        try {
            val nav = BottomNavigationView(this).apply {
                id = View.generateViewId()
                setBackgroundColor(Color.WHITE)
                
                // Використовуємо ТІЛЬКИ системні іконки, щоб не було вильотів
                menu.add(0, 1, 0, "Новини").setIcon(android.R.drawable.ic_menu_gallery)
                menu.add(0, 2, 0, "Матчі").setIcon(android.R.drawable.ic_menu_today)
                menu.add(0, 3, 0, "Таблиці").setIcon(android.R.drawable.ic_menu_sort_by_size)
                menu.add(0, 4, 0, "Більше").setIcon(android.R.drawable.ic_menu_more)

                layoutParams = RelativeLayout.LayoutParams(-1, -2).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }
            root.addView(nav)
        } catch (e: Exception) {
            // Навіть якщо нава впаде, ми її просто не покажемо
        }

        setContentView(root)
    }
}
