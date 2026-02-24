package ua.lviv.maf

data class TournamentRow(
    // 🟢 ОРИГІНАЛЬНІ ПОЛЯ (їхній порядок тепер не зламає адаптер)
    val id: String = "",
    val team1: String = "",
    val logo1: String = "",
    val team2: String = "",
    val logo2: String = "",
    val score: String = "", // Тепер тут точно буде час або рахунок!
    val date: String = "",  // А тут точно дата
    val league: String = "",
    val stage: String = "",
    val isHeader: Boolean = false,

    // 🟡 НОВІ ПОЛЯ (в самому кінці, нічого не зсувають)
    val home_team_id: String = "0",
    val away_team_id: String = "0",
    val stadium: String = "",
    val referee: String = "",
    val status: String = ""
)
