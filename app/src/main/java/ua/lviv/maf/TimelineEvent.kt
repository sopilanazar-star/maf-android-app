package ua.lviv.maf

data class TimelineEvent(
    val minute: String,
    val type: String,
    val player_name: String,      // ТУТ МАЄ БУТИ НИЖНЄ ПІДКРЕСЛЕННЯ
    val player_out_name: String?, // ТУТ ТЕЖ
    val side: String
)
