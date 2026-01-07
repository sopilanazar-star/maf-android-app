package ua.lviv.maf

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class IntroActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var introFinished = false
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MAFFootball)
        
        // Повний екран
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val logo: ImageView = findViewById(R.id.logo)
        val dot1: View = findViewById(R.id.dot1)
        val dot2: View = findViewById(R.id.dot2)
        val dot3: View = findViewById(R.id.dot3)

        startLogoAnimation(logo)
        startDotsAnimation(dot1, dot2, dot3)

        // Ініціалізація перевірки оновлень через Firebase
        setupRemoteConfig()

        // Перевіряємо інтернет через 2 секунди після початку анімації
        handler.postDelayed({
            checkStatusAndProceed()
        }, 2500L)
    }

    private fun setupRemoteConfig() {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Перевірка раз на годину
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("current_version" to 21L)) // 21 - ваш поточний versionCode
    }

    private fun checkStatusAndProceed() {
        if (!isOnline()) {
            showOfflineDialog()
            return
        }

        // Перевіряємо оновлення в Firebase
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            val latestVersion = remoteConfig.getLong("current_version")
            val currentVersion = 21L // Збігається з вашим build.gradle

            if (latestVersion > currentVersion) {
                showUpdateDialog()
            } else {
                goToMain()
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showOfflineDialog() {
        AlertDialog.Builder(this)
            .setTitle("Відсутній інтернет")
            .setMessage("Додаток МАФ потребує підключення для оновлення даних. Перевірте мережу.")
            .setPositiveButton("Повторити") { _, _ -> checkStatusAndProceed() }
            .setNegativeButton("Вийти") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle("Доступне оновлення")
            .setMessage("Вийшла нова версія додатка MAF. Будь ласка, оновіться для стабільної роботи.")
            .setPositiveButton("Завантажити") { _, _ ->
                // Відкриваємо ваш сайт з APK
                val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://maf.lviv.ua"))
                startActivity(browserIntent)
            }
            .setNegativeButton("Пізніше") { _, _ -> goToMain() }
            .setCancelable(false)
            .show()
    }

    private fun goToMain() {
        if (introFinished) return
        introFinished = true
        startActivity(Intent(this, MainActivity::class.java))
        // Додаємо невелику плавну анімацію замість 0, 0 щоб прибрати різкий чорний перехід
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    // Анімації залишаємо як були...
    private fun startLogoAnimation(logo: ImageView) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.08f)
        val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, -4f, 4f)
        ObjectAnimator.ofPropertyValuesHolder(logo, scaleX, scaleY, rotation).apply {
            duration = 900
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun startDotsAnimation(dot1: View, dot2: View, dot3: View) {
        listOf(dot1, dot2, dot3).forEachIndexed { index, view ->
            ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1f).apply {
                duration = 500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                startDelay = index * 150L
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
