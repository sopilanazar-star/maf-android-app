package ua.lviv.maf

data class TournamentRow(
    val team1: String,        // Назва першої команди або заголовок
    val team2: String = "",   // Назва другої команди
    val score: String = "",   // Рахунок (напр. "2 : 1")
    val isHeader: Boolean = false // Чи це заголовок розділу
)
