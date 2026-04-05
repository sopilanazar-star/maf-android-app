package ua.lviv.maf.models

data class PredictionMatchModel(
    val id: Int,
    val tournamentId: Int,
    val team1Name: String,
    val team1LogoUrl: String,
    val team2Name: String,
    val team2LogoUrl: String,
    val matchDateStr: String,
    val tournament: String,
    val stage: String,
    val deadlineTimestamp: Long,

    // UI state (RecyclerView + EditText)
    var predictedScore1: String?,
    var predictedScore2: String?
)