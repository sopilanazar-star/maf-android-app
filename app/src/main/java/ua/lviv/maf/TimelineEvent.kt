package ua.lviv.maf

data class TimelineEvent(
    val minute: String,
    val type: String,
    val playerName: String,
    val teamId: String
)
