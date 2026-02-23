package ua.lviv.maf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

class PredictionsFragment : Fragment() {

    private lateinit var tvYearTitle: TextView
    private lateinit var layoutAuth: LinearLayout
    private lateinit var rvPredictions: RecyclerView
    private lateinit var btnAuthTelegram: Button

    // Твій лінк на бота для авторизації
    private val TELEGRAM_BOT_URL = "https://t.me/MAFLoginBot"

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

        // Відразу при завантаженні тягнемо рік зі спінера
        loadDataForYear(AppConfig.selectedYear)
        
        // Перевіряємо, чи авторизований користувач
        checkAuthState()

        return view
    }

    // Метод для реакції на зміну року
    fun refreshData() {
        if (isAdded) {
            loadDataForYear(AppConfig.selectedYear)
            checkAuthState() // Оновлюємо стан при зміні даних
        }
    }

    private fun loadDataForYear(year: String) {
        tvYearTitle.text = "Прогнози на матчі. Сезон: $year"
    }

    // ДОДАНО: Перевірка авторизації
    private fun checkAuthState() {
        val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        val isLogged = sharedPrefs.getBoolean("is_logged_in", false)

        if (isLogged) {
            // Якщо авторизований - показуємо список, ховаємо авторизацію
            layoutAuth.visibility = View.GONE
            rvPredictions.visibility = View.VISIBLE
            fetchPredictionsFromApi() // Тут будемо тягнути матчі
        } else {
            // Якщо ні - ховаємо список, показуємо кнопку входу
            layoutAuth.visibility = View.VISIBLE
            rvPredictions.visibility = View.GONE
        }
    }

    // ДОДАНО: Логіка переходу в Telegram для авторизації
    private fun startTelegramAuth() {
        // Генеруємо унікальний код для сесії авторизації
        val authSessionCode = UUID.randomUUID().toString().substring(0, 8)
        
        // Зберігаємо цей код тимчасово, щоб потім перевіряти статус по API
        val sharedPrefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("auth_session_code", authSessionCode).apply()

        // Формуємо лінк з параметром start (Deep Link)
        val botUrl = "$TELEGRAM_BOT_URL?start=auth_$authSessionCode"

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(botUrl))
            startActivity(intent)
            
            // ТУТ В МАЙБУТНЬОМУ БУДЕ ЗАПУСК ПЕРЕВІРКИ API (POLLING)
            Toast.makeText(context, "Переходимо в Telegram... Натисніть 'Розпочати'", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram не встановлено!", Toast.LENGTH_SHORT).show()
        }
    }

    // ДОДАНО: Заглушка для майбутнього API-запиту прогнозів
    private fun fetchPredictionsFromApi() {
        // Тут будемо використовувати Retrofit для отримання списку матчів
    }
}
