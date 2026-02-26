package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MediaFragment : Fragment() {

    private lateinit var rvMedia: RecyclerView
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v2/matches"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_media, container, false)

        rvMedia = view.findViewById(R.id.rvMedia)
        rvMedia.layoutManager = LinearLayoutManager(context)

        // Кнопка "Назад"
        val btnBack = view.findViewById<View>(R.id.btnBackMedia)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadVideosFromApi()

        return view
    }

    // --- ДОДАЛИ ЦЮ ФУНКЦІЮ ДЛЯ ОНОВЛЕННЯ РОКУ ---
    fun refreshData() {
        if (isAdded) {
            loadVideosFromApi()
        }
    }

    private fun loadVideosFromApi() {
        // Беремо актуальний рік
        val year = AppConfig.selectedYear 
        val client = OkHttpClient()
        val request = Request.Builder().url("$MAF_API_URL?year=$year").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string() ?: ""
                try {
                    val array = JSONArray(jsonData)
                    val videosList = mutableListOf<TournamentRow>()

                    for (i in 0 until array.length()) {
                        val m = array.getJSONObject(i)
                        val youtubeId = m.optString("youtube_id", "")
                        
                        if (youtubeId.isNotEmpty()) {
                            videosList.add(TournamentRow(
                                team1 = m.optString("team1"),
                                team2 = m.optString("team2"),
                                score = m.optString("score"),
                                date = m.optString("date"),
                                league = m.optString("league"),
                                youtubeId = youtubeId
                            ))
                        }
                    }

                    activity?.runOnUiThread {
                        rvMedia.adapter = MediaAdapter(videosList)
                    }
                } catch (e: Exception) {}
            }
        })
    }
}
