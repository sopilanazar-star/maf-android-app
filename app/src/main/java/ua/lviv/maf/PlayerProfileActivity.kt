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
    private lateinit var tvPosition: TextView
    private lateinit var tvDob: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        ivPlayerPhoto = findViewById(R.id.ivPlayerPhoto)
        tvName = findViewById(R.id.tvPlayerName)
        tvTeam = findViewById(R.id.tvTeamName)
        tvPosition = findViewById(R.id.tvPosition)
        tvDob = findViewById(R.id.tvDob)

        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)
        Glide.with(this).load(R.drawable.maf_logo).into(ivWatermark)

        // intent дані
        val playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Команда"
        val positionCode = intent.getStringExtra("PLAYER_POSITION") ?: ""
        val playerNumber = intent.getStringExtra("PLAYER_NUMBER") ?: ""
        val birthDate = intent.getStringExtra("PLAYER_BIRTHDATE") ?: ""
        val age = intent.getIntExtra("PLAYER_AGE", 0)
        val photoUrl = intent.getStringExtra("PLAYER_PHOTO")

        // placeholder дані
        tvName.text = playerName
        tvTeam.text = teamName

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
            birthDate.isNotEmpty() && age > 0 -> "$birthDate ($age років)"
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
                    if (!response.isSuccessful) return
                    val p = response.body() ?: return

                    runOnUiThread {

                        // ім'я
                        if (p.name.isNotEmpty()) {
                            tvName.text = p.name
                        }

                        // дата народження
                        if (!p.birthDate.isNullOrEmpty()) {
                            val ageText =
                                if ((p.age ?: 0) > 0) " (${p.age} років)" else ""
                            tvDob.text = "${p.birthDate}$ageText"
                        }

                        // позиція
                        val remotePos = when (p.position.lowercase()) {
                            "g", "gk" -> "Воротар"
                            "d", "df" -> "Захисник"
                            "m", "mf" -> "Півзахисник"
                            "f", "fw" -> "Нападник"
                            else -> p.position
                        }

                        if (remotePos.isNotEmpty()) {
                            tvPosition.text =
                                if (p.number.isNotEmpty())
                                    "$remotePos • #${p.number}"
                                else remotePos
                        }

                        // фото
                        if (p.photo.isNotEmpty()) {
                            Glide.with(this@PlayerProfileActivity)
                                .load(p.photo)
                                .transform(PlayerTopCropTransformation())
                                .placeholder(android.R.drawable.ic_menu_camera)
                                .into(ivPlayerPhoto)
                        }
                    }
                }

                override fun onFailure(call: Call<Player>, t: Throwable) {
                    t.printStackTrace()
                }
            })
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
