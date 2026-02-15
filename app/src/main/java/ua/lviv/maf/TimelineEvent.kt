package ua.lviv.maf

import com.google.gson.annotations.SerializedName

data class TimelineEvent(
    @SerializedName("minute")
    val minute: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("player_name")
    val player_name: String,

    @SerializedName("player_out_name")
    val player_out_name: String?,

    @SerializedName("side")
    val side: String,

    @SerializedName("team_id") // Нове поле
    val team_id: Int           // ID команди, яка створила подію
)
