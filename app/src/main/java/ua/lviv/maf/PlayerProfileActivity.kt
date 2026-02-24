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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // 1. Кнопка НАЗАД
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 2. Елементи
        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)
        val ivPlayerPhoto: ImageView = findViewById(R.id.ivPlayerPhoto)
        val tvName: TextView = findViewById(R.id.tvPlayerName)
        val tvTeam: TextView = findViewById(R.id.tvTeamName)
        val ivTeamLogoSmall: ImageView = findViewById(R.id.ivTeamLogoSmall)
        val tvPosition: TextView = findViewById(R.id.tvPosition)
        val tvDob: TextView = findViewById(R.id.tvDob)

        // 3. Отримання даних з Intent (ТВІЙ ОРИГІНАЛЬНИЙ КОД)
        val playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        val playerPhotoUrl = intent.getStringExtra("PLAYER_PHOTO")
        val playerNumber = intent.getStringExtra("PLAYER_NUMBER") ?: ""
        val positionCode = intent.getStringExtra("PLAYER_POSITION") ?: ""
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Команда"
        val teamLogoUrl = intent.getStringExtra("TEAM_LOGO") 
        val birthDate = intent.getStringExtra("PLAYER_BIRTHDATE") ?: ""
        val age = intent.getIntExtra("PLAYER_AGE", 0)

        // 4. Заповнення (ТВІЙ ОРИГІНАЛЬНИЙ КОД)
        tvName.text = playerName
        tvTeam.text = teamName

        val fullPositionName = when (positionCode.lowercase()) {
            "g", "gk" -> "Воротар"
            "d", "df" -> "Захисник"
            "m", "mf" -> "Півзахисник"
            "f", "fw" -> "Нападник"
            else -> positionCode
        }
        val posText = if (playerNumber.isNotEmpty()) "$fullPositionName • #$playerNumber" else fullPositionName
        tvPosition.text = posText
        
        if (birthDate.isNotEmpty() && age > 0) {
            tvDob.text = "$birthDate ($age років)"
        } else if (birthDate.isNotEmpty()) {
            tvDob.text = birthDate
        } else {
            // Якщо дн порожня (як у дискваліфікаціях), поки пишемо це
            tvDob.text = "Дата народження невідома"
        }

        // 5. Картинки
        Glide.with(this).load(R.drawable.maf_logo).into(ivWatermark)

        if (!playerPhotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(playerPhotoUrl)
                .transform(PlayerTopCropTransformation()) 
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(ivPlayerPhoto)
        }

        if (!teamLogoUrl.isNullOrEmpty()) {
            Glide.with(this).load(teamLogoUrl).fitCenter().placeholder(R.drawable.maf_logo).into(ivTeamLogoSmall)
        }

        // 🔥 ДОДАТОК: Тільки якщо ми прийшли з екрана, де НЕМАЄ дати (Disqualified)
        // ми робимо запит, щоб ДОПОВНИТИ інформацію, а не замінити її.
        if (birthDate.isEmpty() && playerId.isNotEmpty()) {
            RetrofitClient.instance.getPlayerProfile(playerId).enqueue(object : Callback<Player> {
                override fun onResponse(call: Call<Player>, response: Response<Player>) {
                    if (response.isSuccessful) {
                        val p = response.body() ?: return
                        
                        // Довантажуємо ТІЛЬКИ те, чого не було
                        if (tvDob.text == "Дата народження невідома" && !p.birthDate.isNullOrEmpty()) {
                            val ageText = if ((p.age ?: 0) > 0) " (${p.age} років)" else ""
                            tvDob.text = "${p.birthDate}$ageText"
                        }
                        
                        if (tvPosition.text.isNullOrEmpty() || tvPosition.text == positionCode) {
                             val remotePos = when (p.position?.lowercase()) {
                                "g", "gk" -> "Воротар"
                                "d", "df" -> "Захисник"
                                "m", "mf" -> "Півзахисник"
                                "f", "fw" -> "Нападник"
                                else -> p.position ?: ""
                            }
                            tvPosition.text = if (!p.number.isNullOrEmpty()) "$remotePos • #${p.number}" else remotePos
                        }

                        if (playerPhotoUrl.isNullOrEmpty() && !p.photo.isNullOrEmpty()) {
                            Glide.with(this@PlayerProfileActivity).load(p.photo).transform(PlayerTopCropTransformation()).into(ivPlayerPhoto)
                        }
                    }
                }
                override fun onFailure(call: Call<Player>, t: Throwable) {}
            })
        }

        setupTabs(playerId, positionCode)
    }

    private fun setupTabs(playerId: String, position: String) {
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = PlayerTabsAdapter(this, playerId, position)
        TabLayoutMediator(tabLayout, viewPager) { tab, positionIndex ->
            tab.text = if (positionIndex == 0) "СТАТИСТИКА" else "МАТЧІ"
        }.attach()
    }
}

class PlayerTabsAdapter(activity: AppCompatActivity, private val playerId: String, private val position: String) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(positionIndex: Int): Fragment {
        return when (positionIndex) {
            0 -> PlayerStatsFragment.newInstance(playerId, position)
            1 -> PlayerMatchesFragment.newInstance(playerId)
            else -> Fragment()
        }
    }
}
