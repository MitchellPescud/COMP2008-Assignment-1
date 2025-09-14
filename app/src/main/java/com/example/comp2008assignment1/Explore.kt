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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParkAdapter
    private lateinit var searchView: SearchView
    private lateinit var chipGroupActivities: ChipGroup
    private lateinit var chipGroupFeatures: ChipGroup
    private var allParks: List<Park> = emptyList()
    private var currentQuery: String = ""
    private var currentActivity: String = "All"
    private var currentFeature: String = "All"

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

        // Restore state if available
        savedInstanceState?.let {
            currentActivity = it.getString("currentActivity", "All")
            currentFeature = it.getString("currentFeature", "All")
        }

        // --- Toolbar setup ---
        val toolbar = findViewById<MaterialToolbar>(R.id.Navigation_Bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Back arrow to return to MainActivity
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // --- View lookups ---
        searchView = findViewById(R.id.searchView)
        chipGroupActivities = findViewById(R.id.chipGroupActivities)
        chipGroupFeatures = findViewById(R.id.chipGroupFeatures)
        recyclerView = findViewById(R.id.recyclerParks)

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
        recyclerView.adapter = adapter

        // Load data from repository
        allParks = ParkRepository.getAll() // Get all parks from ParkRepository

        // Build chips separately
        buildActivityChips(deriveActivities(allParks))
        buildFeatureChips(deriveFeatures(allParks))

        // Restore selected for rotation
        chipGroupActivities.findViewWithTag<Chip>(currentActivity)?.isChecked = true
        chipGroupFeatures.findViewWithTag<Chip>(currentFeature)?.isChecked = true

        // Live text search: filter on every keystroke (case-insensitive)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // Activity filter change
        chipGroupActivities.setOnCheckedStateChangeListener { group, _ ->
            val id = group.checkedChipId
            currentActivity = group.findViewById<Chip>(id)?.text?.toString() ?: "All"
            applyFilters()
        }

        // Feature filter change
        chipGroupFeatures.setOnCheckedStateChangeListener { group, _ ->
            val id = group.checkedChipId
            currentFeature = group.findViewById<Chip>(id)?.text?.toString() ?: "All"
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
            // Text match
            val textMatch = currentQuery.isBlank() ||
                    p.name.contains(currentQuery, ignoreCase = true) ||
                    p.location.contains(currentQuery, ignoreCase = true) ||
                    p.activities.any { it.contains(currentQuery, ignoreCase = true) } ||
                    p.features.any { it.contains(currentQuery, ignoreCase = true) }

            // Activity match (if not “All”, require park to contain that activity)
            val activityMatch = (currentActivity == "All") ||
                    p.activities.any { it.equals(currentActivity, ignoreCase = true) }

            // Feature match (if not “All”, require park to contain that feature)
            val featureMatch = (currentFeature == "All") ||
                    p.features.any { it.equals(currentFeature, ignoreCase = true) }

            textMatch && activityMatch && featureMatch
        }
            .map { it.copy(isFavourite = favs.contains(it.parkID)) }

        adapter.setItems(filtered)
    }

    // ---- Chip builders & set derivation ----
    private fun buildActivityChips(activities: List<String>) {
        chipGroupActivities.removeAllViews()
        activities.forEachIndexed { idx, item ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = item
                tag = item
                isCheckable = true
                isCheckedIconVisible = false
            }
            chipGroupActivities.addView(chip)
            if (idx == 0) chip.isChecked = true // “All”
        }
    }

    private fun buildFeatureChips(features: List<String>) {
        chipGroupFeatures.removeAllViews()
        features.forEachIndexed { idx, item ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = item
                tag = item
                isCheckable = true
                isCheckedIconVisible = false
            }
            chipGroupFeatures.addView(chip)
            if (idx == 0) chip.isChecked = true // “All”
        }
    }

    private fun deriveActivities(parks: List<Park>): List<String> {
        val set = linkedSetOf("All")
        parks.forEach { p -> p.activities.forEach { if (it.isNotBlank()) set.add(it.trim()) } }
        return set.toList()
    }

    private fun deriveFeatures(parks: List<Park>): List<String> {
        val set = linkedSetOf("All")
        parks.forEach { p -> p.features.forEach { if (it.isNotBlank()) set.add(it.trim()) } }
        return set.toList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentActivity", currentActivity)
        outState.putString("currentFeature", currentFeature)
    }
}
