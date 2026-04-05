package ua.lviv.maf

import android.app.Activity
import android.content.Context
// Відключаємо рекламні імпорти для версії 2.3
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.FullScreenContentCallback
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.interstitial.InterstitialAd
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdInterceptor {
    // Рекламну змінну відключено
    // private var mInterstitialAd: InterstitialAd? = null

    // 1. Функція завантаження тепер "порожня"
    fun load(context: Context) {
        /* Тимчасово відключено для версії без реклами
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            context.getString(R.string.ad_unit_id_interstitial),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            }
        )
        */
    }

    // 2. Головна функція: тепер просто виконує дію (action)
    fun execute(context: Context, action: () -> Unit) {
        val prefs = context.getSharedPreferences("MAF_ADS_PREFS", Context.MODE_PRIVATE)
        var count = prefs.getInt("global_click_counter", 0)

        count++ // Продовжуємо рахувати кліки в пам'ять
        prefs.edit().putInt("global_click_counter", count).apply()

        // Рекламу відключено: відразу виконуємо перехід
        action()

        /* Стара логіка реклами закоментована для стабільності
        if (count % 10 == 1 && mInterstitialAd != null && context is Activity) {
            // ... (код виклику реклами) ...
        }
        */
    }
}