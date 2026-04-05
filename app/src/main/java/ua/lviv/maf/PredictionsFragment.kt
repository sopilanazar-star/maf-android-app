package ua.lviv.maf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import ua.lviv.maf.adapters.PredictionTableAdapter
import ua.lviv.maf.models.PredictionTablePlayer
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ua.lviv.maf.adapters.*
import ua.lviv.maf.models.*
import ua.lviv.maf.network.Network
import java.io.IOException
import java.util.UUID

class PredictionsFragment : Fragment() {

    private lateinit var tvYearTitle: TextView
    private lateinit var layoutAuth: ScrollView
    private lateinit var layoutRules: LinearLayout
    private lateinit var rvPredictions: RecyclerView
    private lateinit var rvStages: RecyclerView
    private lateinit var btnAuthTelegram: Button
    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnAcceptRules: Button

    private lateinit var stagesAdapter: StagesAdapter
    private lateinit var predictionAdapter: PredictionAdapter

    private val client = Network.client

    private val TELEGRAM_BOT_URL = "https://t.me/MAFLoginBot"
    private val BASE_URL = "https://maf.lviv.ua/wp-json/maf-bet/v1"
    private lateinit var btnPredictionsTable: LinearLayout
    private lateinit var rvPredictionsTable: RecyclerView
    private var screenMode = "predictions"
    private lateinit var tableAdapter: PredictionTableAdapter
    private lateinit var btnRegister: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_predictions, container, false)

        // 1. Ініціалізація всіх View
        tvYearTitle = view.findViewById(R.id.tvYearTitle)
        layoutAuth = view.findViewById(R.id.layoutAuth)
        layoutRules = view.findViewById(R.id.layoutRules)
        rvPredictions = view.findViewById(R.id.rvPredictions)
        rvPredictionsTable = view.findViewById(R.id.rvPredictionsTable)
        rvStages = view.findViewById(R.id.rvStages)
        etLogin = view.findViewById(R.id.etLogin)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnPredictionsTable = view.findViewById(R.id.btnPredictionsTable)
        btnAuthTelegram = view.findViewById(R.id.btnAuthTelegram)
        btnAcceptRules = view.findViewById(R.id.btnAcceptRules)
        btnRegister = view.findViewById(R.id.btnRegister)

        // Перевірка режиму екрану
        if (screenMode == "table") {
            rvStages.visibility = View.GONE
            rvPredictions.visibility = View.GONE
            rvPredictionsTable.visibility = View.VISIBLE
            btnPredictionsTable.visibility = View.GONE
            tvYearTitle.text = "Таблиця учасників"
        }

        predictionAdapter = PredictionAdapter { match, s1, s2 ->
            savePredictionOnServer(match, s1, s2)
        }

        rvPredictions.layoutManager = LinearLayoutManager(requireContext())
        rvPredictions.adapter = predictionAdapter
        rvPredictionsTable.layoutManager = LinearLayoutManager(requireContext())

        // Тимчасові дані для рейтингу
        val tablePlayers = listOf(
            PredictionTablePlayer(1, "admin 👑", 14, 10, 3, 2),
            PredictionTablePlayer(2, "BOGDAN", 12, 9, 2, 3),
            PredictionTablePlayer(3, "bohdan_chekanskyi", 11, 8, 1, 4),
            PredictionTablePlayer(4, "Managershop018", 10, 7, 2, 5)
        )

        tableAdapter = PredictionTableAdapter(tablePlayers) { player ->
            val fragment = PredictionPlayerFragment()
            val bundle = Bundle()
            bundle.putString("username", player.name)
            fragment.arguments = bundle

            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace((requireActivity() as MainActivity).fragmentContainer.id, fragment)
                .addToBackStack(null)
                .commit()
        }

        rvPredictionsTable.adapter = tableAdapter
        rvPredictions.itemAnimator = null

        setupStagesAdapter()

        // Кнопка Назад
        view.findViewById<TextView>(R.id.btnBack).setOnClickListener {
            if (screenMode == "table") {
                screenMode = "predictions"
                rvStages.visibility = View.VISIBLE
                rvPredictions.visibility = View.VISIBLE
                rvPredictionsTable.visibility = View.GONE
                btnPredictionsTable.visibility = View.VISIBLE
                checkAuthState()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        // Авторизація Telegram
        btnAuthTelegram.setOnClickListener { startTelegramAuth() }

        // Авторизація Логін/Пароль
        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (login.isNotEmpty() && password.isNotEmpty()) {
                loginWithCredentials(login, password)
            } else {
                Toast.makeText(context, "Заповніть всі поля!", Toast.LENGTH_SHORT).show()
            }
        }
// Відкриваємо реєстрацію на сайті
        btnRegister.setOnClickListener {
            val url = "https://maf.lviv.ua/register"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        // Перехід до таблиці
        btnPredictionsTable.setOnClickListener {
            screenMode = "table"
            rvStages.visibility = View.GONE
            rvPredictions.visibility = View.GONE
            rvPredictionsTable.visibility = View.VISIBLE
            btnPredictionsTable.visibility = View.GONE
            tvYearTitle.text = "Таблиця учасників"
        }

        // Правила
        btnAcceptRules.setOnClickListener {
            requireActivity()
                .getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("rules_accepted", true)
                .apply()
            checkAuthState()
        }

        checkAuthState()
        return view
    }

    fun refreshData() {
        checkAuthState()
    }

    private fun setupStagesAdapter() {
        stagesAdapter = StagesAdapter { clickedStage ->
            val updated = stagesAdapter.currentList.map {
                it.copy(isSelected = it.id == clickedStage.id)
            }
            stagesAdapter.submitList(updated)
            fetchPredictionsFromApi(clickedStage.id)
        }
        rvStages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvStages.adapter = stagesAdapter
    }

    private fun checkAuthState() {
        val prefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        val rulesAccepted = prefs.getBoolean("rules_accepted", false)
        val username = prefs.getString("tg_username", null)

        layoutAuth.visibility = View.GONE
        layoutRules.visibility = View.GONE
        rvPredictions.visibility = View.GONE
        rvStages.visibility = View.GONE
        btnPredictionsTable.visibility = View.GONE

        if (!rulesAccepted) {
            layoutRules.visibility = View.VISIBLE
            tvYearTitle.text = "ПРАВИЛА ТУРНІРУ"
        } else if (username == null) {
            layoutAuth.visibility = View.VISIBLE
            tvYearTitle.text = "АВТОРИЗАЦІЯ"
        } else {
            rvPredictions.visibility = View.VISIBLE
            rvStages.visibility = View.VISIBLE
            btnPredictionsTable.visibility = View.VISIBLE
            loadDataForYear(AppConfig.selectedYear, username)
        }
    }

    private fun loadDataForYear(year: String, username: String) {
        tvYearTitle.text = "Прогнози ($username). Сезон: $year"
        fetchStagesFromApi()
    }

    private fun loginWithCredentials(user: String, pass: String) {
        val json = JSONObject().apply {
            put("username", user)
            put("password", pass)
        }
        val request = Request.Builder()
            .url("$BASE_URL/login")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread { Toast.makeText(context, "Помилка мережі", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val jsonRes = JSONObject(body)
                if (jsonRes.optString("status") == "success") {
                    val prefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("tg_username", jsonRes.optString("display_name"))
                        .putString("tg_id", jsonRes.optString("user_id"))
                        .apply()
                    requireActivity().runOnUiThread {
                        checkAuthState()
                        Toast.makeText(context, "Вітаємо, ${jsonRes.optString("display_name")}!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requireActivity().runOnUiThread { Toast.makeText(context, "Невірний логін або пароль", Toast.LENGTH_SHORT).show() }
                }
            }
        })
    }

    private fun fetchStagesFromApi() {
        val url = "$BASE_URL/stages?year=${AppConfig.selectedYear}"
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                if (json.optString("status") != "success") return
                val arr = json.getJSONArray("stages")
                val list = mutableListOf<Stage>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Stage(obj.getString("id").toInt(), obj.getString("name"), i == 0))
                }
                if (!isAdded) return
                requireActivity().runOnUiThread {
                    stagesAdapter.submitList(list)
                    if (list.isNotEmpty()) {
                        val currentStageIndex = list.indexOfFirst { it.isSelected }
                        if (currentStageIndex != -1) {
                            rvStages.scrollToPosition(currentStageIndex)
                            fetchPredictionsFromApi(list[currentStageIndex].id)
                        } else {
                            fetchPredictionsFromApi(list.first().id)
                        }
                    }
                }
            }
        })
    }

    private fun fetchPredictionsFromApi(stageId: Int) {
        val prefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        val tgId = prefs.getString("tg_id", "") ?: ""
        val url = "$BASE_URL/matches-for-prediction?year=${AppConfig.selectedYear}&tg_id=$tgId&stage_id=$stageId"
        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val json = JSONObject(body)
                if (json.optString("status") != "success") return
                val matchesArray = json.getJSONArray("matches")
                val matches = mutableListOf<PredictionMatchModel>()
                for (i in 0 until matchesArray.length()) {
                    val obj = matchesArray.getJSONObject(i)
                    matches.add(PredictionMatchModel(
                        obj.optInt("id"), obj.optInt("tournament_id"), obj.optString("team1_name"),
                        obj.optString("team1_logo"), obj.optString("team2_name"), obj.optString("team2_logo"),
                        obj.optString("match_date"), obj.optString("tournament"), obj.optString("stage"),
                        obj.optLong("deadline_timestamp") * 1000L, obj.optString("pred1").ifEmpty { null },
                        obj.optString("pred2").ifEmpty { null }
                    ))
                }
                val grouped = mutableListOf<PredictionListItem>()
                matches.sortedBy { it.deadlineTimestamp }.groupBy { it.stage }.forEach { (stageName, list) ->
                    grouped.add(PredictionListItem.StageHeader(stageName))
                    list.forEach { grouped.add(PredictionListItem.MatchItem(it)) }
                }
                if (!isAdded) return
                requireActivity().runOnUiThread { predictionAdapter.submitList(grouped) }
            }
        })
    }

    private fun savePredictionOnServer(match: PredictionMatchModel, s1: String, s2: String) {
        val prefs = requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE)
        val tgId = prefs.getString("tg_id", "") ?: return
        val json = JSONObject().apply {
            put("tg_id", tgId)
            put("match_id", match.id)
            put("pred1", s1.toInt())
            put("pred2", s2.toInt())
            put("tournament_id", match.tournamentId)
        }
        val request = Request.Builder()
            .url("$BASE_URL/save-prediction")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return
                requireActivity().runOnUiThread { Toast.makeText(context, "✅ Збережено!", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun startTelegramAuth() {
        val code = UUID.randomUUID().toString().substring(0, 8)
        requireActivity().getSharedPreferences("MafPrefs", Context.MODE_PRIVATE).edit()
            .putString("auth_session_code", code).apply()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$TELEGRAM_BOT_URL?start=auth_$code")))
    }
}