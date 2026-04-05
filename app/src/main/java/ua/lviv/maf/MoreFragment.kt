package ua.lviv.maf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Відключаємо імпорти реклами
// import com.google.android.gms.ads.AdRequest
// import com.google.android.gms.ads.FullScreenContentCallback
// import com.google.android.gms.ads.LoadAdError
// import com.google.android.gms.ads.interstitial.InterstitialAd
// import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import android.content.Context

data class MenuItem(val id: Int, val title: String)

class MoreFragment : Fragment() {

    private val TELEGRAM_BOT_URL = "https://t.me/MafFeedback_bot"

    // Реклама та лічильник
    //private var mInterstitialAd: InterstitialAd? = null
    // Тепер ми не задаємо 0 тут, а будемо брати значення з пам'яті

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        val rvMoreMenu = view.findViewById<RecyclerView>(R.id.rvMoreMenu)
        val btnTelegram = view.findViewById<Button>(R.id.btnTelegram)

        // Завантажуємо рекламу відразу
        //loadAd()

        btnTelegram.setOnClickListener {
            openTelegramBot()
        }

        // --- ДОДАЛИ НОВИЙ ПУНКТ (8) ---
        val menuItems = listOf(
            // MenuItem(1, "Прогноз на матчі"), // Тимчасово приховано
            MenuItem(2, "Дискваліфіковані гравці"),
            MenuItem(3, "Бомбардири (І ліга)"),
            MenuItem(4, "Бомбардири (ІІ ліга)"),
            MenuItem(5, "Бомбардири U-19 (І ліга)"),
            MenuItem(6, "Бомбардири U-19 (ІІ ліга)"),
            MenuItem(7, "Арбітри"),
            MenuItem(8, "📺 Відеоогляди матчів")
        )

        rvMoreMenu.layoutManager = LinearLayoutManager(context)
        rvMoreMenu.adapter = MoreMenuAdapter(menuItems) { clickedItem ->
            handleMenuClick(clickedItem)
        }

        return view
    }

    fun refreshData() {
        if (isAdded) {
            val containerId = (requireView().parent as View).id
            val currentFragment = parentFragmentManager.findFragmentById(containerId)
            
            if (currentFragment is DisqualifiedFragment) {
                currentFragment.updateYear() 
            }
            
            Toast.makeText(context, "Рік змінено на ${AppConfig.selectedYear}", Toast.LENGTH_SHORT).show()
        }
    }
    /* Функція loadAd тимчасово відключена
    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(requireContext(), getString(R.string.ad_unit_id_interstitial), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
            })
    }
    */
    private fun openTelegramBot() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_BOT_URL))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telegram не встановлено на цьому пристрої", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMenuClick(item: MenuItem) {
        // Залишаємо логіку лічильника для стабільності
        val prefs = requireContext().getSharedPreferences("MAF_ADS_PREFS", Context.MODE_PRIVATE)
        var currentCount = prefs.getInt("global_click_counter", 0)
        currentCount++
        prefs.edit().putInt("global_click_counter", currentCount).apply()

        // Рекламу відключено: відразу викликаємо перехід
        executeNavigation(item)

        /* Стара логіка з показом реклами закоментована
        if (currentCount % 10 == 1 && mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    // loadAd()
                    executeNavigation(item)
                }
                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    mInterstitialAd = null
                    executeNavigation(item)
                }
            }
            mInterstitialAd?.show(requireActivity())
        }
        */
    }

    // Твоя стара логіка переходу, просто перейменована
    private fun executeNavigation(item: MenuItem) {
        val containerId = (requireView().parent as View).id
        when (item.id) {
            1 -> {
                val predictionsFragment = PredictionsFragment()
                parentFragmentManager.beginTransaction().replace(containerId, predictionsFragment).addToBackStack(null).commit()
            }
            2 -> {
                val disqualifiedFragment = DisqualifiedFragment()
                parentFragmentManager.beginTransaction().replace(containerId, disqualifiedFragment).addToBackStack(null).commit()
            }
            3 -> openScorersFragment("І ліга", containerId)
            4 -> openScorersFragment("ІІ ліга", containerId)
            5 -> openScorersFragment("U-19 (І ліга)", containerId)
            6 -> openScorersFragment("U-19 (ІІ ліга)", containerId)
            7 -> {
                val refereesFragment = RefereesFragment()
                val bundle = Bundle()
                bundle.putString("SELECTED_YEAR", AppConfig.selectedYear.toString())
                refereesFragment.arguments = bundle
                parentFragmentManager.beginTransaction().replace(containerId, refereesFragment).addToBackStack(null).commit()
            }
            8 -> {
                val mediaFragment = MediaFragment()
                parentFragmentManager.beginTransaction().replace(containerId, mediaFragment).addToBackStack(null).commit()
            }
        }
    }

    private fun openScorersFragment(leagueType: String, containerId: Int) {
        val fragment = ScorersFragment()
        val bundle = Bundle()
        
        bundle.putString("LEAGUE_TYPE", leagueType)
        bundle.putString("SELECTED_YEAR", AppConfig.selectedYear.toString()) 
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    inner class MoreMenuAdapter(
        private val items: List<MenuItem>,
        private val onClick: (MenuItem) -> Unit
    ) : RecyclerView.Adapter<MoreMenuAdapter.MenuViewHolder>() {

        inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMenuTitle: TextView = view.findViewById(R.id.tvMenuTitle)
            init {
                view.setOnClickListener { 
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onClick(items[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_more_menu, parent, false)
            return MenuViewHolder(view)
        }

        override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
            holder.tvMenuTitle.text = items[position].title
        }

        override fun getItemCount() = items.size
    }
}
