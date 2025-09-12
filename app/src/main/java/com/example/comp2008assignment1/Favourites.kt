package com.example.comp2008assignment1

import android.os.Bundle
import android.content.Intent
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
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Favourites : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParkAdapter
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var emptyView: TextView
    private var favParks: List<Park> = emptyList()
    private var currentQuery: String = ""
    private var currentCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favourites)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toolbar + Back Button
        val toolbar = findViewById<MaterialToolbar>(R.id.Navigation_Bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Views
        searchView = findViewById(R.id.searchView)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerParks)
        emptyView = findViewById(R.id.textEmpty)

        // RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
                // Rebuild favourites because the set changed
                favParks = buildFavourites()
                // Categories may change when an item is removed; rebuild chips if needed
                rebuildChipsIfNeeded()
                applyFilters()
            }
        }
        recyclerView.adapter = adapter

        // Search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // Chips
        chipGroup.setOnCheckedStateChangeListener { group, _ ->
            val checkedId = group.checkedChipId
            currentCategory = group.findViewById<Chip>(checkedId)?.text?.toString() ?: "All"
            applyFilters()
        }

        // Initial content
        favParks = buildFavourites()
        buildCategoryChips(deriveCategories(favParks))
        applyFilters()
    }

    override fun onResume() {
        super.onResume()
        // Refresh favourites (e.g., changed in Explore)
        val before = favParks
        favParks = buildFavourites()

        // If the set changed, rebuild chips (categories may differ)
        if (before.map { it.parkID }.toSet() != favParks.map { it.parkID }.toSet()) {
            rebuildChipsIfNeeded()
        }

        applyFilters()
    }

    // --- Core filtering ---

    private fun applyFilters() {
        val filtered = favParks.filter { p ->
            val textMatch = currentQuery.isBlank() ||
                    p.name.contains(currentQuery, ignoreCase = true) ||
                    p.location.contains(currentQuery, ignoreCase = true) ||
                    p.activities.any { it.contains(currentQuery, ignoreCase = true) } ||
                    p.features.any { it.contains(currentQuery, ignoreCase = true) }

            val catMatch = (currentCategory == "All") ||
                    p.activities.any { it.equals(currentCategory, ignoreCase = true) } ||
                    p.features.any { it.equals(currentCategory, ignoreCase = true) }

            textMatch && catMatch
        }
        adapter.setItems(filtered.map { it.copy(isFavourite = true) })
        updateEmptyState(filtered)
    }

    // Build the current favourites list from storage
    private fun buildFavourites(): List<Park> =
        ParkRepository.getAll()
            .filter { FavouriteStore.getFavourites(this).contains(it.parkID) }
            .map { it.copy(isFavourite = true) }

    private fun updateEmptyState(items: List<Park>) {
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // --- Categories & chips ---
    private fun deriveCategories(parks: List<Park>): List<String> {
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
                isCheckable = true
                isCheckedIconVisible = false
            }
            chipGroup.addView(chip)
            if (idx == 0) chip.isChecked = true // "All"
        }
        // Ensure state is consistent if we rebuilt after user had chosen a category
        if (currentCategory != "All") {
            val match = categories.indexOfFirst { it.equals(currentCategory, ignoreCase = true) }
            if (match >= 0) {
                (chipGroup.getChildAt(match) as? Chip)?.isChecked = true
            } else {
                currentCategory = "All"
                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
            }
        }
    }

    private fun rebuildChipsIfNeeded() {
        val cats = deriveCategories(favParks)
        buildCategoryChips(cats)
    }
}
