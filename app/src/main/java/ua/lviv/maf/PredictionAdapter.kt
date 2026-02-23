package ua.lviv.maf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Адаптер тепер приймає список PredictionListItem (заголовки + матчі)
class PredictionAdapter(
    private val items: List<PredictionListItem>,
    private val onSubmitPrediction: (PredictionMatchModel, String, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Константи для типів елементів
    private val TYPE_HEADER = 0
    private val TYPE_MATCH = 1

    // Визначаємо тип елемента за позицією
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PredictionListItem.StageHeader -> TYPE_HEADER
            is PredictionListItem.MatchItem -> TYPE_MATCH
        }
    }

    // Створюємо відповідний ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stage_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_MATCH -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prediction, parent, false)
                MatchViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // Прив'язуємо дані до ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is PredictionListItem.StageHeader -> {
                (holder as HeaderViewHolder).bind(item)
            }
            is PredictionListItem.MatchItem -> {
                (holder as MatchViewHolder).bind(item.match)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder для заголовка туру
    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvStageHeader: TextView = view.findViewById(R.id.tvStageHeader)
        fun bind(header: PredictionListItem.StageHeader) {
            tvStageHeader.text = header.stageName
        }
    }

    // ViewHolder для картки матчу
    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeague: TextView = view.findViewById(R.id.tvLeague)
        val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)
        val tvTeam1Name: TextView = view.findViewById(R.id.tvTeam1Name)
        val tvTeam2Name: TextView = view.findViewById(R.id.tvTeam2Name)
        val ivTeam1Logo: ImageView = view.findViewById(R.id.ivTeam1Logo)
        val ivTeam2Logo: ImageView = view.findViewById(R.id.ivTeam2Logo)
        val etScore1: EditText = view.findViewById(R.id.etScore1)
        val etScore2: EditText = view.findViewById(R.id.etScore2)
        val btnSubmitPrediction: Button = view.findViewById(R.id.btnSubmitPrediction)

        fun bind(match: PredictionMatchModel) {
            tvLeague.text = match.tournament // Показуємо назву турніру
            tvMatchDate.text = match.matchDateStr
            tvTeam1Name.text = match.team1Name
            tvTeam2Name.text = match.team2Name

            // 🔥 Завантажуємо логотипи через Glide
            Glide.with(itemView.context)
                .load(match.team1LogoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery) // Заглушка, поки вантажиться
                .error(android.R.drawable.ic_delete) // Якщо помилка завантаження
                .into(ivTeam1Logo)

            Glide.with(itemView.context)
                .load(match.team2LogoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(ivTeam2Logo)

            etScore1.setText(match.predictedScore1 ?: "")
            etScore2.setText(match.predictedScore2 ?: "")

            btnSubmitPrediction.setOnClickListener {
                val score1 = etScore1.text.toString()
                val score2 = etScore2.text.toString()
                if (score1.isNotEmpty() && score2.isNotEmpty()) {
                    onSubmitPrediction(match, score1, score2)
                } else {
                    Toast.makeText(itemView.context, "Введіть рахунок для обох команд", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
