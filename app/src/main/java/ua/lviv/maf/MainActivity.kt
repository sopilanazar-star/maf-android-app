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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import ua.lviv.maf.WebAppInterface

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val START_URL = "https://maf.lviv.ua"
    private val OFFLINE_URL = "file:///android_asset/offline.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        webView = WebView(this)
        setContentView(webView)

        webView.setBackgroundColor(Color.BLACK)
        webView.alpha = 0f

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

            cacheMode = if (isNetworkAvailable()) {
                WebSettings.LOAD_DEFAULT
            } else {
                WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
        }

        webView.webViewClient = object : WebViewClient() {

            // Основний обробник посилань
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return handleUrl(url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return if (url != null) handleUrl(url) else false
            }

            // Логіка фільтрації посилань (PDF, Месенджери, Сайти)
            private fun handleUrl(url: String): Boolean {
                // 1. Якщо це PDF
                if (url.lowercase().endsWith(".pdf")) {
                    val googleDocsUrl = "https://docs.google.com/viewer?embedded=true&url=$url"
                    webView.loadUrl(googleDocsUrl)
                    return true
                }

                // 2. Якщо це зовнішні сервіси (телефон, месенджери, пошта)
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("intent:") || url.startsWith("tg:") || url.startsWith("viber:")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }

                // 3. Звичайний перехід по сайту
                webView.loadUrl(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                webView.animate().alpha(1f).setDuration(200).start()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) showOfflinePage()
            }
        }

        if (isNetworkAvailable()) {
            webView.loadUrl(START_URL)
        } else {
            showOfflinePage()
        }
    }

    // Кнопка назад тепер працює і для PDF (бо це крок в історії)
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun showOfflinePage() {
        webView.loadUrl(OFFLINE_URL)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val info = cm.activeNetworkInfo
            info != null && info.isConnected
        }
    }
}
