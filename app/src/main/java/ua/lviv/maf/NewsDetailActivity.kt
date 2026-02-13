package ua.lviv.maf

import android.os.Bundle
import android.text.Html
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val btnBack: ImageButton = findViewById(R.id.btnNewsBack)
        val tvHeader: TextView = findViewById(R.id.tvNewsDetailHeader)
        val tvContent: TextView = findViewById(R.id.tvFullContent)

        btnBack.setOnClickListener { finish() }

        val title = intent.getStringExtra("NEWS_TITLE") ?: ""
        val rawHtml = intent.getStringExtra("NEWS_CONTENT") ?: ""
        
        tvHeader.text = title

        // Обробка HTML: прибираємо теги, залишаємо іконки та форматування
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvContent.text = Html.fromHtml(rawHtml, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            tvContent.text = Html.fromHtml(rawHtml)
        }
    }
}
