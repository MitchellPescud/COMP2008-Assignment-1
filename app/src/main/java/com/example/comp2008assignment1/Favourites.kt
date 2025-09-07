package com.example.comp2008assignment1

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import android.widget.TextView
import android.view.View

// Activity that displays the list of all favourited parks
// Shows parks saved as favourites and allows unfavouriting them
// Also displays an "empty state" message if no favourites exist

class Favourites : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge UI layout
        setContentView(R.layout.activity_favourites)

        // Handle padding for system bars (status/nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Toolbar setup with back button ---
        val toolbar = findViewById<MaterialToolbar>(R.id.Navigation_Bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Show back arrow
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide default title (we use custom TextView)

        // Handle back arrow click → return to MainActivity
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close this activity
        }

        // --- RecyclerView setup for favourites list ---
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerParks)
        recyclerView.layoutManager = LinearLayoutManager(this) // Vertical list layout

        // Create adapter with callback when favourite button clicked
        val adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                // User just favourited the park → show toast
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                // User unfavourited → show toast, update displayed list
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
                val favsNow = buildFavourites()
                (recyclerView.adapter as ParkAdapter).setItems(favsNow)
                updateEmptyState(favsNow, recyclerView)
            }
        }
        recyclerView.adapter = adapter

        // --- Load initial favourites and set empty state ---
        val initialItems = buildFavourites()
        adapter.setItems(initialItems)
        updateEmptyState(initialItems, recyclerView)
    }

    override fun onResume() {
        super.onResume()
        // Refresh favourites whenever returning to this screen
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerParks)
        val adapter = recyclerView.adapter as ParkAdapter
        val favsNow = buildFavourites()
        adapter.setItems(favsNow)
        updateEmptyState(favsNow, recyclerView)
    }

    // --- Helpers ---

    /**
     * Builds the list of favourited parks.
     * Pulls all parks from repository, filters those in FavouriteStore, and sets isFavourite=true.
     */
    private fun buildFavourites(): List<Park> =
        ParkRepository.getAll()
            .filter { FavouriteStore.getFavourites(this).contains(it.parkID) }
            .map { it.copy(isFavourite = true) }

    /**
     * Updates the empty state.
     * If no favourites, show "No favourited items yet" text and hide RecyclerView.
     * Otherwise, show RecyclerView and hide the empty message.
     */
    private fun updateEmptyState(items: List<Park>, recyclerView: RecyclerView) {
        val emptyView = findViewById<TextView>(R.id.textEmpty)
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
