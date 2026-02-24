package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class DisqualifiedPlayer(
    @SerializedName("player") val name: String,   // ПІБ гравця
    @SerializedName("team") val teamName: String, // Назва команди
    @SerializedName("matches") val matches: Int,  // Кількість матчів
    @SerializedName("status") val status: String  // "активна" або інше
)
