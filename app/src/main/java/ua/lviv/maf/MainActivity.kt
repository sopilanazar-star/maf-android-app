package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Створюємо найпростіший напис без тем і стилів
        val testView = TextView(this).apply {
            text = "КОНТАКТ Є! ПРОБЛЕМА В ТЕМІ (THEMES.XML)"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#007c3d"))
            textSize = 20f
            gravity = Gravity.CENTER
        }
        
        setContentView(testView)
    }
}
