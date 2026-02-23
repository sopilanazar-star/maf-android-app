package ua.lviv.maf

// Базовий клас для елементів списку (заголовок туру АБО матч)
sealed class PredictionListItem {
    // Елемент-заголовок (наприклад, "1 тур")
    data class StageHeader(val stageName: String) : PredictionListItem()
    
    // Елемент-матч
    data class MatchItem(val match: PredictionMatchModel) : PredictionListItem()
}

// Модель самого матчу (оновлена з новими полями з API)
data class PredictionMatchModel(
    val id: Int,
    val team1Name: String,
    val team1LogoUrl: String,
    val team2Name: String,
    val team2LogoUrl: String,
    val matchDateStr: String,
    val tournament: String, // Назва турніру і ліги
    val stage: String,      // Назва туру (етапу)
    val deadlineTimestamp: Long,
    var predictedScore1: String? = null,
    var predictedScore2: String? = null
)
