package ua.lviv.maf.models

data class PredictionTablePlayer(
    val position: Int,
    val name: String,
    val points: Int,
    val exact: Int,
    val correct: Int,
    val wrong: Int
)
