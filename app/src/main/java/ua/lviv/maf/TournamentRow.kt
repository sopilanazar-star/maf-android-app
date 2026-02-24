package ua.lviv.maf

data class TournamentRow(
    val id: String = "",
    val home_team_id: String = "0",
    val away_team_id: String = "0",
    val team1: String = "",
    val logo1: String = "",
    val team2: String = "",
    val logo2: String = "",
    val score: String = "", // Сюди потрапить рахунок з API
    val date: String = "",  // Сюди потрапить "24.02.2026"
    val league: String = "",
    val stage: String = "",
    val stadium: String = "",
    val referee: String = "",
    val isHeader: Boolean = false,
    val status: String = ""
)
