package ua.lviv.maf

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Показуємо Splash до готовності першого кадру
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Fullscreen + ховаємо статусбар і навігацію
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val webView = WebView(this)
        setContentView(webView)

        // Чорний фон, щоб не було білого миготіння
        webView.setBackgroundColor(Color.BLACK)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
        }

        webView.webViewClient = WebViewClient()

        // 🔥 Замість прямого заходу на сайт – спочатку локальний лоадер
        webView.loadUrl("file:///android_asset/loader.html")
    }
}
