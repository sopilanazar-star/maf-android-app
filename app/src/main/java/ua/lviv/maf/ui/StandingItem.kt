package ua.lviv.maf.ui

sealed class StandingItem {
    data class GroupHeader(val title: String) : StandingItem()
    data class TeamRow(
        val position: Int,
        val teamName: String,
        val logo: String,
        val games: Int,
        val win: Int,
        val draw: Int,
        val loss: Int,
        val goals: String,
        val points: Int
    ) : StandingItem()
}

data class Competition(
    val id: Int,
    val title: String
)
