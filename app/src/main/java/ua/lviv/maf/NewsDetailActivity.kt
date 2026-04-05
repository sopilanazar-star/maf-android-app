package ua.lviv.maf

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
// Імпорти реклами відключено для версії 2.3
// import com.google.android.gms.ads.AdLoader
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.nativead.NativeAdView

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val btnBack: ImageButton = findViewById(R.id.btnNewsBack)
        val tvHeader: TextView = findViewById(R.id.tvNewsDetailHeader)
        val tvContent: TextView = findViewById(R.id.tvFullContent)

        btnBack.setOnClickListener { finish() }

        // Отримуємо дані
        val title = intent.getStringExtra("NEWS_TITLE") ?: "Новина"
        val content = intent.getStringExtra("NEWS_CONTENT") ?: ""

        tvHeader.text = title

        // Відображаємо текст (спрощено без зайвих if)
        tvContent.text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)

        // Завантаження реклами відключено
        // loadNativeAd()
    }

    private fun loadNativeAd() {
        /* Логіку завантаження нативної реклами закоментовано, щоб не було помилок
        val adContainer = findViewById<FrameLayout>(R.id.ad_container)

        val adLoader = AdLoader.Builder(this, getString(R.string.ad_unit_id_native))
            .forNativeAd { nativeAd ->
                // Інфлейтимо макет правильно (без null)
                val adView = LayoutInflater.from(this)
                    .inflate(R.layout.item_native_ad, adContainer, false) as NativeAdView

                adView.findViewById<TextView>(R.id.ad_headline).text = nativeAd.headline
                adView.findViewById<TextView>(R.id.ad_body).text = nativeAd.body
                adView.findViewById<Button>(R.id.ad_call_to_action).text = nativeAd.callToAction

                val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
                if (nativeAd.icon != null) {
                    iconView.setImageDrawable(nativeAd.icon?.drawable)
                } else {
                    iconView.visibility = View.GONE
                }

                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.iconView = iconView
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

                adView.setNativeAd(nativeAd)
                adContainer.removeAllViews()
                adContainer.addView(adView)
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
        */
    }
}