package ua.lviv.maf

data class TournamentRow(
    val year: String,
    val winner: String,
    val second: String = "",
    val third: String = ""
)
