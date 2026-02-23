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

    // Лінк на бота для авторизації
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

        btnAuthTelegram.setOnClickListener {
            startTelegramAuth()
        }

        loadDataForYear(AppConfig.selectedYear)
        checkAuthState()

        return view
    }

    fun refreshData() {
        if (isAdded) {
            loadDataForYear(AppConfig.selectedYear)
            checkAuthState()
        }
    }

    private fun loadDataForYear(year: String) {
        tvYearTitle.text = "Прогнози на матчі. Сезон: $year"
    }

    private fun checkAuthState() {
        val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        val isLogged = sharedPrefs.getBoolean("is_logged_in", false)

        if (isLogged) {
            val username = sharedPrefs.getString("tg_username", "Гравець")
            layoutAuth.visibility = View.GONE
            rvPredictions.visibility = View.VISIBLE
            // Можемо привітати гравця або просто показати матчі
            tvYearTitle.text = "Прогнози ($username). Сезон: ${AppConfig.selectedYear}"
            fetchPredictionsFromApi() 
        } else {
            layoutAuth.visibility = View.VISIBLE
            rvPredictions.visibility = View.GONE
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
            
            Toast.makeText(context, "Переходимо в Telegram... Натисніть 'Розпочати'", Toast.LENGTH_LONG).show()
            
            // Запускаємо перевірку статусу авторизації
            startAuthPolling(authSessionCode)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram не встановлено!", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔥 ДОДАНО: Логіка опитування сервера (Polling)
    private fun startAuthPolling(authCode: String) {
        authCheckHandler = Handler(Looper.getMainLooper())
        authCheckRunnable = object : Runnable {
            override fun run() {
                checkAuthApi(authCode)
                // Повторюємо запит кожні 3 секунди
                authCheckHandler?.postDelayed(this, 3000) 
            }
        }
        authCheckHandler?.post(authCheckRunnable!!)
    }

    private fun stopAuthPolling() {
        authCheckRunnable?.let { authCheckHandler?.removeCallbacks(it) }
    }

    // Зупиняємо перевірку, якщо користувач вийшов з вкладки
    override fun onDestroyView() {
        super.onDestroyView()
        stopAuthPolling()
    }

    // 🔥 ДОДАНО: Запит до нашого нового API на сайті
    private fun checkAuthApi(authCode: String) {
        val url = "$AUTH_CHECK_API_URL?auth_code=$authCode"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Ігноруємо помилки мережі, просто спробуємо ще раз через 3 сек
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: return
                try {
                    val json = JSONObject(responseData)
                    if (json.optString("status") == "success") {
                        val userObj = json.getJSONObject("user")
                        val tgId = userObj.optString("tg_id")
                        val tgUsername = userObj.optString("username")

                        // Зберігаємо дані і зупиняємо таймер
                        activity?.runOnUiThread {
                            val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
                            sharedPrefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("tg_id", tgId)
                                .putString("tg_username", tgUsername)
                                .apply()

                            stopAuthPolling()
                            Toast.makeText(context, "Успішно! Вітаємо, $tgUsername ⚽️", Toast.LENGTH_SHORT).show()
                            checkAuthState() // Оновлюємо інтерфейс
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun fetchPredictionsFromApi() {
        // Заглушка: тут будемо тягнути список матчів для прогнозування
    }
}
