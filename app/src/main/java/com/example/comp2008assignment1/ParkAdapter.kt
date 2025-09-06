package com.example.comp2008assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParkAdapter(
    private val onFavouriteClick: (Park) -> Unit
) : RecyclerView.Adapter<ParkAdapter.ParkViewHolder>() {

    private val items = mutableListOf<Park>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_park, parent, false)
        return ParkViewHolder(view, onFavouriteClick)
    }

    override fun onBindViewHolder(holder: ParkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<Park>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(park: Park) {
        items.add(park)
        notifyItemInserted(items.lastIndex)
    }

    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun currentItems(): List<Park> = items // handy for Favourites screen, if needed

    class ParkViewHolder(
        itemView: View,
        private val onFavouriteClick: (Park) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.textName)
        private val location: TextView = itemView.findViewById(R.id.textLocation)
        private val activities: TextView = itemView.findViewById(R.id.textActivities)
        private val features: TextView = itemView.findViewById(R.id.textFeatures)
        private val favourite: ImageButton = itemView.findViewById(R.id.btnFavourite)

        fun bind(park: Park) {
            val ctx = itemView.context

            // Re-sync in-memory flag from persisted store (covers orientation/app restart)
            park.isFavourite = FavouriteStore.isFavourite(ctx, park.parkID)

            name.text = park.name
            location.text = park.location

            val act = park.activities.filter { it.isNotBlank() }.joinToString(" • ")
            activities.visibility = if (act.isBlank()) View.GONE else View.VISIBLE
            activities.text = if (act.isNotBlank()) "Activities: $act" else ""

            val feat = park.features.filter { it.isNotBlank() }.joinToString(" • ")
            features.visibility = if (feat.isBlank()) View.GONE else View.VISIBLE
            features.text = if (feat.isNotBlank()) "Features: $feat" else ""

            favourite.setImageResource(
                if (park.isFavourite) R.drawable.favourited_button_icon
                else R.drawable.favourite_button_icon
            )

            favourite.setOnClickListener {
                park.isFavourite = !park.isFavourite
                // Persist the change
                FavouriteStore.toggleFavourite(ctx, park.parkID, park.isFavourite)
                // Update icon immediately
                favourite.setImageResource(
                    if (park.isFavourite) R.drawable.favourited_button_icon
                    else R.drawable.favourite_button_icon
                )
                onFavouriteClick(park)
            }
        }
    }
}
