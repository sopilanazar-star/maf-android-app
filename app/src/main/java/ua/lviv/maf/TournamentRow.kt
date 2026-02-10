package ua.lviv.maf

/**
 * Модель даних для рядка матчу у списку.
 * Поля відповідають JSON-відповіді з нашого WordPress плагіна (maf/v2/matches).
 */
data class TournamentRow(
    val team1: String,        // Назва першої команди
    val logo1: String,        // Посилання на логотип першої команди
    val team2: String,        // Назва другої команди
    val logo2: String,        // Посилання на логотип другої команди
    val score: String,        // Рахунок (напр. "2 : 1") або час (напр. "18:00")
    val date: String,         // Дата матчу
    val league: String,       // Назва ліги (напр. "I ліга (Дорослі) 2025")
    val isHeader: Boolean = false // Прапорець для заголовка (якщо використовуєш)
)
