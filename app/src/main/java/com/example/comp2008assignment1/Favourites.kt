package com.example.comp2008assignment1

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class Favourites : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParkAdapter
    private lateinit var searchView: SearchView
    private lateinit var chipGroupActivities: ChipGroup
    private lateinit var chipGroupFeatures: ChipGroup
    private lateinit var emptyView: TextView

    private var favParks: List<Park> = emptyList()
    private var currentQuery: String = ""
    private var currentActivity: String = "All"
    private var currentFeature: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favourites)

        // Handle edge-to-edge padding for system bars (status/nav bar areas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        // Restore state if available
        savedInstanceState?.let {
            currentActivity = it.getString("currentActivity", "All")
            currentFeature  = it.getString("currentFeature",  "All")
            currentQuery    = it.getString("currentQuery", "")
        }

        // --- Toolbar setup ---
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
        chipGroupActivities = findViewById(R.id.chipGroupActivities)
        chipGroupFeatures   = findViewById(R.id.chipGroupFeatures)
        recyclerView        = findViewById(R.id.recyclerParks)
        emptyView           = findViewById(R.id.textEmpty)

        // RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
                // Favourites changed; refresh lists/chips
                favParks = buildFavourites()
                rebuildChipsAndSelections()
                applyFilters()
            }
        }
        recyclerView.adapter = adapter

        // Search behaviour
        if (currentQuery.isNotBlank()) searchView.setQuery(currentQuery, false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // Load initial favourite parks
        favParks = buildFavourites()
        buildActivityChips(deriveActivities(favParks))
        buildFeatureChips(deriveFeatures(favParks))

        // Reselect previous chips if applicable
        selectChipByText(chipGroupActivities, currentActivity) ||
                selectChipByText(chipGroupActivities, "All")
        selectChipByText(chipGroupFeatures, currentFeature) ||
                selectChipByText(chipGroupFeatures, "All")

        // Chip listeners
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

    override fun onResume() {
        super.onResume()
        // Update if favourites changed elsewhere
        val beforeIds = favParks.map { it.parkID }.toSet()
        favParks = buildFavourites()
        val afterIds = favParks.map { it.parkID }.toSet()

        if (beforeIds != afterIds) {
            rebuildChipsAndSelections()
        }
        applyFilters()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentActivity", currentActivity)
        outState.putString("currentFeature", currentFeature)
        outState.putString("currentQuery", currentQuery)
    }

    // Filtering Logic
    private fun applyFilters() {
        val filtered = favParks.filter { p ->
            val textMatch = currentQuery.isBlank() ||
                    p.name.contains(currentQuery, true) ||
                    p.location.contains(currentQuery, true) ||
                    p.activities.any { it.contains(currentQuery, true) } ||
                    p.features.any   { it.contains(currentQuery, true) }

            val activityMatch = (currentActivity == "All") ||
                    p.activities.any { it.equals(currentActivity, true) }

            val featureMatch = (currentFeature == "All") ||
                    p.features.any { it.equals(currentFeature, true) }

            textMatch && activityMatch && featureMatch
        }.map { it.copy(isFavourite = true) }

        adapter.setItems(filtered)
        updateEmptyState(filtered)
    }

    private fun updateEmptyState(items: List<Park>) {
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // Get favourites from storage
    private fun buildFavourites(): List<Park> =
        ParkRepository.getAll()
            .filter { FavouriteStore.getFavourites(this).contains(it.parkID) }
            .map { it.copy(isFavourite = true) }

    // Chip builders
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

    // Derive Categories from list of parks
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

    // Helpers
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

    private fun rebuildChipsAndSelections() {
        // Rebuild chip lists from current favourites
        val acts = deriveActivities(favParks)
        val feats = deriveFeatures(favParks)
        buildActivityChips(acts)
        buildFeatureChips(feats)

        // Try to keep previous selections if still present, else reset to “All”
        if (!selectChipByText(chipGroupActivities, currentActivity)) {
            currentActivity = "All"
            selectChipByText(chipGroupActivities, "All")
        }
        if (!selectChipByText(chipGroupFeatures, currentFeature)) {
            currentFeature = "All"
            selectChipByText(chipGroupFeatures, "All")
        }
    }
}
