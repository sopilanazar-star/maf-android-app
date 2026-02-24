package ua.lviv.maf

data class TournamentRow(
    val id: String = "",
    val home_team_id: String = "0",
    val away_team_id: String = "0",
    val team1: String = "",
    val logo1: String = "",
    val team2: String = "",
    val logo2: String = "",
    val score: String = "", // Рахунок або час матчу
    val date: String = "",
    val league: String = "",
    val stage: String = "",
    val stadium: String = "",
    val referee: String = "",
    val isHeader: Boolean = false
)
