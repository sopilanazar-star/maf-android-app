package ua.lviv.maf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.security.MessageDigest

class PlayerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // 1. Кнопка НАЗАД
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Закриває вікно і повертає назад
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
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        val playerPhotoUrl = intent.getStringExtra("PLAYER_PHOTO")
        val playerNumber = intent.getStringExtra("PLAYER_NUMBER") ?: ""
        val positionCode = intent.getStringExtra("PLAYER_POSITION") ?: ""
        
        // Отримуємо команду і лого (які ми передамо з Адаптера)
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "Команда"
        val teamLogoUrl = intent.getStringExtra("TEAM_LOGO") 

        // 4. Заповнення
        tvName.text = playerName
        tvTeam.text = teamName

        // Розшифровка позиції
        val fullPositionName = when (positionCode.lowercase()) {
            "g", "gk" -> "Воротар"
            "d", "df" -> "Захисник"
            "m", "mf" -> "Півзахисник"
            "f", "fw" -> "Нападник"
            else -> positionCode // Якщо код невідомий
        }
        
        val posText = if (playerNumber.isNotEmpty()) "$fullPositionName • #$playerNumber" else fullPositionName
        tvPosition.text = posText
        
        // Дата народження (поки пусто, треба передати дані)
        tvDob.text = "" 

        // 5. Картинки
        Glide.with(this).load(R.drawable.maf_logo).into(ivWatermark)

        if (!playerPhotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(playerPhotoUrl)
                .transform(TopCropCircleTransformation()) 
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
             // Якщо лого немає, ховаємо або ставимо заглушку
             ivTeamLogoSmall.setImageResource(R.drawable.maf_logo) 
        }

        setupTabs()
    }

    private fun setupTabs() {
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        viewPager.adapter = PlayerTabsAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "СТАТИСТИКА"
                1 -> tab.text = "МАТЧІ"
            }
        }.attach()
    }
}

class PlayerTabsAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment = Fragment()
}

class TopCropCircleTransformation : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("top_crop_circle_v2".toByteArray())
    }
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val size = minOf(outWidth, outHeight)
        val y = (toTransform.height * 0.15).toInt() 
        val cropped = Bitmap.createBitmap(toTransform, 0, y, toTransform.width, toTransform.height - y)
        val squared = Bitmap.createScaledBitmap(cropped, size, size, true)
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.isAntiAlias = true
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squared, 0f, 0f, paint)
        return result
    }
}
