class StandingLeftAdapter(private val items: List<StandingItem>) : 
    RecyclerView.Adapter<StandingLeftAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val positionMarker: View = view.findViewById(R.id.positionMarker)
        val tvPosition: TextView = view.findViewById(R.id.tvPosition)
        val ivTeamLogo: ImageView = view.findViewById(R.id.ivTeamLogo)
        val tvTeamName: TextView = view.findViewById(R.id.tvTeamName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_left, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPosition.text = item.position.toString()
        holder.tvTeamName.text = item.teamName
        
        // Завантаження лого (використовуй свою бібліотеку, наприклад Glide або Coil)
        // Glide.with(holder.ivTeamLogo).load(item.logoUrl).into(holder.ivTeamLogo)
        
        // Колір маркера позиції (якщо у тебе є логіка зон вильоту/єврокубків)
        // holder.positionMarker.setBackgroundColor(Color.parseColor(item.zoneColor))
    }

    override fun getItemCount() = items.size
}
