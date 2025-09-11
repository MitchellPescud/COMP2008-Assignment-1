package com.example.comp2008assignment1

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

// Activity for the "Explore" page of the app.
// Displays all available parks from ParkRepository, shows their details
// Lets the user favourite/unfavourite them.

class Explore : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParkAdapter
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup

    private var allParks: List<Park> = emptyList()
    private var currentQuery: String = ""
    private var currentCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge layout (draw under system bars)
        setContentView(R.layout.activity_explore)

        // Handle padding for system bars (status/nav bar areas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Toolbar setup ---
        val toolbar = findViewById<MaterialToolbar>(R.id.Navigation_Bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Show back arrow
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title (use custom TextView instead)

        // Handle back arrow click → navigate back to MainActivity
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close Explore activity
        }

        // -- Views
        searchView = findViewById(R.id.searchView)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerParks)

        // --- RecyclerView setup for displaying parks ---
        recyclerView.layoutManager = LinearLayoutManager(this) // Vertical scrolling list

        // Create adapter with callback when user clicks favourite button
        adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                // User just favourited → show toast
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                // User unfavourited → show toast
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        // --- Load data from repository ---
        // Get all parks from ParkRepository
        allParks = ParkRepository.getAll()

        // Build chips from categories present in data
        buildCategoryChips(deriveCategories(allParks))

        // Serch listener (live filter)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // Category change listener (live filter)
        chipGroup.setOnCheckedStateChangeListener { group,  _ ->
            val checkedId = group.checkedChipId
            currentCategory = group.findViewById<Chip>(checkedId)?.text?.toString() ?: "All"
            applyFilters()
        }

        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        // Refresh favourite icons after returning from elsewhere
        applyFilters()
    }
    private fun applyFilters() {
        val favs = FavouriteStore.getFavourites(this)

        // Text match across name, location, activities, features
        val filtered = allParks.filter { p ->
            val textMatch = currentQuery.isBlank() ||
                    p.name.contains(currentQuery, ignoreCase = true) ||
                    p.location.contains(currentQuery, ignoreCase = true) ||
                    p.activities.any { it.contains(currentQuery, ignoreCase = true) } ||
                    p.features.any {
                        it.contains(currentQuery, ignoreCase = true)
                    }

            val catMatch = (currentCategory == "All") ||
                    p.activities.any { it.equals(currentCategory, ignoreCase = true) } ||
                    p.features.any {
                        it.equals(currentCategory, ignoreCase = true)
                    }

            textMatch && catMatch
        }.map { it.copy(isFavourite = favs.contains(it.parkID)) }

        adapter.setItems(filtered)
        }

    private fun deriveCategories(parks: List<Park>): List<String> {
        val set = linkedSetOf("All")

        parks.forEach { p ->
            p.activities.forEach { if (it.isNotBlank()) set.add(it.trim()) }
            p.features.forEach { if (it.isNotBlank()) set.add(it.trim()) }
        }

        return set.toList()
    }

    private fun buildCategoryChips(categories: List<String>) {
        chipGroup.removeAllViews()
        categories.forEachIndexed { idx, cat ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = cat
                isCheckable = true
                isCheckedIconVisible = false
            }
            chipGroup.addView(chip)
            if (idx == 0) chip.isChecked = true // "All" preselected
        }
    }
}
