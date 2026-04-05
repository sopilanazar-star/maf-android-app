package ua.lviv.maf

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PredictionsTableActivity : AppCompatActivity() {

    private lateinit var rvTable: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predictions_table)

        val btnBack = findViewById<TextView>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        rvTable = findViewById(R.id.rvPredictionsTable)
        rvTable.layoutManager = LinearLayoutManager(this)
    }
}