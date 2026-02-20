package ua.lviv.maf

import java.util.Calendar

object AppConfig {
    // Автоматично визначаємо поточний рік (наприклад, 2026)
    var selectedYear: String = Calendar.getInstance().get(Calendar.YEAR).toString()
}
