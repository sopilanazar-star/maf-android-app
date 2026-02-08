package ua.lviv.maf

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    
    private val START_URL = "https://maf.lviv.ua"
    private val OFFLINE_URL = "file:///android_asset/offline.html"
    private val VERSION_JSON_URL = "https://raw.githubusercontent.com/sopilanazar-star/maf-android-app/main/version.json"
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setFullScreen()

        // Створюємо головний контейнер (вертикальний)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Контейнер для контенту (WebView та RecyclerView)
        val contentFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f // Займає весь вільний простір
            )
        }

        // 1. Налаштовуємо WebView
        webView = WebView(this).apply {
            setBackgroundColor(Color.WHITE)
            alpha = 0f
        }
        setupWebViewSettings()
        setupWebViewClient()

        // 2. Налаштовуємо RecyclerView (Нативний список)
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
        }

        // 3. Налаштовуємо Bottom Navigation
        bottomNav = BottomNavigationView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Важливо: файл menu/bottom_nav_menu.xml вже має бути створений
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(Color.WHITE)
            
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        recyclerView.visibility = View.GONE
                        webView.visibility = View.VISIBLE
                        true
                    }
                    R.id.nav_tables -> {
                        webView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        true
                    }
                    R.id.nav_matches -> {
                        // Тут буде логіка для матчів пізніше
                        true
                    }
                    else -> false
                }
            }
        }

        // Збираємо ієрархію
        contentFrame.addView(webView)
        contentFrame.addView(recyclerView)
        
        mainLayout.addView(contentFrame)
        mainLayout.addView(bottomNav)
        
        setContentView(mainLayout)

        if (isNetworkAvailable()) {
            webView.loadUrl(START_URL)
            loadNativeData()
            checkUpdate()
        } else {
            webView.loadUrl(OFFLINE_URL)
        }
    }

    private fun setupWebViewSettings() {
        val settings = webView.settings
        with(settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            cacheMode = if (isNetworkAvailable()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    private fun setupWebViewClient() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                val hideStyle = "javascript:(function() { " +
                        "var style = document.getElementById('maf-hide-style');" +
                        "if (!style) {" +
                        "style = document.createElement('style');" +
                        "style.id = 'maf-hide-style';" +
                        "style.innerHTML = 'header, footer, .maf-article:first-of-type { display: none !important; }';" +
                        "document.head.appendChild(style);" +
                        "}" +
                        "})()"
                view?.loadUrl(hideStyle)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.animate().alpha(1f).setDuration(400).start()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.lowercase().endsWith(".pdf")) {
                    webView.loadUrl("https://docs.google.com/viewer?embedded=true&url=$url")
                    return true
                }
                if (!url.contains("maf.lviv.ua")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }
                return false
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) { webView.loadUrl(OFFLINE_URL) }
            }
        }
    }

    private fun loadNativeData() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    try {
                        val jsonObject = JSONObject(jsonString)
                        val futsalStats = jsonObject.getJSONObject("futsal").getJSONObject("stats")
                        val tournamentList = mutableListOf<TournamentRow>()

                        val years = listOf("2026", "2025")
                        for (year in years) {
                            if (futsalStats.has(year)) {
                                val data = futsalStats.getJSONArray(year)
                                tournamentList.add(TournamentRow(year, data.getString(0)))
                            }
                        }

                        runOnUiThread {
                            recyclerView.adapter = TournamentAdapter(tournamentList)
                        }
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        })
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "maf_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "MAF", NotificationManager.IMPORTANCE_LOW))
        }
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(message).setAutoCancel(true)
        manager.notify(100, builder.build())
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun checkUpdate() {
        val client = OkHttpClient()
        val request = Request.Builder().url(VERSION_JSON_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    try {
                        val json = JSONObject(jsonString)
                        if (json.getInt("new_version_code") > BuildConfig.VERSION_CODE) {
                            runOnUiThread { showUpdateDialog(json.getString("download_url")) }
                        }
                    } catch (e: Exception) {}
                }
            }
        })
    }

    private fun showUpdateDialog(url: String) {
        AlertDialog.Builder(this).setTitle("Доступне оновлення").setMessage("Бажаєте оновити додаток?")
            .setPositiveButton("Оновити") { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .setNegativeButton("Пізніше", null).show()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) cm.getNetworkCapabilities(cm.activeNetwork) else null
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    override fun onBackPressed() {
        if (recyclerView.visibility == View.VISIBLE) {
            bottomNav.selectedItemId = R.id.nav_home
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this).setTitle("Вихід").setMessage("Вийти з додатка?")
            .setPositiveButton("Так") { _, _ -> finish() }.setNegativeButton("Ні", null).show()
    }
}
