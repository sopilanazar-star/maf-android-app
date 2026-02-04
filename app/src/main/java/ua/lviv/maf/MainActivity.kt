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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val START_URL = "https://maf.lviv.ua"
    private val OFFLINE_URL = "file:///android_asset/offline.html"
    private val VERSION_JSON_URL = "https://raw.githubusercontent.com/sopilanazar-star/maf-android-app/main/version.json"
    // Новий API для турнірів
    private val MAF_API_URL = "https://maf.lviv.ua/wp-json/maf/v1/tables-data"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setFullScreen()

        webView = WebView(this)
        setContentView(webView)

        webView.setBackgroundColor(Color.WHITE)
        webView.alpha = 0f 
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val settings = webView.settings
        with(settings) {
            javaScriptEnabled = true
            webView.addJavascriptInterface(WebAppInterface(this@MainActivity), "Android")
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = if (isNetworkAvailable()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // КРОК 1: Приховуємо статтю миттєво
                val hideStyle = "javascript:(function() { " +
                        "var style = document.getElementById('maf-hide-style');" +
                        "if (!style) {" +
                        "style = document.createElement('style');" +
                        "style.id = 'maf-hide-style';" +
                        "style.innerHTML = '.maf-article:has(.maf-title:contains(\"Додаток МАФ\")), .maf-article:first-of-type, header, footer { display: none !important; opacity: 0 !important; visibility: hidden !important; }';" +
                        "document.head.appendChild(style);" +
                        "}" +
                        "})()"
                view?.loadUrl(hideStyle)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                
                webView.postDelayed({
                    // КРОК 2: Видаляємо статтю та елементи меню через JS
                    view?.evaluateJavascript("""
                        (function() {
                            document.querySelectorAll('header, footer, .menu-toggle').forEach(el => el.style.display = 'none');
                            var articles = document.querySelectorAll('.maf-article');
                            articles.forEach(function(article) {
                                var title = article.querySelector('.maf-title');
                                if (title && title.innerText.includes('Додаток МАФ')) {
                                    article.remove();
                                }
                            });
                        })();
                    """.trimIndent(), null)
                    
                    // КРОК 3: Плавно показуємо сайт
                    webView.animate().alpha(1f).setDuration(400).start()
                }, 400)
                
                if (isNetworkAvailable()) {
                    checkUpdate()      // Твоя перевірка версії додатка
                    checkMafApiUpdates() // Наша нова перевірка даних турнірів
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return handleUrl(request?.url.toString())
            }

            private fun handleUrl(url: String): Boolean {
                if (url.lowercase().endsWith(".pdf")) {
                    webView.loadUrl("https://docs.google.com/viewer?embedded=true&url=$url")
                    return true
                }
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("tg:") || url.startsWith("viber:") || url.startsWith("whatsapp:")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    } catch (e: Exception) { return false }
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

        webView.loadUrl(START_URL)
    }

    // НОВА ФУНКЦІЯ: Перевірка оновлень турнірів через API
    private fun checkMafApiUpdates() {
        val client = OkHttpClient()
        val request = Request.Builder().url(MAF_API_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { 
                    if (it.contains("2026")) { 
                        runOnUiThread { showNotification("МАФ", "Оновлено турнірні таблиці 2026!") }
                    }
                }
            }
        })
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "maf_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "MAF", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
        manager.notify(100, builder.build())
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) setFullScreen() }

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

    override fun onBackPressed() { if (webView.canGoBack()) webView.goBack() else showExitDialog() }

    private fun showExitDialog() {
        AlertDialog.Builder(this).setTitle("Вихід").setMessage("Вийти з додатка?")
            .setPositiveButton("Так") { _, _ -> finish() }.setNegativeButton("Ні", null).show()
    }
}
