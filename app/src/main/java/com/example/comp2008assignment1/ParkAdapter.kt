package com.example.comp2008assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.google.android.material.imageview.ShapeableImageView

// Adapter to handle displaying a list of Park objects inside a RecyclerView
// Responsible for binding data to item views and handling interactions (favourite button)

class ParkAdapter(
    private val onFavouriteClick: (Park) -> Unit // Callback when a park is favourited/unfavourited
) : RecyclerView.Adapter<ParkAdapter.ParkViewHolder>() {

    // List of Park objects for the adapter
    private val items = mutableListOf<Park>()

    // Create new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_park, parent, false) // Inflate custom item layout
        return ParkViewHolder(view, onFavouriteClick)
    }

    // Bind data to an existing ViewHolder at a given position
    override fun onBindViewHolder(holder: ParkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // Returns how many items are currently in the list
    override fun getItemCount(): Int = items.size

    // Replaces current list with a new one and refreshes the RecyclerView
    fun setItems(newItems: List<Park>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // Adds a single park item to the list
    fun addItem(park: Park) {
        items.add(park)
        notifyItemInserted(items.lastIndex) // Only refresh newly added item
    }

    // Removes a park at a specific position
    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Returns the current list of items
    fun currentItems(): List<Park> = items

    class ParkViewHolder(
        itemView: View,
        private val onFavouriteClick: (Park) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val parkImage: ShapeableImageView = itemView.findViewById(R.id.imagePark)
        private val name: TextView = itemView.findViewById(R.id.textName)
        private val location: TextView = itemView.findViewById(R.id.textLocation)
        private val activities: TextView = itemView.findViewById(R.id.textActivities)
        private val features: TextView = itemView.findViewById(R.id.textFeatures)
        private val favourite: ImageButton = itemView.findViewById(R.id.btnFavourite)

        // Bind a single Park object to the view
        fun bind(park: Park) {
            val ctx = itemView.context

            // Ensure isFavourite is synced with persisted storage
            park.isFavourite = FavouriteStore.isFavourite(ctx, park.parkID)

            // Try to resolve park image using parkID
            val imageId = ctx.resources.getIdentifier(
                park.parkID.lowercase(), // assumes drawable names are lowercase
                "drawable",
                ctx.packageName
            )

            // If image exists, use it, otherwise fallback to sample
            if (imageId != 0) {
                parkImage.setImageResource(imageId)
            } else {
                parkImage.setImageResource(R.drawable.sample_park) // fallback/default
            }

            // Set name and location text
            name.text = park.name
            location.text = park.location

            // Join non-blank activities with • and show/hide if empty
            val act = park.activities.filter { it.isNotBlank() }.joinToString(" • ")
            activities.visibility = if (act.isBlank()) View.GONE else View.VISIBLE
            activities.text = if (act.isNotBlank()) "Activities: $act" else ""

            // Join non-blank features with • and show/hide if empty
            val feat = park.features.filter { it.isNotBlank() }.joinToString(" • ")
            features.visibility = if (feat.isBlank()) View.GONE else View.VISIBLE
            features.text = if (feat.isNotBlank()) "Features: $feat" else ""

            // Update star icon depending on favourite state
            favourite.setImageResource(
                if (park.isFavourite) R.drawable.favourited_button_icon
                else R.drawable.favourite_button_icon
            )

            // Handle clicks on favourite button
            favourite.setOnClickListener {
                // Toggle favourite state
                park.isFavourite = !park.isFavourite

                // Save the change to persistent storage
                FavouriteStore.toggleFavourite(ctx, park.parkID, park.isFavourite)

                // Update star icon immediately for visual feedback
                favourite.setImageResource(
                    if (park.isFavourite) R.drawable.favourited_button_icon
                    else R.drawable.favourite_button_icon
                )

                // Trigger callback so Activity/Fragment can react (e.g., show Toast)
                onFavouriteClick(park)
            }

            // Handle clicks anywhere else on the item
            itemView.setOnClickListener {
                val intent = Intent(ctx, ParkDetailedView::class.java).apply {
                    putExtra("PARK_ID", park.parkID) // pass only the ID
                }
                ctx.startActivity(intent)
            }
        }
    }
}
