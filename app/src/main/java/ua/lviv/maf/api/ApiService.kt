package ua.lviv.maf.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ua.lviv.maf.models.DisqualifiedPlayer
import ua.lviv.maf.models.Player

interface ApiService {
    // Отримання списку дискваліфікованих гравців за рік
    @GET("wp-json/maf/v1/banned-players")
    fun getDisqualifiedPlayers(
        @Query("year") year: String
    ): Call<List<DisqualifiedPlayer>>

    // 🔥 НОВЕ: Отримання повної анкети гравця за його ID
    @GET("wp-json/maf/v2/player-profile")
    fun getPlayerProfile(
        @Query("id") id: String
    ): Call<Player>
}
