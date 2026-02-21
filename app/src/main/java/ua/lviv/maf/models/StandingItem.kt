package ua.lviv.maf.models

data class PlayoffGroup(
    val groupName: String,
    val clubs: List<Int>
)

sealed class StandingItem {

    data class GroupHeader(val title: String) : StandingItem()

    object TableHeader : StandingItem()

    data class TeamRow(
        val position: Int,
        val name: String,
        val logo: String,
        val games: Int,
        val win: Int,
        val draw: Int,
        val loss: Int,
        val goalsFor: Int,
        val goalsAgainst: Int,
        val points: Int
    ) : StandingItem()

    data class PlayoffHeader(val title: String) : StandingItem()

    data class PlayoffStage(
        val title: String
    ) : StandingItem()
}
