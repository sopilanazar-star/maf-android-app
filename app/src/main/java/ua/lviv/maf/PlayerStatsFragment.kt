class PlayerStatsFragment : Fragment(R.layout.fragment_player_stats) {

    private var isGoalkeeper = false
    private lateinit var rows: List<View>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getString("position") ?: ""
        // Перевірка, чи це воротар (може бути "Воротар", "Goalkeeper", "GK")
        isGoalkeeper = position.contains("Воротар", ignoreCase = true) || position.contains("GK")

        // Знаходимо всі рядки (include)
        rows = listOf(
            view.findViewById(R.id.row1),
            view.findViewById(R.id.row2),
            view.findViewById(R.id.row3),
            view.findViewById(R.id.row4),
            view.findViewById(R.id.row5)
        )

        // TODO: Тут має бути запит до API або БД за статистикою по ID
        // Поки що заповнимо тестовими даними (Mock Data)
        fillStats(getMockStats()) 
    }

    private fun fillStats(stats: JSONObject) {
        // Допоміжна функція для заповнення рядка
        fun setRow(rowIndex: Int, leftVal: String, leftLbl: String, rightVal: String, rightLbl: String) {
            val row = rows[rowIndex]
            row.findViewById<TextView>(R.id.tvValueLeft).text = leftVal
            row.findViewById<TextView>(R.id.tvLabelLeft).text = leftLbl
            row.findViewById<TextView>(R.id.tvValueRight).text = rightVal
            row.findViewById<TextView>(R.id.tvLabelRight).text = rightLbl
        }

        val played = stats.optString("matches", "0")
        val started = stats.optString("started", "0")
        val subIn = stats.optString("sub_in", "0")
        val minutes = stats.optString("minutes", "0")
        val yellow = stats.optString("yellow", "0")
        val secondYellow = stats.optString("yellow2", "0")
        val red = stats.optString("red", "0")
        
        if (isGoalkeeper) {
            // ЛОГІКА ВОРОТАРЯ
            val conceded = stats.optString("conceded", "0") // Пропущені
            val cleanSheets = stats.optString("clean_sheets", "0") // Сухі
            val goalsScored = stats.optString("goals", "0") // Голи (раптом забив)

            setRow(0, played, "Зіграні матчі", started, "У старті")
            setRow(1, subIn, "Вийшов на заміну", minutes, "Хвилини на полі")
            setRow(2, yellow, "Жовті картки", secondYellow, "Друга жовта")
            setRow(3, red, "Вилучення", goalsScored, "Голи")
            setRow(4, conceded, "Пропущені голи", cleanSheets, "Сухі матчі")
        } else {
            // ЛОГІКА ПОЛЬОВОГО ГРАВЦЯ
            val goals = stats.optString("goals", "0")
            val assists = stats.optString("assists", "0")

            setRow(0, played, "Зіграні матчі", started, "У старті")
            setRow(1, subIn, "Вийшов на заміну", minutes, "Хвилини на полі")
            setRow(2, yellow, "Жовті картки", secondYellow, "Друга жовта")
            setRow(3, red, "Вилучення", assists, "Асисти")
            
            // Останній рядок - акцент на голи
            setRow(4, goals, "ГОЛИ", "", "") 
        }
    }
    
    // Фейкові дані для тесту
    private fun getMockStats(): JSONObject {
        return JSONObject().apply {
            put("matches", "15")
            put("started", "12")
            put("goals", "5")
            put("assists", "2")
            put("clean_sheets", "4") // Для тесту воротаря
        }
    }

    companion object {
        fun newInstance(id: String, pos: String) = PlayerStatsFragment().apply {
            arguments = Bundle().apply {
                putString("player_id", id)
                putString("position", pos)
            }
        }
    }
}
