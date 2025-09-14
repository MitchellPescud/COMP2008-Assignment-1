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

    companion object {
        const val EXTRA_ACTIVITY_FILTER = "extra_activity_filter"
        const val EXTRA_FEATURE_FILTER  = "extra_feature_filter"
    }

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

        // Handle edge-to-edge padding for system bars (status/nav bar areas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Show back arrow
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title (use custom TextView instead)
        toolbar.setNavigationOnClickListener { // Handle back arrow click
            val intent = Intent(this, MainActivity::class.java) // Navigate back to MainActivity
            startActivity(intent)
            finish() // Close Explore activity
        }

        // --- View lookups ---
        searchView = findViewById(R.id.searchView)
        chipGroupActivities = findViewById(R.id.chipGroupActivities)
        chipGroupFeatures   = findViewById(R.id.chipGroupFeatures)
        recyclerView        = findViewById(R.id.recyclerParks)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ParkAdapter { park ->
            if (park.isFavourite)
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter

        allParks = ParkRepository.getAll()

        // 1) Start with defaults or rotation restore
        savedInstanceState?.let {
            currentActivity = it.getString("currentActivity", "All")
            currentFeature  = it.getString("currentFeature",  "All")
        }

        // 2) Intents from MainActivity take precedence if present
        intent.getStringExtra(EXTRA_ACTIVITY_FILTER)?.let { currentActivity = it }
        intent.getStringExtra(EXTRA_FEATURE_FILTER )?.let { currentFeature  = it }

        // Build chips (these include "All" at index 0)
        buildActivityChips(deriveActivities(allParks))
        buildFeatureChips(deriveFeatures(allParks))

        // Select the requested chips (fallback to "All" if not found)
        selectChipByText(chipGroupActivities, currentActivity) ||
                selectChipByText(chipGroupActivities, "All")
        selectChipByText(chipGroupFeatures, currentFeature) ||
                selectChipByText(chipGroupFeatures, "All")

        // Listeners (unchanged)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })
        chipGroupActivities.setOnCheckedStateChangeListener { group, _ ->
            val id = group.checkedChipId
            currentActivity = group.findViewById<Chip>(id)?.text?.toString() ?: "All"
            applyFilters()
        }
        chipGroupFeatures.setOnCheckedStateChangeListener { group, _ ->
            val id = group.checkedChipId
            currentFeature = group.findViewById<Chip>(id)?.text?.toString() ?: "All"
            applyFilters()
        }

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

    // Helper to select a chip by its visible text (case-insensitive).
    private fun selectChipByText(group: ChipGroup, text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            if (chip.text.toString().equals(text, ignoreCase = true)) {
                chip.isChecked = true
                return true
            }
        }
        return false
    }
}
