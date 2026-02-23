package ua.lviv.maf

data class PredictionMatchModel(
    val id: Int,
    val team1Name: String,
    val team1LogoUrl: String,
    val team2Name: String,
    val team2LogoUrl: String,
    val matchDateStr: String,
    val league: String,
    val deadlineTimestamp: Long,
    var predictedScore1: String? = null,
    var predictedScore2: String? = null
)
