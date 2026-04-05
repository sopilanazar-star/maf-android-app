package ua.lviv.maf.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import ua.lviv.maf.R
import ua.lviv.maf.models.Stage // Імпорт твоєї моделі Stage

class StagesAdapter(private val onStageClick: (Stage) -> Unit) :
    ListAdapter<Stage, StagesAdapter.StageViewHolder>(StageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stage_selector, parent, false)
        return StageViewHolder(view)
    }

    override fun onBindViewHolder(holder: StageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardStage: MaterialCardView = view.findViewById(R.id.cardStage)
        private val tvStageName: TextView = view.findViewById(R.id.tvStageName)

        fun bind(stage: Stage) {
            tvStageName.text = stage.name

            // Логіка підсвічування (Етап 2)
            if (stage.isSelected) {
                // Вибраний тур: Червоний фон, Білий текст
                cardStage.setCardBackgroundColor(Color.parseColor("#E30613"))
                tvStageName.setTextColor(Color.WHITE)
                cardStage.strokeWidth = 0
            } else {
                // Невибраний тур: Білий фон, Чорний текст, Сіра рамка
                cardStage.setCardBackgroundColor(Color.WHITE)
                tvStageName.setTextColor(Color.BLACK)
                cardStage.strokeWidth = 2
                cardStage.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")))
            }

            itemView.setOnClickListener {
                onStageClick(stage)
            }
        }
    }

    // Клас для швидкого оновлення списку без "миготіння"
    class StageDiffCallback : DiffUtil.ItemCallback<Stage>() {
        override fun areItemsTheSame(oldItem: Stage, newItem: Stage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Stage, newItem: Stage): Boolean {
            return oldItem == newItem
        }
    }
}