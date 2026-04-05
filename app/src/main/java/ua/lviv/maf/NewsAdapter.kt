package ua.lviv.maf

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.app.Activity
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.FullScreenContentCallback
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.interstitial.InterstitialAd
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class NewsAdapter(private val newsList: List<NewsModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // private var mInterstitialAd: InterstitialAd? = null

    // Додаємо ініціалізатор, щоб реклама почала вантажитись відразу при створенні адаптера
    init {
        // Ми не можемо викликати loadAd тут прямо, бо немає context,
        // тому ми покладемося на виклик у onBindViewHolder, який я додав вище.
    }

    // Метод для завантаження реклами всередині адаптера
    private fun loadAd(context: Context) {
        /*
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.ad_unit_id_interstitial), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
        */
    }

    private val itemsWithAds: List<Any> = mutableListOf<Any>().apply {
        val AD_INTERVAL = 3
        var count = 0
        newsList.forEach { item ->
            add(item)
            count++
            // if (count % AD_INTERVAL == 0) add("AD_SLOT")
        }
        // if (!contains("AD_SLOT") && newsList.isNotEmpty()) add("AD_SLOT")
    }

    private val TYPE_NEWS = 0
    private val TYPE_AD = 1

    override fun getItemViewType(position: Int): Int {
        return if (itemsWithAds[position] is String) TYPE_AD else TYPE_NEWS
    }

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNewsTitle)
        val tvPreview: TextView = view.findViewById(R.id.tvNewsPreview)
        val tvDate: TextView = view.findViewById(R.id.tvNewsDate)
    }

    class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // val adView: com.google.android.gms.ads.nativead.NativeAdView = view as com.google.android.gms.ads.nativead.NativeAdView
        val headline: TextView = view.findViewById(R.id.ad_headline)
        val body: TextView = view.findViewById(R.id.ad_body)
        val icon: android.widget.ImageView = view.findViewById(R.id.ad_app_icon)
        val callToAction: android.widget.Button = view.findViewById(R.id.ad_call_to_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_AD) {
            AdViewHolder(inflater.inflate(R.layout.item_native_ad, parent, false))
        } else {
            NewsViewHolder(inflater.inflate(R.layout.item_news, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemOrAd = itemsWithAds[position]

        // ПОЧИНАЄМО ЗАВАНТАЖЕННЯ ВІДРАЗУ
        /*
        if (mInterstitialAd == null) {
            loadAd(holder.itemView.context)
        }
        */

        if (holder is AdViewHolder) {
            /*
            val adLoader = com.google.android.gms.ads.AdLoader.Builder(holder.itemView.context, holder.itemView.context.getString(R.string.ad_unit_id_native))
                .forNativeAd { nativeAd ->
                    holder.headline.text = nativeAd.headline
                    holder.body.text = nativeAd.body
                    holder.callToAction.text = nativeAd.callToAction
                    if (nativeAd.icon != null) {
                        holder.icon.visibility = View.VISIBLE
                        holder.icon.setImageDrawable(nativeAd.icon?.drawable)
                    } else {
                        holder.icon.visibility = View.GONE
                    }
                    holder.adView.headlineView = holder.headline
                    holder.adView.bodyView = holder.body
                    holder.adView.iconView = holder.icon
                    holder.adView.callToActionView = holder.callToAction
                    holder.adView.setNativeAd(nativeAd)
                }.build()
            adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
            */
            return
        }

        if (holder is NewsViewHolder && itemOrAd is NewsModel) {
            holder.tvTitle.text = itemOrAd.title
            holder.tvPreview.text = itemOrAd.preview
            holder.tvDate.visibility = View.GONE

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context // Визначаємо контекст

                // 1. Працюємо з глобальним лічильником
                val prefs = context.getSharedPreferences("MAF_ADS_PREFS", Context.MODE_PRIVATE)
                var currentCount = prefs.getInt("global_click_counter", 0)
                currentCount++
                prefs.edit().putInt("global_click_counter", currentCount).apply()

                // Функція для переходу до новини
                val openNewsAction = {
                    val intent = Intent(context, NewsDetailActivity::class.java).apply {
                        putExtra("NEWS_TITLE", itemOrAd.title)
                        putExtra("NEWS_CONTENT", itemOrAd.content)
                        putExtra("NEWS_DATE", itemOrAd.date)
                    }
                    context.startActivity(intent)
                }

                // Відкриваємо новину відразу
                openNewsAction()

                // 2. Умова на кожен 10-й клік (1, 11, 21...)
                /*
                if (currentCount % 10 == 1 && mInterstitialAd != null) {

                    // ВАЖЛИВО: Створюємо об'єкт слухача
                    mInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            mInterstitialAd = null
                            loadAd(context)
                            openNewsAction()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            mInterstitialAd = null
                            openNewsAction()
                        }
                    }

                    if (context is Activity) {
                        mInterstitialAd?.show(context)
                    } else {
                        openNewsAction()
                    }
                } else {
                    // 3. Якщо реклами немає або не той клік — просто відкриваємо
                    if (mInterstitialAd == null) loadAd(context)
                    openNewsAction()
                }
                */
            }
        }
    }

    override fun getItemCount() = itemsWithAds.size
}