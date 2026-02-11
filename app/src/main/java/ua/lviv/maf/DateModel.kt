package ua.lviv.maf

data class DateModel(
    val date: String,          // Повна дата для фільтрації (напр. "16.11.2025")
    val dayName: String,       // Скорочена назва дня (напр. "Нд")
    val dayNumber: String,     // Число (напр. "16")
    val month: String,         // Назва місяця (напр. "Лис")
    var isSelected: Boolean = false // Чи обрана ця дата зараз
)
