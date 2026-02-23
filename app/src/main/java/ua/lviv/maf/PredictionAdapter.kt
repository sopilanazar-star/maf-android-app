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

class PredictionAdapter(
    private val matches: List<PredictionMatchModel>,
    private val onSubmitPrediction: (PredictionMatchModel, String, String) -> Unit
) : RecyclerView.Adapter<PredictionAdapter.PredictionViewHolder>() {

    inner class PredictionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLeague: TextView = view.findViewById(R.id.tvLeague)
        val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)
        val tvTeam1Name: TextView = view.findViewById(R.id.tvTeam1Name)
        val tvTeam2Name: TextView = view.findViewById(R.id.tvTeam2Name)
        val ivTeam1Logo: ImageView = view.findViewById(R.id.ivTeam1Logo)
        val ivTeam2Logo: ImageView = view.findViewById(R.id.ivTeam2Logo)
        val etScore1: EditText = view.findViewById(R.id.etScore1)
        val etScore2: EditText = view.findViewById(R.id.etScore2)
        val btnSubmitPrediction: Button = view.findViewById(R.id.btnSubmitPrediction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prediction, parent, false)
        return PredictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        val match = matches[position]

        holder.tvLeague.text = match.league
        holder.tvMatchDate.text = match.matchDateStr
        holder.tvTeam1Name.text = match.team1Name
        holder.tvTeam2Name.text = match.team2Name

        // Тут потім додамо Glide або Picasso для загрузки логотипів з match.team1LogoUrl

        // Якщо користувач вже вводив прогноз, показуємо його
        holder.etScore1.setText(match.predictedScore1 ?: "")
        holder.etScore2.setText(match.predictedScore2 ?: "")

        holder.btnSubmitPrediction.setOnClickListener {
            val score1 = holder.etScore1.text.toString()
            val score2 = holder.etScore2.text.toString()

            if (score1.isNotEmpty() && score2.isNotEmpty()) {
                onSubmitPrediction(match, score1, score2)
            } else {
                Toast.makeText(holder.itemView.context, "Введіть рахунок для обох команд", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = matches.size
}
