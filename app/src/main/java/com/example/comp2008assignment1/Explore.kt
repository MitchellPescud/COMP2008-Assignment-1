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

class Explore : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore)

        // Handle insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Back arrow → MainActivity
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // RecyclerView setup
        recycler = findViewById(R.id.recyclerCategories)
        recycler.layoutManager = GridLayoutManager(this, 2)

        val parks = ParkRepository.getAll()
        val categories = deriveCategoriesWithCounts(parks)

        // Add Favourites card at the end
        val allWithFav = categories + CategoryItem("Favourites", 0, android.R.drawable.btn_star)

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

    // Build categories with counts
    private fun deriveCategoriesWithCounts(parks: List<Park>): List<CategoryItem> {
        val counts = linkedMapOf<String, Int>()
        counts["All"] = parks.size

        val unique = linkedSetOf<String>()
        parks.forEach { p ->
            p.activities.forEach { if (it.isNotBlank()) unique.add(it.trim()) }
            p.features.forEach { if (it.isNotBlank()) unique.add(it.trim()) }
        }

        unique.forEach { cat ->
            counts[cat] = parks.count { p ->
                p.activities.any { it.equals(cat, true) } ||
                        p.features.any { it.equals(cat, true) }
            }
        }

        return counts.map { (name, count) ->
            CategoryItem(
                name = name,
                count = count,
                iconRes = android.R.drawable.ic_menu_myplaces // built-in system icon
            )
        }
    }
}
