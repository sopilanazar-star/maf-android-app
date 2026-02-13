package ua.lviv.maf

import android.os.Bundle
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
        // Отримуємо вже очищений на сервері текст
        val cleanContent = intent.getStringExtra("NEWS_CONTENT") ?: ""
        
        tvHeader.text = title

        // Тепер просто встановлюємо текст. 
        // Оскільки в PHP ми додали "\n\n", TextView сам зробить гарні відступи.
        tvContent.text = cleanContent
    }
}
