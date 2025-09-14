package com.example.comp2008assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val items: List<CategoryItem>,
    private val onClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return Holder(v, onClick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class Holder(
        view: View,
        private val onClick: (CategoryItem) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.iconCategory)
        private val name: TextView = view.findViewById(R.id.textCategory)
        private val count: TextView = view.findViewById(R.id.textCount)

        fun bind(item: CategoryItem) {
            icon.setImageResource(item.iconRes)
            name.text = item.name
            count.text = "${item.count} parks"
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
