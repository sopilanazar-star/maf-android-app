package ua.lviv.maf

data class StandingRow(
    val team_id: String,
    val position: Int,
    val team_name: String,
    val logo: String,
    val games: Int,
    val win: Int,
    val draw: Int,
    val loss: Int,
    val goals_for: Int,
    val goals_against: Int,
    val points: Int
)
