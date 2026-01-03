package ua.lviv.maf

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

// Імпорт вашого інтерфейсу
import ua.lviv.maf.WebAppInterface

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val START_URL = "https://maf.lviv.ua"
    private val OFFLINE_URL = "file:///android_asset/offline.html"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // SplashScreen API
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Fullscreen + ховаємо статусбар і навігацію
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        webView = WebView(this)
        setContentView(webView)

        // Чорний фон, щоб не було білих миготінь
        webView.setBackgroundColor(Color.BLACK)

        // Спочатку ховаємо WebView для плавної появи
        webView.alpha = 0f

        val settings = webView.settings
        with(settings) {
            javaScriptEnabled = true

            // Підключення JS -> Android
            webView.addJavascriptInterface(
                WebAppInterface(this@MainActivity),
                "Android"
            )

            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // Кешування
            cacheMode = if (isNetworkAvailable()) {
                WebSettings.LOAD_DEFAULT
            } else {
                WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
        }

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                view?.loadUrl(url)
                return true
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) view?.loadUrl(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                webView.animate().alpha(1f).setDuration(200).start()
            }

            @Suppress("DEPRECATION")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showOfflinePage()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showOfflinePage()
                }
            }
        }

        // Стартове завантаження
        if (isNetworkAvailable()) {
            webView.loadUrl(START_URL)
        } else {
            showOfflinePage()
        }
    }

    /**
     * ПРАВКА: Обробка кнопки "Назад".
     * Якщо браузер може повернутися назад — повертаємось.
     * Якщо ми на головній — закриваємо додаток.
     */
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
            val activeNetworkInfo = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}
