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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val START_URL = "https://maf.lviv.ua"
    private val OFFLINE_URL = "file:///android_asset/offline.html"
    
    // Пряме RAW посилання на ваш файл конфігурації
    private val VERSION_JSON_URL = "https://raw.githubusercontent.com/sopilanazar-star/maf-android-app/main/version.json"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.animate().alpha(1f).setDuration(500).start()
                
                // Перевіряємо наявність оновлень, коли основна сторінка завантажилась
                if (isNetworkAvailable()) {
                    checkUpdate()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return handleUrl(url)
            }

            private fun handleUrl(url: String): Boolean {
                if (url.lowercase().endsWith(".pdf")) {
                    webView.loadUrl("https://docs.google.com/viewer?embedded=true&url=$url")
                    return true
                }
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("tg:") || url.startsWith("viber:") || url.startsWith("whatsapp:")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
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
                if (request?.isForMainFrame == true) {
                    webView.loadUrl(OFFLINE_URL)
                }
            }
        }

        webView.loadUrl(START_URL)
    }

    // --- ФУНКЦІЯ ПЕРЕВІРКИ ОНОВЛЕНЬ ---
    private fun checkUpdate() {
        val client = OkHttpClient()
        val request = Request.Builder().url(VERSION_JSON_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    try {
                        val json = JSONObject(jsonString)
                        val newVersionCode = json.getInt("new_version_code")
                        val downloadUrl = json.getString("download_url")

                        // Порівнюємо з versionCode з вашого build.gradle.kts
                        if (newVersionCode > BuildConfig.VERSION_CODE) {
                            runOnUiThread {
                                showUpdateDialog(downloadUrl)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun showUpdateDialog(downloadUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Доступне оновлення")
            .setMessage("Ми випустили нову версію МАФ з новими можливостями. Бажаєте оновитися?")
            .setCancelable(false) // Користувач має прийняти рішення
            .setPositiveButton("Оновити") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                startActivity(intent)
            }
            .setNegativeButton("Пізніше", null)
            .show()
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
