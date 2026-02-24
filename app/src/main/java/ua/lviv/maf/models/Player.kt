package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

// Основна модель гравця
data class Player(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("number") val number: String,
    @SerializedName("position") val position: String,
    @SerializedName("photo") val photo: String,
    @SerializedName("birth_date") val birthDate: String? = "",
    @SerializedName("age") val age: Int? = 0
)

// Модель для списку дискваліфікацій
data class DisqualifiedPlayer(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("team_name") val teamName: String,     // Назва команди
    @SerializedName("reason") val reason: String,           // Причина (червона, жовті тощо)
    @SerializedName("expiry_date") val expiryDate: String,  // Дата "yyyy-MM-dd"
    @SerializedName("year") val year: Int                   // Рік для фільтрації через спінер
)
