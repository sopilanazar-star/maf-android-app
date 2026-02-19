package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("number") val number: String,
    @SerializedName("position") val position: String,
    @SerializedName("photo") val photo: String,
    @SerializedName("birth_date") val birthDate: String? = "",
    @SerializedName("age") val age: Int? = 0
)
