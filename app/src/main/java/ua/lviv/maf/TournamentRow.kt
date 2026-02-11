package ua.lviv.maf

/**
 * Оновлена модель даних для підтримки переходів у деталі матчу.
 */
data class TournamentRow(
    val id: String = "",          // ID матчу з бази (для запиту деталей)
    val team1: String = "",       // Назва першої команди
    val logo1: String = "",       // Логотип першої команди
    val team2: String = "",       // Назва другої команди
    val logo2: String = "",       // Логотип другої команди
    val score: String = "",       // Рахунок (напр. "2 : 1") або час (напр. "18:00")
    val date: String = "",        // Дата матчу
    val league: String = "",      // Назва турніру
    val stage: String = "",       // Тур або етап
    val stadium: String = "",     // Назва стадіону
    val referee: String = "",     // Головний арбітр
    val isHeader: Boolean = false // Прапорець для заголовка ліги
)
