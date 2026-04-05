package ua.lviv.maf.models

data class PlayerMatchPrediction(
    val teams: String,       // Наприклад: "Ураган - Устя"
    val date: String,        // Дата матчу
    val userPrediction: String, // Прогноз гравця "2:1"
    val realScore: String,   // Реальний рахунок "1:1"
    val points: String       // Очки за матч "+1"
)