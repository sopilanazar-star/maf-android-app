package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Додані імпорти для реклами (відключено)
// import com.google.android.gms.ads.AdLoader
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.nativead.NativeAdView

class GroupAdapter(
    private var groups: List<GroupTable>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemsWithAds: List<Any> = emptyList()

    private val TYPE_GROUP = 0
    private val TYPE_AD = 1

    init {
        updateItemsWithAds()
    }

    private fun updateItemsWithAds() {
        itemsWithAds = mutableListOf<Any>().apply {
            groups.forEach { group ->
                add(group)
                // Відключаємо додавання слотів реклами
                // add("AD_SLOT")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemsWithAds[position] is String) TYPE_AD else TYPE_GROUP
    }

    inner class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupTitle: TextView = view.findViewById(R.id.tvGroupTitle)
        val rvStandingLeft: RecyclerView = view.findViewById(R.id.rvStandingLeft)
        val rvStandingRight: RecyclerView = view.findViewById(R.id.rvStandingRight)

        init {
            rvStandingLeft.layoutManager = LinearLayoutManager(view.context)
            rvStandingRight.layoutManager = LinearLayoutManager(view.context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_AD) {
            AdViewHolder(inflater.inflate(R.layout.item_native_ad, parent, false))
        } else {
            GroupViewHolder(inflater.inflate(R.layout.item_group_card, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemOrAd = itemsWithAds[position]

        if (holder is AdViewHolder) {
            // Рекламу відключено, нічого не завантажуємо
            /*
            val adLoader = AdLoader.Builder(holder.itemView.context, holder.itemView.context.getString(R.string.ad_unit_id_native))
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
            adLoader.loadAd(AdRequest.Builder().build())
            */
            return
        }

        if (holder is GroupViewHolder && itemOrAd is GroupTable) {
            holder.tvGroupTitle.text = itemOrAd.groupName
            holder.rvStandingLeft.adapter = StandingLeftAdapter(itemOrAd.teams)
            holder.rvStandingRight.adapter = StandingRightAdapter(itemOrAd.teams)
        }
    }

    override fun getItemCount() = itemsWithAds.size

    fun updateData(newGroups: List<GroupTable>) {
        groups = newGroups
        updateItemsWithAds()
        notifyDataSetChanged()
    }

    class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Коментуємо через відсутність бібліотеки Google Ads
        // val adView: com.google.android.gms.ads.nativead.NativeAdView = view as com.google.android.gms.ads.nativead.NativeAdView
        val headline: TextView = view.findViewById(R.id.ad_headline)
        val body: TextView = view.findViewById(R.id.ad_body)
        val icon: android.widget.ImageView = view.findViewById(R.id.ad_app_icon)
        val callToAction: android.widget.Button = view.findViewById(R.id.ad_call_to_action)
    }
}
