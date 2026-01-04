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
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        webView = WebView(this)
        setContentView(webView)

        webView.setBackgroundColor(Color.BLACK)
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
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                webView.animate().alpha(1f).setDuration(400).start()
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
                if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("tg:") || url.startsWith("viber:")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    } catch (e: Exception) { return false }
                }
                webView.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) webView.loadUrl(OFFLINE_URL)
            }
        }

        if (isNetworkAvailable()) {
            webView.loadUrl(START_URL)
        } else {
            webView.loadUrl(OFFLINE_URL)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Вихід")
            .setMessage("Бажаєте вийти з додатка?")
            .setPositiveButton("Так") { _, _ -> finish() }
            .setNegativeButton("Ні") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) cm.activeNetwork else null
        val caps = if (network != null) cm.getNetworkCapabilities(network) else null
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }
}
