package ua.lviv.maf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val START_URL = "https://maf.lviv.ua"
    private val OFFLINE_URL = "file:///android_asset/offline.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Прибираємо повний екран тут, щоб бачити статус-бар під час вводу паролів
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        webView = WebView(this)
        setContentView(webView)

        // ВИПРАВЛЕННЯ: Міняємо чорний фон на білий, щоб не було "чорного квадрата"
        webView.setBackgroundColor(Color.WHITE)
        webView.alpha = 0f // Починаємо прозорим для плавного входу
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val settings = webView.settings
        with(settings) {
            javaScriptEnabled = true
            // Підключаємо інтерфейс для взаємодії з сайтом
            webView.addJavascriptInterface(WebAppInterface(this@MainActivity), "Android")
            
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            
            // ОПТИМІЗАЦІЯ ОФЛАЙНУ: Використовуємо кеш, якщо немає мережі
            cacheMode = if (isNetworkAvailable()) WebSettings.LOAD_DEFAULT else WebSettings.LOAD_CACHE_ELSE_NETWORK
            
            // Дозволяємо змішаний контент (https + http)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Коли сторінка повністю готова — плавно показуємо її
                webView.animate().alpha(1f).setDuration(500).start()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return handleUrl(url)
            }

            private fun handleUrl(url: String): Boolean {
                // PDF через Google Viewer
                if (url.lowercase().endsWith(".pdf")) {
                    webView.loadUrl("https://docs.google.com/viewer?embedded=true&url=$url")
                    return true
                }
                // Зовнішні посилання та месенджери
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("tg:") || url.startsWith("viber:") || url.startsWith("whatsapp:")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } catch (e: Exception) { return false }
                }
                
                // Якщо посилання веде на зовнішній сайт — відкриваємо в браузері, 
                // якщо на maf.lviv.ua — всередині додатка
                if (!url.contains("maf.lviv.ua")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }

                return false // Дозволяємо WebView самому вантажити посилання
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    webView.loadUrl(OFFLINE_URL)
                }
            }
        }

        // Запуск
        if (isNetworkAvailable()) {
            webView.loadUrl(START_URL)
        } else {
            // Спроба взяти з кешу або показати офлайн сторінку
            webView.loadUrl(START_URL) 
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) cm.activeNetwork else null
        val caps = cm.getNetworkCapabilities(network)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Вихід")
            .setMessage("Бажаєте вийти з додатка МАФ?")
            .setPositiveButton("Так") { _, _ -> finish() }
            .setNegativeButton("Ні", null)
            .show()
    }
}
