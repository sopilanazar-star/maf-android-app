package ua.lviv.maf

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(private val newsList: List<NewsModel>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvNewsTitle)
        val tvPreview: TextView = view.findViewById(R.id.tvNewsPreview)
        val tvDate: TextView = view.findViewById(R.id.tvNewsDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]
        holder.tvTitle.text = news.title
        holder.tvPreview.text = news.preview
        holder.tvDate.text = news.date

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, NewsDetailActivity::class.java).apply {
                putExtra("NEWS_TITLE", news.title)
                putExtra("NEWS_CONTENT", news.content)
                putExtra("NEWS_DATE", news.date)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = newsList.size
}
