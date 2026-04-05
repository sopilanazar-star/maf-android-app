package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class DisqualifiedPlayer(

    @SerializedName("player_id")
    val playerId: String? = null,

    @SerializedName("player")
    val name: String? = null,

    @SerializedName("team")
    val teamName: String? = null,

    @SerializedName("team_logo")
    val teamLogo: String? = null,

    @SerializedName("photo")
    val photo: String? = null,

    @SerializedName("position")
    val position: String? = null,

    @SerializedName("birth_date")
    val birthDate: String? = null,

    @SerializedName("age")
    val age: Int? = null,

    @SerializedName("matches")
    val matches: Int? = 0,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("expiry_date")
    val expiryDate: String? = null
)