package ua.lviv.maf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Переконайся, що у тебе є layout fragment_timeline з LinearLayout (id: timelineContainer)
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
                    setPadding(0, 50, 0, 0)
                }
                container.addView(emptyTv)
                return
            }

            for (i in 0 until timeline.length()) {
                val event = timeline.getJSONObject(i)
                val minute = event.optString("minute")
                val playerName = event.optString("player_name")
                val type = event.optString("type")

                // Створюємо горизонтальний контейнер для одного рядка події
                val rowLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(20, 25, 20, 25)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }

                // 1. Текст хвилини (наприклад, 25')
                val tvMin = TextView(context).apply {
                    text = "$minute'"
                    setTextColor(android.graphics.Color.parseColor("#BCBCBC"))
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(90, -2)
                }

                // 2. Іконка події (М'яч, Картка, Заміна)
                val ivIcon = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(45, 45).apply {
                        setMargins(10, 0, 30, 0)
                    }
                }

                // Логіка вибору іконки та кольору залежно від типу події
                when (type) {
                    "goal" -> {
                        ivIcon.setImageResource(android.R.drawable.ic_btn_speak_now) // Схоже на м'яч
                        ivIcon.setColorFilter(android.graphics.Color.WHITE)
                    }
                    "goal_og" -> { // Автогол
                        ivIcon.setImageResource(android.R.drawable.ic_btn_speak_now)
                        ivIcon.setColorFilter(android.graphics.Color.RED)
                    }
                    "yellow_card" -> {
                        ivIcon.setImageResource(android.R.drawable.checkbox_on_background) // Прямокутник
                        ivIcon.setColorFilter(android.graphics.Color.YELLOW)
                    }
                    "red_card" -> {
                        ivIcon.setImageResource(android.R.drawable.checkbox_on_background)
                        ivIcon.setColorFilter(android.graphics.Color.RED)
                    }
                    "substitution" -> {
                        ivIcon.setImageResource(android.R.drawable.stat_notify_sync) // Стрілочки
                        ivIcon.setColorFilter(android.graphics.Color.GREEN)
                    }
                    else -> {
                        ivIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                        ivIcon.setColorFilter(android.graphics.Color.GRAY)
                    }
                }

                // 3. Прізвище гравця
                val tvPlayer = TextView(context).apply {
                    text = if (type == "goal_og") "$playerName (АГ)" else playerName
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                }

                // Додаємо всі елементи в рядок
                rowLayout.addView(tvMin)
                rowLayout.addView(ivIcon)
                rowLayout.addView(tvPlayer)

                // Додаємо рядок в основний контейнер
                container.addView(rowLayout)

                // Додаємо тонку лінію-розділювач між подіями
                val divider = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(android.graphics.Color.parseColor("#333333"))
                }
                container.addView(divider)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
