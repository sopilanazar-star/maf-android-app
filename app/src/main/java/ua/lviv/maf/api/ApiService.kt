package ua.lviv.maf.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ua.lviv.maf.models.DisqualifiedPlayer

interface ApiService {
    @GET("wp-json/maf/v1/banned-players")
    fun getDisqualifiedPlayers(
        @Query("year") year: Int
    ): Call<List<DisqualifiedPlayer>>
}
