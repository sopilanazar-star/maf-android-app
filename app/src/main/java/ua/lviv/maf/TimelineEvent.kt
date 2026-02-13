package ua.lviv.maf

data class TimelineEvent(
    val minute: String,
    val type: String,
    val playerName: String,
    val playerOutName: String? = null, // Додаємо для замін (хто вийшов)
    val teamId: String,
    val side: String // "left" для господарів, "right" для гостей
)
