package ua.lviv.maf

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TimelineFragment : Fragment(R.layout.fragment_timeline) {

    private val client = OkHttpClient()

    companion object {
        fun newInstance(matchId: String): TimelineFragment {
            val args = Bundle().apply { putString("match_id", matchId) }
            return TimelineFragment().apply { arguments = args }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val matchId = arguments?.getString("match_id") ?: return
        
        // Знаходимо контейнер, куди будемо додавати рядки подій
        val container = view.findViewById<LinearLayout>(R.id.timelineContainer)
        val tvLoading = view.findViewById<TextView>(R.id.tvTimelineLoading)

        loadTimelineData(matchId, container, tvLoading)
    }

    private fun loadTimelineData(matchId: String, container: LinearLayout, tvLoading: TextView) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/match-details?id=$matchId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { tvLoading.text = "Помилка завантаження" }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string() ?: return
                activity?.runOnUiThread {
                    tvLoading.visibility = View.GONE
                    parseAndShowTimeline(json, container)
                }
            }
        })
    }

    private fun parseAndShowTimeline(json: String, container: LinearLayout) {
        val root = JSONObject(json)
        val timeline = root.optJSONArray("timeline") ?: return

        for (i in 0 until timeline.length()) {
            val event = timeline.getJSONObject(i)
            val minute = event.optString("minute")
            val playerName = event.optString("player_name")
            val type = event.optString("type")

            // Створюємо простий текстовий рядок для кожної події (поки без іконок)
            val textView = TextView(context).apply {
                text = "$minute' - $playerName ($type)"
                setTextColor(android.graphics.Color.WHITE)
                setPadding(0, 10, 0, 10)
                textSize = 16f
            }
            container.addView(textView)
        }
    }
}
