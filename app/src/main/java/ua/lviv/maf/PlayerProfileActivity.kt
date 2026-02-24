package ua.lviv.maf

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    // Оголошуємо змінні на рівні класу, щоб мати до них доступ із мережевого запиту
    private lateinit var ivPlayerPhoto: ImageView
    private lateinit var tvPosition: TextView
    private lateinit var tvDob: TextView
    private lateinit var ivTeamLogoSmall: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // 1. Кнопка НАЗАД
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // 2. Елементи
        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)
        ivPlayerPhoto = findViewById(R.id.ivPlayerPhoto)
        val tvName: TextView = findViewById(R.id.tvPlayerName)
        val tvTeam: TextView = findViewById(R.id.tvTeamName)
        ivTeamLogoSmall = findViewById(R.id.ivTeamLogoSmall)
        tvPosition = findViewById(R.id.tvPosition)
        tvDob = findViewById(R.id.tvDob)

        // 3. Отримання даних з Intent
        val playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Команда"
        
        // Початкове заповнення (те, що вже знаємо)
        tvName.text = playerName
        tvTeam.text = teamName

        // 4. Завантаження фонового вотермарка
        Glide.with(this).load(R.drawable.maf_logo).into(ivWatermark)

        // 5. 🔥 КЛЮЧОВА ПРАВКА: Якщо у нас є ID, вантажимо повну біометрію з сервера
        if (playerId.isNotEmpty()) {
            loadFullPlayerInfo(playerId)
        }

        // Передаємо ID у вкладки (статистика і матчі працюватимуть як раніше)
        setupTabs(playerId, intent.getStringExtra("PLAYER_POSITION") ?: "")
    }

    private fun loadFullPlayerInfo(id: String) {
        // Викликаємо твій API (метод getPlayerProfile має бути в ApiService)
        RetrofitClient.instance.getPlayerProfile(id).enqueue(object : Callback<Player> {
            override fun onResponse(call: Call<Player>, response: Response<Player>) {
                if (response.isSuccessful) {
                    val p = response.body() ?: return
                    updateUI(p)
                }
            }

            override fun onFailure(call: Call<Player>, t: Throwable) {
                // Якщо впав інет, просто нічого не міняємо
            }
        })
    }

    private fun updateUI(p: Player) {
        // Оновлюємо позицію та номер
        val fullPositionName = when (p.position.lowercase()) {
            "g", "gk" -> "Воротар"
            "d", "df" -> "Захисник"
            "m", "mf" -> "Півзахисник"
            "f", "fw" -> "Нападник"
            else -> p.position
        }
        val posText = if (!p.number.isNullOrEmpty()) "$fullPositionName • #${p.number}" else fullPositionName
        tvPosition.text = posText

        // Оновлюємо дату народження та вік
        if (!p.birthDate.isNullOrEmpty() && (p.age ?: 0) > 0) {
            tvDob.text = "${p.birthDate} (${p.age} років)"
        } else {
            tvDob.text = p.birthDate ?: "Дата народження невідома"
        }

        // Завантажуємо фото гравця
        if (!p.photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(p.photo)
                .transform(PlayerTopCropTransformation()) 
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(ivPlayerPhoto)
        }
        
        // Якщо потрібно оновити лого команди (якщо воно є в моделі Player)
        // Glide.with(this).load(p.teamLogo).into(ivTeamLogoSmall)
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

// Адаптер залишається без змін
class PlayerTabsAdapter(
    activity: AppCompatActivity, 
    private val playerId: String, 
    private val position: String
) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(positionIndex: Int): Fragment {
        return when (positionIndex) {
            0 -> PlayerStatsFragment.newInstance(playerId, position)
            1 -> PlayerMatchesFragment.newInstance(playerId)
            else -> Fragment()
        }
    }
}
