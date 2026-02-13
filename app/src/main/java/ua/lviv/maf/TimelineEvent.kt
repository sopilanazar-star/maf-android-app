package ua.lviv.maf

import com.google.gson.annotations.SerializedName

data class TimelineEvent(
    @SerializedName("minute")
    val minute: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("player_name") // Це зв'язує JSON з кодом
    val player_name: String,       // Тепер адаптер знайде цю змінну

    @SerializedName("player_out_name")
    val player_out_name: String?,  // Знак питання важливий!

    @SerializedName("side")
    val side: String               // Саме це відповідає за "left" або "right"
)
