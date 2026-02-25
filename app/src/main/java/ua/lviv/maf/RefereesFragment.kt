package ua.lviv.maf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RefereesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var tvHeaderTitle: TextView
    private val client = OkHttpClient()

    private var selectedYear: String = AppConfig.selectedYear

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_referees, container, false)

        recyclerView = view.findViewById(R.id.rvReferees)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle)

        selectedYear = AppConfig.selectedYear
        tvHeaderTitle.text = "Арбітри ($selectedYear)"

        view.findViewById<View>(R.id.btnBackText)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        fetchReferees()
        return view
    }

    fun updateYear(year: String) {
        if (selectedYear != year) {
            selectedYear = year
            tvHeaderTitle.text = "Арбітри ($selectedYear)"
            fetchReferees()
        }
    }

    private fun fetchReferees() {

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.GONE

        // 🔥 тепер передаємо просто YEAR
        val url = "https://maf.lviv.ua/wp-json/maf/v2/referees/full?year=$selectedYear"

        Log.d("REFEREES", url)

        client.newCall(Request.Builder().url(url).build()).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                activity?.runOnUiThread { showEmptyState() }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return
                val body = response.body?.string() ?: ""

                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    try {
                        val array = JSONArray(body)
                        val list = mutableListOf<JSONObject>()
                        for (i in 0 until array.length()) list.add(array.getJSONObject(i))

                        if (list.isEmpty()) showEmptyState()
                        else setupList(list)

                    } catch (e: Exception) {
                        showEmptyState()
                    }
                }
            }
        })
    }

    private fun setupList(data: List<JSONObject>) {
        recyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RefereesAdapter(data) {}
    }

    private fun showEmptyState() {
        if (!isAdded) return
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }
}
