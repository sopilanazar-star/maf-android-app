package ua.lviv.maf.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.lviv.maf.R
import ua.lviv.maf.models.StandingItem

class StandingAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<StandingItem>()

    fun submit(list: List<StandingItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is StandingItem.GroupHeader -> 0
            is StandingItem.TableHeader -> 1
            is StandingItem.TeamRow -> 2
            is StandingItem.PlayoffHeader -> 3
            is StandingItem.PlayoffStage -> 4
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            0 -> GroupHeaderVH(inflater.inflate(R.layout.item_group_header, parent, false))
            1 -> TableHeaderVH(inflater.inflate(R.layout.item_table_header, parent, false))
            2 -> TeamVH(inflater.inflate(R.layout.item_team, parent, false))
            3 -> PlayoffHeaderVH(inflater.inflate(R.layout.item_playoff_header, parent, false))
            else -> PlayoffStageVH(inflater.inflate(R.layout.item_playoff_stage, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is StandingItem.GroupHeader -> (holder as GroupHeaderVH).bind(item)
            is StandingItem.TableHeader -> {}
            is StandingItem.TeamRow -> (holder as TeamVH).bind(item)
            is StandingItem.PlayoffHeader -> (holder as PlayoffHeaderVH).bind(item)
            is StandingItem.PlayoffStage -> (holder as PlayoffStageVH).bind(item)
        }
    }

    class GroupHeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.groupTitle)
        fun bind(item: StandingItem.GroupHeader) {
            title.text = item.title.uppercase()
        }
    }

    class TableHeaderVH(v: View) : RecyclerView.ViewHolder(v)

    class TeamVH(v: View) : RecyclerView.ViewHolder(v) {

        private val pos = v.findViewById<TextView>(R.id.position)
        private val logo = v.findViewById<ImageView>(R.id.logo)
        private val name = v.findViewById<TextView>(R.id.teamName)
        private val games = v.findViewById<TextView>(R.id.games)
        private val win = v.findViewById<TextView>(R.id.win)
        private val draw = v.findViewById<TextView>(R.id.draw)
        private val loss = v.findViewById<TextView>(R.id.loss)
        private val goals = v.findViewById<TextView>(R.id.goals)
        private val pts = v.findViewById<TextView>(R.id.points)

        fun bind(item: StandingItem.TeamRow) {
            pos.text = item.position.toString()
            name.text = item.name
            games.text = item.games.toString()
            win.text = item.win.toString()
            draw.text = item.draw.toString()
            loss.text = item.loss.toString()
            goals.text = "${item.goalsFor}-${item.goalsAgainst}"
            pts.text = item.points.toString()

            Glide.with(itemView.context)
                .load(item.logo)
                .into(logo)
        }
    }

    class PlayoffHeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.playoffHeader)
        fun bind(item: StandingItem.PlayoffHeader) {
            title.text = item.title
        }
    }

    class PlayoffStageVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.stageTitle)
        fun bind(item: StandingItem.PlayoffStage) {
            title.text = item.title
        }
    }
}
