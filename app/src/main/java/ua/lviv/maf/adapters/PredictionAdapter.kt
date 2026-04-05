package ua.lviv.maf.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.lviv.maf.R
import ua.lviv.maf.models.PredictionListItem
import ua.lviv.maf.models.PredictionMatchModel

class PredictionAdapter(
    private val onSubmitPrediction: (PredictionMatchModel, String, String) -> Unit
) : ListAdapter<PredictionListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MATCH = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is PredictionListItem.StageHeader -> TYPE_HEADER
            is PredictionListItem.MatchItem -> TYPE_MATCH
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                inflater.inflate(R.layout.item_stage_header, parent, false)
            )
        } else {
            MatchViewHolder(
                inflater.inflate(R.layout.item_prediction, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PredictionListItem.StageHeader ->
                (holder as HeaderViewHolder).bind(item)

            is PredictionListItem.MatchItem ->
                (holder as MatchViewHolder).bind(item.match)
        }
    }

    // ================= HEADER =================

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvStageHeader: TextView =
            view.findViewById(R.id.tvStageHeader)

        fun bind(header: PredictionListItem.StageHeader) {
            tvStageHeader.text = header.title
        }
    }

    // ================= MATCH =================

    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val tvLeague: TextView = view.findViewById(R.id.tvLeague)
        private val tvMatchDate: TextView = view.findViewById(R.id.tvMatchDate)
        private val tvTeam1Name: TextView = view.findViewById(R.id.tvTeam1Name)
        private val tvTeam2Name: TextView = view.findViewById(R.id.tvTeam2Name)
        private val ivTeam1Logo: ImageView = view.findViewById(R.id.ivTeam1Logo)
        private val ivTeam2Logo: ImageView = view.findViewById(R.id.ivTeam2Logo)
        private val etScore1: EditText = view.findViewById(R.id.etScore1)
        private val etScore2: EditText = view.findViewById(R.id.etScore2)
        private val btnSubmit: Button = view.findViewById(R.id.btnSubmitPrediction)

        fun bind(match: PredictionMatchModel) {

            tvLeague.text = match.tournament
            tvMatchDate.text = match.matchDateStr
            tvTeam1Name.text = match.team1Name
            tvTeam2Name.text = match.team2Name

            Glide.with(itemView)
                .load(match.team1LogoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivTeam1Logo)

            Glide.with(itemView)
                .load(match.team2LogoUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivTeam2Logo)

            // restore state
            etScore1.setText(match.predictedScore1 ?: "")
            etScore2.setText(match.predictedScore2 ?: "")

            etScore1.doAfterTextChanged {
                match.predictedScore1 = it?.toString()
            }

            etScore2.doAfterTextChanged {
                match.predictedScore2 = it?.toString()
            }

            btnSubmit.setOnClickListener {

                val s1 = match.predictedScore1
                val s2 = match.predictedScore2

                if (s1.isNullOrEmpty() || s2.isNullOrEmpty()) {
                    Toast.makeText(
                        itemView.context,
                        "Введіть рахунок",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                onSubmitPrediction(match, s1, s2)
            }
        }
    }

    // ================= DIFF =================

    class DiffCallback : DiffUtil.ItemCallback<PredictionListItem>() {

        override fun areItemsTheSame(
            oldItem: PredictionListItem,
            newItem: PredictionListItem
        ): Boolean =
            when {
                oldItem is PredictionListItem.StageHeader &&
                        newItem is PredictionListItem.StageHeader ->
                    oldItem.title == newItem.title

                oldItem is PredictionListItem.MatchItem &&
                        newItem is PredictionListItem.MatchItem ->
                    oldItem.match.id == newItem.match.id

                else -> false
            }

        override fun areContentsTheSame(
            oldItem: PredictionListItem,
            newItem: PredictionListItem
        ) = oldItem == newItem
    }
}