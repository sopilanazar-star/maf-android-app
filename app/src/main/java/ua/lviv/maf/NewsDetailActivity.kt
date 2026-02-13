package ua.lviv.maf

import android.os.Build
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

        // ПЕРЕВІР: Ключі "NEWS_TITLE" та "NEWS_CONTENT" мають бути такими ж в адаптері!
        val title = intent.getStringExtra("NEWS_TITLE") ?: "Новина"
        val content = intent.getStringExtra("NEWS_CONTENT") ?: ""
        
        tvHeader.text = title

        // Використовуємо FromHtml, щоб \n\n або залишки тегів <b> відображалися красиво
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvContent.text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            tvContent.text = Html.fromHtml(content)
        }
    }
}
