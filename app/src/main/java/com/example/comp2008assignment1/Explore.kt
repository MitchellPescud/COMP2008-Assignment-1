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
// Lets the user favourite / un-favourite them.
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore)

        // Handle edge-to-edge padding for system bars (status/nav bar areas)
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
        toolbar.setNavigationOnClickListener { // Handle back arrow click
            val intent = Intent(this, MainActivity::class.java) // Navigate back to MainActivity
            startActivity(intent)
            finish() // Close Explore activity
        }

        // --- View lookups ---
        searchView = findViewById(R.id.searchView)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerParks)

        // --- RecyclerView setup for displaying parks ---
        recyclerView.layoutManager = LinearLayoutManager(this) // Vertical scrolling list
        adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                // User just favourited → show toast
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                // User un-favourited → show toast
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        // Load data from repository
        allParks = ParkRepository.getAll() // Get all parks from ParkRepository

        // Build chips from categories present in data
        buildCategoryChips(deriveCategories(allParks))

        // Live text search: filter on every keystroke (case-insensitive)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // Live category filter: chips are single-select; "All" shows everything
        chipGroup.setOnCheckedStateChangeListener { group,  _ ->
            val checkedId = group.checkedChipId
            currentCategory = group.findViewById<Chip>(checkedId)?.text?.toString() ?: "All"
            applyFilters()
        }

        // Initial list
        applyFilters()
    }

    override fun onResume() {
        super.onResume()

        // Refresh list to reflect any favourite changes made elsewhere
        applyFilters()
    }

    // --- Filtering logic ---

    // Applies current search + category filter, merges favourite state, and updates the adapter
    private fun applyFilters() {
        val favs = FavouriteStore.getFavourites(this)

        val filtered = allParks.filter { p ->
            // Text match across name, location, activities, features
            val textMatch = currentQuery.isBlank() ||
                    p.name.contains(currentQuery, ignoreCase = true) ||
                    p.location.contains(currentQuery, ignoreCase = true) ||
                    p.activities.any { it.contains(currentQuery, ignoreCase = true) } ||
                    p.features.any {
                        it.contains(currentQuery, ignoreCase = true)
                    }

            // Category match across activities OR features (treat either as a "category")
            val catMatch = (currentCategory == "All") ||
                    p.activities.any { it.equals(currentCategory, ignoreCase = true) } ||
                    p.features.any {
                        it.equals(currentCategory, ignoreCase = true)
                    }

            textMatch && catMatch
        }
            // Copy list so each item reflects favourite state
            .map { it.copy(isFavourite = favs.contains(it.parkID)) }

        adapter.setItems(filtered)
        }

    // Derives a unique, ordered category list from parks' activities + features
    private fun deriveCategories(parks: List<Park>): List<String> {
        // First item is always "All"
        val set = linkedSetOf("All")

        parks.forEach { p ->
            p.activities.forEach { if (it.isNotBlank()) set.add(it.trim()) }
            p.features.forEach { if (it.isNotBlank()) set.add(it.trim()) }
        }

        return set.toList()
    }

    // Creates a chip for each category and adds it to the ChipGroup
    private fun buildCategoryChips(categories: List<String>) {
        chipGroup.removeAllViews()
        categories.forEachIndexed { idx, cat ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = cat
                isCheckable = true // Enable selection behaviour
                isCheckedIconVisible = false
            }
            chipGroup.addView(chip)
            if (idx == 0) chip.isChecked = true // "All" preselected
        }
    }
}
