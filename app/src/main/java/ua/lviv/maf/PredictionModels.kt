package ua.lviv.maf

sealed class PredictionListItem {
    data class StageHeader(val stageName: String) : PredictionListItem()
    data class MatchItem(val match: PredictionMatchModel) : PredictionListItem()
}

data class PredictionMatchModel(
    val id: Int,
    val tournamentId: Int, // 🔥 Додано ID турніру
    val team1Name: String,
    val team1LogoUrl: String,
    val team2Name: String,
    val team2LogoUrl: String,
    val matchDateStr: String,
    val tournament: String,
    val stage: String,
    val deadlineTimestamp: Long,
    var predictedScore1: String? = null,
    var predictedScore2: String? = null
)
