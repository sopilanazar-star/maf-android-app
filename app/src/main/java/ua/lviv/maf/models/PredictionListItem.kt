package ua.lviv.maf.models

sealed class PredictionListItem {

    data class StageHeader(
        val title: String
    ) : PredictionListItem()

    data class MatchItem(
        val match: PredictionMatchModel
    ) : PredictionListItem()
}