package ua.lviv.maf.models

import com.google.gson.annotations.SerializedName

data class Player(

    val id: String? = null,

    val name: String? = null,

    val number: String? = null,

    val position: String? = null,

    val photo: String? = null,

    @SerializedName("birth_date")
    val birthDate: String? = null,

    val age: Int? = null,

    val team_name: String? = null,

    val team_logo: String? = null
)