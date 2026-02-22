package ua.lviv.maf

// Твоя оновлена модель рядка команди
data class StandingRow(
    val team_id: String = "",
    val position: Int = 0,
    val team_name: String = "",
    val logo: String = "",
    val games: Int = 0,
    val win: Int = 0,
    val draw: Int = 0,
    val loss: Int = 0,
    val goals_for: Int = 0,
    val goals_against: Int = 0,
    val points: Int = 0,
    val is_group_header: Boolean = false,
    val group_name: String = "",
    val form: List<String>? = emptyList(),
    
    // 🔥 НОВЕ: Поле для шторки (відкрита/закрита)
    var isExpanded: Boolean = false 
)

// 🔥 НОВЕ: Модель для картки групи (УЄФА стиль)
data class GroupTable(
    val groupName: String,
    val teams: List<StandingRow>
)
