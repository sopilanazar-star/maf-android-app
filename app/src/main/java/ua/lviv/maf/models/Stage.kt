package ua.lviv.maf.models // Перевір, щоб тут був твій шлях

data class Stage(
    val id: Int,          // ID туру з бази maf.lviv.ua
    val name: String,      // Назва (напр. "1 тур")
    var isSelected: Boolean = false // Чи вибраний цей тур зараз (для підсвітки)
)