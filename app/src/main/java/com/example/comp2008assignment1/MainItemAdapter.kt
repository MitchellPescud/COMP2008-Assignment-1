package com.example.comp2008assignment1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// Category item model
data class CategoryItem(
    val name: String,
    val iconRes: Int? = null   // optional drawable resource
)

// --- Helpers: name -> drawable id (with caching) ---
private fun categoryToDrawableName(raw: String): String =
    raw.lowercase()
        .replace("[^a-z0-9]+".toRegex(), "_")
        .replace("_+".toRegex(), "_")
        .trim('_')
        .ifBlank { "unknown" } // fallback to your unknown drawable

private class DrawableIdCache(private val ctx: Context) {
    private val map = mutableMapOf<String, Int>()
    fun idFor(category: String): Int {
        return map.getOrPut(category) {
            val name = categoryToDrawableName(category)
            val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
            if (id != 0) id else R.drawable.unknown
        }
    }
}

// --- Diff ---
private object CategoryDiff : DiffUtil.ItemCallback<CategoryItem>() {
    override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem) =
        oldItem.name == newItem.name
    override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem) =
        oldItem == newItem
}

// --- Adapter ---
class CategoryButtonAdapter(
    context: Context,
    private val onClick: (CategoryItem, Boolean) -> Unit, // (item, isSelected)
    private val toggleSelection: Boolean = true           // Row1 can pass false to avoid toggle
) : ListAdapter<CategoryItem, CategoryButtonAdapter.VH>(CategoryDiff) {

    private val iconCache = DrawableIdCache(context.applicationContext)
    private val selected = mutableSetOf<String>() // store selected names

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tile: View = itemView.findViewById(R.id.squareContainer) ?: itemView
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_button, parent, false)
        v.isClickable = true
        v.isFocusable = true
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val isSelected = toggleSelection && selected.contains(item.name)

        holder.label.text = item.name

        // Use custom icon if provided, else fallback to name-mapped drawable
        val resId = item.iconRes ?: iconCache.idFor(item.name)
        holder.icon.setImageResource(resId)
        holder.icon.contentDescription = item.name

        // Selected state styling only if toggleSelection is enabled
        holder.itemView.alpha = if (isSelected) 1.0f else 0.92f
        holder.itemView.isSelected = isSelected

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            val clicked = getItem(pos)
            if (toggleSelection) {
                val nowSelected = if (selected.remove(clicked.name)) {
                    false
                } else {
                    selected.add(clicked.name); true
                }
                notifyItemChanged(pos)
                onClick(clicked, nowSelected)
            } else {
                // Fire-and-forget (no toggle UI/state) — perfect for Row 1 navigation
                onClick(clicked, false)
            }
        }
    }

    // Public APIs for selection control (optional)
    fun setSelectedCategories(names: Set<String>) {
        selected.clear()
        selected.addAll(names)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        if (selected.isEmpty()) return
        selected.clear()
        notifyDataSetChanged()
    }
}
