package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class DisqualifiedPlayer(
    @SerializedName("player") val name: String? = "Невідомий", 
    @SerializedName("team") val teamName: String? = "Без команди",
    @SerializedName("matches") val matches: Int? = 0,
    @SerializedName("status") val status: String? = ""
)
