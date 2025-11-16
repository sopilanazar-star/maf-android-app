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

        // Чорний фон, щоб не було білих миготінь
        webView.setBackgroundColor(Color.BLACK)

        // Спочатку ховаємо WebView (буде невидимий)
        webView.alpha = 0f

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
        }

        // Показуємо WebView лише тоді, коли сторінка реально намальована
        webView.webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                // Плавно показуємо сайт без білого спалаху
                webView.animate().alpha(1f).setDuration(200).start()
            }
        }

        // 🔙 Повертаємо пряме завантаження сайту (без loader.html)
        webView.loadUrl("https://maf.lviv.ua")
    }
}
