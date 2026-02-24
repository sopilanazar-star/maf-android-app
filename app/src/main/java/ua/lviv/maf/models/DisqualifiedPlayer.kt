package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class DisqualifiedPlayer(
    @SerializedName("player_id") val playerId: String? = null,
    @SerializedName("player") val name: String? = "Невідомий", 
    @SerializedName("team") val teamName: String? = "Без команди",
    @SerializedName("matches") val matches: Int? = 0,
    @SerializedName("status") val status: String? = "",
    @SerializedName("reason") val reason: String? = "",       // Нове поле: причина
    @SerializedName("expiry_date") val expiryDate: String? = "" // Нове поле: дата завершення
)
