package ua.lviv.maf

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.lviv.maf.api.RetrofitClient
import ua.lviv.maf.models.Player

class PlayerProfileActivity : AppCompatActivity() {

    private lateinit var ivPlayerPhoto: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvTeam: TextView

    private lateinit var ivTeamLogo: ImageView
    private lateinit var tvPosition: TextView
    private lateinit var tvDob: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        ivPlayerPhoto = findViewById(R.id.ivPlayerPhoto)
        tvName = findViewById(R.id.tvPlayerName)
        tvTeam = findViewById(R.id.tvTeamName)
        ivTeamLogo = findViewById(R.id.ivTeamLogoSmall)
        tvPosition = findViewById(R.id.tvPosition)
        tvDob = findViewById(R.id.tvDob)

        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)

        // intent дані
        val playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Команда"
        val teamLogo = intent.getStringExtra("TEAM_LOGO")
        val positionCode = intent.getStringExtra("PLAYER_POSITION") ?: ""
        val playerNumber = intent.getStringExtra("PLAYER_NUMBER") ?: ""
        val birthDate =
            intent.getStringExtra("PLAYER_BIRTHDATE")
                ?: intent.getStringExtra("PLAYER_BIRTH_DATE")
                ?: ""
        val age = intent.getIntExtra("PLAYER_AGE", 0)
        val photoUrl = intent.getStringExtra("PLAYER_PHOTO")

        // placeholder дані
        tvName.text = playerName
        tvTeam.text = teamName
        if (!teamLogo.isNullOrEmpty()) {

            Glide.with(this)
                .load(teamLogo)
                .into(ivTeamLogo)

            Glide.with(this)
                .load(teamLogo)
                .centerInside()
                .dontAnimate()
                .into(ivWatermark)

        } else {

            Glide.with(this)
                .load(R.drawable.maf_logo)
                .centerInside()
                .dontAnimate()
                .into(ivWatermark)

        }

        val fullPositionName = when (positionCode.lowercase()) {
            "g", "gk" -> "Воротар"
            "d", "df" -> "Захисник"
            "m", "mf" -> "Півзахисник"
            "f", "fw" -> "Нападник"
            else -> positionCode
        }

        tvPosition.text =
            if (playerNumber.isNotEmpty())
                "$fullPositionName • #$playerNumber"
            else fullPositionName

        tvDob.text = when {
            birthDate.isNotEmpty() && age > 0 -> "$birthDate (${formatAge(age)})"
            birthDate.isNotEmpty() -> birthDate
            else -> "Дата народження невідома"
        }

        if (!photoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(photoUrl)
                .transform(PlayerTopCropTransformation())
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(ivPlayerPhoto)
        }

        // 🔥 головне — завжди підтягуємо повний профіль
        if (playerId.isNotEmpty()) {
            loadFullPlayer(playerId)
        }

        setupTabs(playerId)
    }

    private fun loadFullPlayer(playerId: String) {
        RetrofitClient.instance.getPlayerProfile(playerId)
            .enqueue(object : Callback<Player> {
                override fun onResponse(call: Call<Player>, response: Response<Player>) {
                    // ЛОГ 1: Перевіряємо статус відповіді (має бути 200)
                    android.util.Log.d("MAF_DEBUG", "Запит успішний? ${response.isSuccessful}. Код: ${response.code()}")

                    if (!response.isSuccessful) {
                        android.util.Log.e("MAF_DEBUG", "Помилка сервера: ${response.errorBody()?.string()}")
                        return
                    }

                    val p = response.body()
                    if (p == null) {
                        android.util.Log.e("MAF_DEBUG", "Тіло відповіді NULL")
                        return
                    }

                    // ЛОГ 2: Виводимо ВСЕ, що Gson зміг прочитати з JSON
                    android.util.Log.d("MAF_DEBUG", "--- ДАНІ ГРАВЦЯ З СЕРВЕРА ---")
                    android.util.Log.d("MAF_DEBUG", "ID: ${p.id}")
                    android.util.Log.d("MAF_DEBUG", "Ім'я: ${p.name}")
                    android.util.Log.d("MAF_DEBUG", "Команда (team_name): ${p.team_name}")
                    android.util.Log.d("MAF_DEBUG", "Лого (team_logo): ${p.team_logo}")
                    android.util.Log.d("MAF_DEBUG", "Дата (birthDate): ${p.birthDate}")
                    android.util.Log.d("MAF_DEBUG", "Фото (photo): ${p.photo}")
                    android.util.Log.d("MAF_DEBUG", "----------------------------")

                    runOnUiThread {

                        if (!p.name.isNullOrEmpty()) tvName.text = p.name

                        // позиція + номер
                        val posCode = p.position ?: ""
                        val num = p.number ?: ""

                        val posName = when (posCode.lowercase()) {
                            "g", "gk" -> "Воротар"
                            "d", "df" -> "Захисник"
                            "m", "mf" -> "Півзахисник"
                            "f", "fw" -> "Нападник"
                            else -> posCode
                        }

                        tvPosition.text =
                            if (num.isNotEmpty())
                                "$posName • #$num"
                            else posName

                        // дата
                        val bDate = p.birthDate ?: ""
                        if (bDate.isNotEmpty()) {
                            val ageVal = p.age ?: 0
                            val ageStr = if (ageVal > 0) " (${formatAge(ageVal)})" else ""
                            tvDob.text = "$bDate$ageStr"
                        }

                        // команда
                        if (!p.team_name.isNullOrEmpty()) {
                            tvTeam.text = p.team_name
                        }

                        // фото
                        if (!p.photo.isNullOrEmpty()) {
                            Glide.with(this@PlayerProfileActivity)
                                .load(p.photo?.replace("http://", "https://"))
                                .transform(PlayerTopCropTransformation())
                                .into(ivPlayerPhoto)
                        }

                        // логотип
                        if (!p.team_logo.isNullOrEmpty()) {

                            val logoUrl = p.team_logo.replace("http://", "https://")

                            Glide.with(this@PlayerProfileActivity)
                                .load(logoUrl)
                                .into(ivTeamLogo)

                            Glide.with(this@PlayerProfileActivity)
                                .load(logoUrl)
                                .centerInside()
                                .into(findViewById(R.id.ivWatermark))
                        }
                    }
                }

                override fun onFailure(call: Call<Player>, t: Throwable) {
                    android.util.Log.e("MAF_DEBUG", "Помилка запиту: ${t.message}")
                }
            })
    }
    private fun formatAge(age: Int): String {
        val mod100 = age % 100
        val mod10 = age % 10

        return when {
            mod100 in 11..14 -> "$age років"
            mod10 == 1 -> "$age рік"
            mod10 in 2..4 -> "$age роки"
            else -> "$age років"
        }
    }
    private fun setupTabs(playerId: String) {
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        viewPager.adapter = PlayerTabsAdapter(this, playerId)

        TabLayoutMediator(tabLayout, viewPager) { tab, positionIndex ->
            tab.text = if (positionIndex == 0) "СТАТИСТИКА" else "МАТЧІ"
        }.attach()
    }
}

class PlayerTabsAdapter(
    activity: AppCompatActivity,
    private val playerId: String
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(positionIndex: Int): Fragment {
        return when (positionIndex) {
            0 -> PlayerStatsFragment.newInstance(playerId, "")
            1 -> PlayerMatchesFragment.newInstance(playerId)
            else -> Fragment()
        }
    }
}
