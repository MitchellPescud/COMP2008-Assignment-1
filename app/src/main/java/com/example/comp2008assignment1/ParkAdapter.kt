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
            name.text = park.name
            location.text = park.location

            // Activities
            val act = park.activities.filter { it.isNotBlank() }.joinToString(" • ")
            activities.visibility = if (act.isBlank()) View.GONE else View.VISIBLE
            activities.text = if (act.isNotBlank()) "Activities: $act" else ""

            // Features
            val feat = park.features.filter { it.isNotBlank() }.joinToString(" • ")
            features.visibility = if (feat.isBlank()) View.GONE else View.VISIBLE
            features.text = if (feat.isNotBlank()) "Features: $feat" else ""

            // Set correct icon
            favourite.setImageResource(
                if (park.isFavourite) R.drawable.favourited_button_icon
                else R.drawable.favourite_button_icon
            )

            // Handle click
            favourite.setOnClickListener {
                park.isFavourite = !park.isFavourite
                onFavouriteClick(park)

                // Update icon immediately
                favourite.setImageResource(
                    if (park.isFavourite) R.drawable.favourited_button_icon
                    else R.drawable.favourite_button_icon
                )
            }
        }
    }
}
