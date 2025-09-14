package com.example.comp2008assignment1

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // --- Load data ---
        val parks = ParkRepository.getAll()

        // Row 1: navigation
        val row1Items = listOf(
            CategoryItem("Explore All", R.drawable.search),
            CategoryItem("Favourites", R.drawable.favourite_button_icon)
        )

        setupHorizontalRecycler(R.id.recyclerSection1, row1Items) { item, _ ->
            when (item.name) {
                "Explore All" -> startActivity(Intent(this, Explore::class.java))
                "Favourites" -> startActivity(Intent(this, Favourites::class.java))
            }
        }

        // Row 2: Activities (no All) -> open Explore with activity chip selected
        val activityItems = deriveActivitiesNoAll(parks).map { CategoryItem(it) }
        setupHorizontalRecycler(R.id.recyclerSection2, activityItems) { item, _ ->
            startActivity(
                Intent(this, Explore::class.java)
                    .putExtra(Explore.EXTRA_ACTIVITY_FILTER, item.name)
                    .putExtra(Explore.EXTRA_FEATURE_FILTER, "All")
            )
        }

        // Row 3: Features (no All) -> open Explore with feature chip selected
        val featureItems = deriveFeaturesNoAll(parks).map { CategoryItem(it) }
        setupHorizontalRecycler(R.id.recyclerSection3, featureItems) { item, _ ->
            startActivity(
                Intent(this, Explore::class.java)
                    .putExtra(Explore.EXTRA_FEATURE_FILTER, item.name)
                    .putExtra(Explore.EXTRA_ACTIVITY_FILTER, "All")
            )
        }
    }

    // --- Helpers ---

    private fun setupHorizontalRecycler(
        recyclerId: Int,
        items: List<CategoryItem>,
        onClick: (CategoryItem, Boolean) -> Unit
    ) {
        val rv = findViewById<RecyclerView>(recyclerId)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val adapter = CategoryButtonAdapter(this, onClick)
        rv.adapter = adapter
        adapter.submitList(items)

        val space = (8 * resources.displayMetrics.density).toInt() // 8dp
        if (rv.itemDecorationCount == 0) {
            rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val pos = parent.getChildAdapterPosition(view)
                    outRect.right = space
                    if (pos == 0) outRect.left = space
                    outRect.top = space / 2
                    outRect.bottom = space / 2
                }
            })
        }

        LinearSnapHelper().attachToRecyclerView(rv)
        rv.setHasFixedSize(true)
    }

    // Derive lists with "All"/blank removed
    private fun deriveActivitiesNoAll(parks: List<Park>): List<String> =
        parks.asSequence()
            .flatMap { it.activities.asSequence() }
            .mapNotNull { it?.trim() }
            .filter { it.isNotEmpty() && !it.equals("all", ignoreCase = true) }
            .distinct()
            .toList()

    private fun deriveFeaturesNoAll(parks: List<Park>): List<String> =
        parks.asSequence()
            .flatMap { it.features.asSequence() }
            .mapNotNull { it?.trim() }
            .filter { it.isNotEmpty() && !it.equals("all", ignoreCase = true) }
            .distinct()
            .toList()
}
