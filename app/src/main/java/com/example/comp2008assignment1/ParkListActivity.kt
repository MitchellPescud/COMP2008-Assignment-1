package com.example.comp2008assignment1

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

// ParkListActivity
// Displays a filtered list of parks (based on search query or category).
// Includes toolbar back arrow, live search, and category chips.
class ParkListActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchView: SearchView
    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParkAdapter

    private var allParks: List<Park> = emptyList()
    private var currentQuery: String = ""
    private var currentCategory: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_park_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        // --- Toolbar with back arrow ---
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Back arrow to Explore
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, Explore::class.java))
            finish()
        }

        // --- Views ---
        searchView = findViewById(R.id.searchView)
        chipGroup = findViewById(R.id.chipGroup)
        recyclerView = findViewById(R.id.recyclerParks)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // RecyclerView adapter
        adapter = ParkAdapter { park ->
            Toast.makeText(
                this,
                if (park.isFavourite) "You favourited ${park.name}" else "You unfavourited ${park.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
        recyclerView.adapter = adapter

        // Load parks
        allParks = ParkRepository.getAll()

        // Build category chips from park data
        buildCategoryChips(deriveCategories(allParks))

        // Apply initial category filter if Explore passed one
        currentCategory = intent.getStringExtra("extra_initial_category") ?: "All"
        selectChip(currentCategory)

        // --- Search ---
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })

        // --- Chips ---
        chipGroup.setOnCheckedStateChangeListener { group, _ ->
            val checkedId = group.checkedChipId
            currentCategory = group.findViewById<Chip>(checkedId)?.text?.toString() ?: "All"
            applyFilters()
        }

        // Initial filter
        applyFilters()
    }

    override fun onResume() {
        super.onResume()

        // Refresh results (in case favourites changed)
        applyFilters()
    }

    // Filter parks by query + category + favourite state
    private fun applyFilters() {
        val favs = FavouriteStore.getFavourites(this)
        val filtered = allParks.filter { p ->
            val textMatch = currentQuery.isBlank() ||
                    p.name.contains(currentQuery, true) ||
                    p.location.contains(currentQuery, true) ||
                    p.activities.any { it.contains(currentQuery, true) } ||
                    p.features.any { it.contains(currentQuery, true) }

            val catMatch = (currentCategory == "All") ||
                    p.activities.any { it.equals(currentCategory, true) } ||
                    p.features.any { it.equals(currentCategory, true) }

            textMatch && catMatch
        }.map { it.copy(isFavourite = favs.contains(it.parkID)) }

        adapter.setItems(filtered)
    }

    // Collect unique categories from activities + features
    private fun deriveCategories(parks: List<Park>): List<String> {
        val set = linkedSetOf("All")
        parks.forEach { p ->
            p.activities.forEach { if (it.isNotBlank()) set.add(it.trim()) }
            p.features.forEach { if (it.isNotBlank()) set.add(it.trim()) }
        }
        return set.toList()
    }

    // Build chip UI elements dynamically
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
            if (idx == 0) chip.isChecked = true
        }
    }

    // Select a chip (used for initial category)
    private fun selectChip(name: String) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip ?: continue
            chip.isChecked = chip.text.toString().equals(name, true)
        }
    }
}
