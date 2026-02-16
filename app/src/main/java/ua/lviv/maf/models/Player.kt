package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("id")
    val id: String? = null, // Тепер це поле не обов'язкове

    @SerializedName("name")
    val name: String? = null, // І це теж

    @SerializedName("number")
    val number: String? = null,

    @SerializedName("position")
    val position: String? = null,

    @SerializedName("photo")
    val photo: String? = null
)
