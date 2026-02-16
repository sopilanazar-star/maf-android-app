package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class Player(
    // Приймаємо будь-що (Any?), щоб Gson не падав від false чи чисел
    @SerializedName("id") private val _id: Any? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("number") private val _number: Any? = null,
    @SerializedName("position") val position: String? = null,
    @SerializedName("photo") private val _photo: Any? = null
) {
    // А тут магія: перетворюємо "брудні" дані в чистий текст для Адаптера

    val id: String
        get() = _id?.toString() ?: ""

    val number: String
        get() = _number?.toString() ?: ""

    val photo: String
        get() {
            // Якщо сервер надіслав false (булеве), повертаємо пустий рядок
            if (_photo is Boolean) return ""
            return _photo?.toString() ?: ""
        }
}
