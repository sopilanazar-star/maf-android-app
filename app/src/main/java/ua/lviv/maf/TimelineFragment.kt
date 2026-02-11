package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TimelineFragment : Fragment() {

    private val client = OkHttpClient()

    companion object {
        fun newInstance(matchId: String): TimelineFragment {
            val args = Bundle().apply { putString("match_id", matchId) }
            return TimelineFragment().apply { arguments = args }
        }
    }

    // Правильний спосіб створення View для фрагмента
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_timeline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val matchId = arguments?.getString("match_id") ?: return
        
        val container = view.findViewById<LinearLayout>(R.id.timelineContainer)
        val tvLoading = view.findViewById<TextView>(R.id.tvTimelineLoading)

        if (container != null) {
            loadTimelineData(matchId, container, tvLoading)
        }
    }

    private fun loadTimelineData(matchId: String, container: LinearLayout, tvLoading: TextView?) {
        val url = "https://maf.lviv.ua/wp-json/maf/v2/match-details?id=$matchId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    tvLoading?.text = "Помилка мережі"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { 
                    if (!response.isSuccessful) return
                    val json = response.body?.string() ?: return
                    activity?.runOnUiThread {
                        tvLoading?.visibility = View.GONE
                        parseAndShowTimeline(json, container)
                    }
                }
            }
        })
    }

    private fun parseAndShowTimeline(json: String, container: LinearLayout) {
        try {
            val root = JSONObject(json)
            val timeline = root.optJSONArray("timeline") ?: return

            container.removeAllViews() // Очищуємо старі дані

            if (timeline.length() == 0) {
                val emptyTv = TextView(context).apply {
                    text = "Подій поки немає"
                    setTextColor(android.graphics.Color.GRAY)
                    gravity = android.view.Gravity.CENTER
                }
                container.addView(emptyTv)
                return
            }

            for (i in 0 until timeline.length()) {
                val event = timeline.getJSONObject(i)
                val minute = event.optString("minute")
                val playerName = event.optString("player_name")
                val type = event.optString("type")

                val textView = TextView(context).apply {
                    text = "$minute' - $playerName ($type)"
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 16f
                    setPadding(0, 15, 0, 15)
                }
                container.addView(textView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
