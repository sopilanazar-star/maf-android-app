package ua.lviv.maf

data class MatchEvent(
    val minute: String,
    val type: String,     // 'goal', 'yellow_card', 'red_card', 'sub'
    val playerName: String,
    val teamId: String    // '1' - господарі, '2' - гості
)
