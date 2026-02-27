class StandingRightAdapter(private val items: List<StandingItem>) : 
    RecyclerView.Adapter<StandingRightAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGames: TextView = view.findViewById(R.id.tvGames)
        val tvWins: TextView = view.findViewById(R.id.tvWins)
        val tvDraws: TextView = view.findViewById(R.id.tvDraws)
        val tvLosses: TextView = view.findViewById(R.id.tvLosses)
        val tvGoalsDiff: TextView = view.findViewById(R.id.tvGoalsDiff)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val layoutForm: LinearLayout = view.findViewById(R.id.layoutForm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_right, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        holder.tvGames.text = item.games.toString()
        holder.tvWins.text = item.wins.toString()
        holder.tvDraws.text = item.draws.toString()
        holder.tvLosses.text = item.losses.toString()
        holder.tvGoalsDiff.text = item.goalsDiff
        holder.tvPoints.text = item.points.toString()

        // Очищуємо стару форму перед додаванням нової (важливо для стабільності)
        holder.layoutForm.removeAllViews()
        // Тут твій існуючий код додавання кружечків форми (W, D, L)
        // addFormCircles(holder.layoutForm, item.form) 
    }

    override fun getItemCount() = items.size
}
