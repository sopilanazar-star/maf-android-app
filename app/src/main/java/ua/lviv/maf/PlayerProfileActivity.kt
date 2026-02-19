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

class PlayerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // 1. Кнопка НАЗАД
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 2. Елементи
        val ivWatermark: ImageView = findViewById(R.id.ivWatermark)
        val ivPlayerPhoto: ImageView = findViewById(R.id.ivPlayerPhoto)
        val tvName: TextView = findViewById(R.id.tvPlayerName)
        val tvTeam: TextView = findViewById(R.id.tvTeamName)
        val ivTeamLogoSmall: ImageView = findViewById(R.id.ivTeamLogoSmall)
        val tvPosition: TextView = findViewById(R.id.tvPosition)
        val tvDob: TextView = findViewById(R.id.tvDob)

        // 3. Отримання даних
        val playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        val playerPhotoUrl = intent.getStringExtra("PLAYER_PHOTO")
        val playerNumber = intent.getStringExtra("PLAYER_NUMBER") ?: ""
        val positionCode = intent.getStringExtra("PLAYER_POSITION") ?: ""
        
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Команда"
        val teamLogoUrl = intent.getStringExtra("TEAM_LOGO") 

        val birthDate = intent.getStringExtra("PLAYER_BIRTHDATE") ?: ""
        val age = intent.getIntExtra("PLAYER_AGE", 0)

        // 4. Заповнення
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
            Glide.with(this)
                .load(teamLogoUrl)
                .fitCenter()
                .placeholder(R.drawable.maf_logo)
                .into(ivTeamLogoSmall)
        } else {
             ivTeamLogoSmall.setImageResource(R.drawable.maf_logo) 
        }

        setupTabs(playerId)
    }

    private fun setupTabs(playerId: String) {
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = PlayerTabsAdapter(this, playerId)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "СТАТИСТИКА"
                1 -> tab.text = "МАТЧІ"
            }
        }.attach()
    }
}

class PlayerTabsAdapter(activity: AppCompatActivity, private val playerId: String) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Fragment() 
            1 -> PlayerMatchesFragment.newInstance(playerId) 
            else -> Fragment()
        }
    }
}
