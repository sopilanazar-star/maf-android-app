package ua.lviv.maf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

class PredictionsFragment : Fragment() {

    private lateinit var tvYearTitle: TextView
    private lateinit var layoutAuth: LinearLayout
    private lateinit var rvPredictions: RecyclerView
    private lateinit var btnAuthTelegram: Button

    private val TELEGRAM_BOT_URL = "https://t.me/MAFLoginBot"
    private val AUTH_CHECK_API_URL = "https://maf.lviv.ua/wp-json/maf-bet/v1/auth-check"

    private val client = OkHttpClient()
    private var authCheckHandler: Handler? = null
    private var authCheckRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_predictions, container, false)

        tvYearTitle = view.findViewById(R.id.tvYearTitle)
        layoutAuth = view.findViewById(R.id.layoutAuth)
        rvPredictions = view.findViewById(R.id.rvPredictions)
        btnAuthTelegram = view.findViewById(R.id.btnAuthTelegram)

        // Кнопка Назад (повернення в меню "Більше")
        val btnBack = view.findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnAuthTelegram.setOnClickListener {
            startTelegramAuth()
        }

        checkAuthState()

        return view
    }

    fun refreshData() {
        if (isAdded) {
            checkAuthState()
        }
    }

    private fun loadDataForYear(year: String, username: String) {
        tvYearTitle.text = "Прогнози ($username). Сезон: $year"
        fetchPredictionsFromApi()
    }

    private fun checkAuthState() {
        val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        val isLogged = sharedPrefs.getBoolean("is_logged_in", false)

        if (isLogged) {
            val username = sharedPrefs.getString("tg_username", "Гравець") ?: "Гравець"
            layoutAuth.visibility = View.GONE
            rvPredictions.visibility = View.VISIBLE
            loadDataForYear(AppConfig.selectedYear, username)
        } else {
            layoutAuth.visibility = View.VISIBLE
            rvPredictions.visibility = View.GONE
            tvYearTitle.text = "Прогнози на матчі. Сезон: ${AppConfig.selectedYear}"
        }
    }

    private fun startTelegramAuth() {
        val authSessionCode = UUID.randomUUID().toString().substring(0, 8)
        val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("auth_session_code", authSessionCode).apply()

        val botUrl = "$TELEGRAM_BOT_URL?start=auth_$authSessionCode"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(botUrl))
            startActivity(intent)
            Toast.makeText(context, "Переходимо в Telegram...", Toast.LENGTH_LONG).show()
            startAuthPolling(authSessionCode)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram не встановлено!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAuthPolling(authCode: String) {
        authCheckHandler = Handler(Looper.getMainLooper())
        authCheckRunnable = object : Runnable {
            override fun run() {
                checkAuthApi(authCode)
                authCheckHandler?.postDelayed(this, 3000) 
            }
        }
        authCheckHandler?.post(authCheckRunnable!!)
    }

    private fun stopAuthPolling() {
        authCheckRunnable?.let { authCheckHandler?.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAuthPolling()
    }

    private fun checkAuthApi(authCode: String) {
        val url = "$AUTH_CHECK_API_URL?auth_code=$authCode"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: return
                try {
                    val json = JSONObject(responseData)
                    if (json.optString("status") == "success") {
                        val userObj = json.getJSONObject("user")
                        activity?.runOnUiThread {
                            val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
                            sharedPrefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("tg_id", userObj.optString("tg_id"))
                                .putString("tg_username", userObj.optString("username"))
                                .apply()

                            stopAuthPolling()
                            checkAuthState()
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }

    // 🔥 ПОВНИЙ ПАРСИНГ ТА ГРУПУВАННЯ ПО ТУРАХ
    private fun fetchPredictionsFromApi() {
        val year = AppConfig.selectedYear
        val url = "https://maf.lviv.ua/wp-json/maf-bet/v1/matches-for-prediction?year=$year"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread { Toast.makeText(context, "Помилка мережі", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: return
                try {
                    val json = JSONObject(responseData)
                    if (json.optString("status") == "success") {
                        val matchesArray = json.getJSONArray("matches")
                        val matchesList = mutableListOf<PredictionMatchModel>()

                        for (i in 0 until matchesArray.length()) {
                            val obj = matchesArray.getJSONObject(i)
                            matchesList.add(PredictionMatchModel(
                                id = obj.getInt("id"),
                                team1Name = obj.getString("team1_name"),
                                team1LogoUrl = obj.optString("team1_logo", ""),
                                team2Name = obj.getString("team2_name"),
                                team2LogoUrl = obj.optString("team2_logo", ""),
                                matchDateStr = obj.getString("match_date"),
                                tournament = obj.optString("tournament", "Турнір"),
                                stage = obj.optString("stage", "Тур"),
                                deadlineTimestamp = obj.getLong("deadline_timestamp") * 1000L
                            ))
                        }

                        // ГРУПУВАННЯ ДЛЯ АДАПТЕРА
                        val groupedItems = mutableListOf<PredictionListItem>()
                        matchesList.groupBy { it.stage }.forEach { (stageName, matches) ->
                            groupedItems.add(PredictionListItem.StageHeader(stageName))
                            matches.forEach { groupedItems.add(PredictionListItem.MatchItem(it)) }
                        }

                        activity?.runOnUiThread {
                            rvPredictions.layoutManager = LinearLayoutManager(context)
                            rvPredictions.adapter = PredictionAdapter(groupedItems) { match, s1, s2 ->
                                // Це Toast перед відправкою, ми його замінимо на POST запит
                                Toast.makeText(context, "Надсилаємо: ${match.team1Name} $s1:$s2 ${match.team2Name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        })
    }
}
