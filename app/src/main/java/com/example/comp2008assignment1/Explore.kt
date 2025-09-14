package com.example.comp2008assignment1

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

// Explore Activity
// Shows all available categories of parks in a grid (e.g. All, Playgrounds, Sports, Favourites).
// Clicking a category opens ParkListActivity or Favourites.
class Explore : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore)

        // Adjust layout for status/nav bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // --- Toolbar setup with back arrow ---
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Back arrow to return to MainActivity
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // --- RecyclerView setup for category grid ---
        recycler = findViewById(R.id.recyclerCategories)
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Build categories list with counts from ParkRepository
        val parks = ParkRepository.getAll()
        val categories = deriveCategoriesWithCounts(parks)

        // Add Favourites as a special card at the end
        val allWithFav = categories + CategoryItem("Favourites", 0, android.R.drawable.btn_star)

        // Bind to adapter (handles click events)
        adapter = CategoryAdapter(allWithFav) { category ->
            when (category.name) {
                "Favourites" -> startActivity(Intent(this, Favourites::class.java))
                "All" -> startActivity(Intent(this, ParkListActivity::class.java))
                else -> {
                    val intent = Intent(this, ParkListActivity::class.java)
                    intent.putExtra("extra_initial_category", category.name) // literal key
                    startActivity(intent)
                }
            }
        }
        recycler.adapter = adapter
    }

    // Build unique categories (activities + features) and count how many parks belong to each
    private fun deriveCategoriesWithCounts(parks: List<Park>): List<CategoryItem> {
        val counts = linkedMapOf<String, Int>()
        counts["All"] = parks.size // "All" always includes all parks

        val unique = linkedSetOf<String>()
        parks.forEach { p ->
            p.activities.forEach { if (it.isNotBlank()) unique.add(it.trim()) }
            p.features.forEach { if (it.isNotBlank()) unique.add(it.trim()) }
        }

        // Count how many parks belong to each category
        unique.forEach { cat ->
            counts[cat] = parks.count { p ->
                p.activities.any { it.equals(cat, true) } ||
                        p.features.any { it.equals(cat, true) }
            }
        }

        // Convert into list of CategoryItems
        return counts.map { (name, count) ->
            CategoryItem(
                name = name,
                count = count,
                iconRes = android.R.drawable.ic_menu_myplaces // built-in system icon
            )
        }
    }
}
