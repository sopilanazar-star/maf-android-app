package ua.lviv.maf

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val btnBack: ImageButton = findViewById(R.id.btnNewsBack)
        val tvHeader: TextView = findViewById(R.id.tvNewsDetailHeader)
        val webView: WebView = findViewById(R.id.webViewNews)

        btnBack.setOnClickListener { finish() }

        // Отримуємо дані
        val title = intent.getStringExtra("NEWS_TITLE") ?: "Новина"
        val rawHtml = intent.getStringExtra("NEWS_CONTENT") ?: ""
        
        tvHeader.text = title

        // Стилізуємо HTML, щоб він пасував до темної теми додатка
        val styledHtml = """
            <html>
            <head>
                <style>
                    body {
                        background-color: #1A1D23;
                        color: #FFFFFF;
                        font-family: sans-serif;
                        padding: 15px;
                        line-height: 1.6;
                    }
                    h2, h3 { color: #E30613; }
                    ul { padding-left: 20px; }
                    li { margin-bottom: 10px; }
                    strong { color: #FFFFFF; }
                    .maf-highlight { 
                        background: #252932; 
                        border-left: 4px solid #E30613; 
                        padding: 10px; 
                        margin: 15px 0; 
                    }
                </style>
            </head>
            <body>
                $rawHtml
            </body>
            </html>
        """.trimIndent()

        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
    }
}
