package ua.lviv.maf

data class TournamentRow(
    val id: String = "0",
    val team1: String = "",
    val team2: String = "",
    val score: String = "",
    val logo1: String = "",
    val logo2: String = "",
    val league: String = "",
    val stage: String = "",
    val date: String = "",
    val stadium: String = "",
    val referee: String = "",
    val home_team_id: String = "0",
    val away_team_id: String = "0",
    val isHeader: Boolean = false
)
