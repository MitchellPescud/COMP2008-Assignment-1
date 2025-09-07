package com.example.comp2008assignment1

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast

// Activity for the "Explore" page of the app.
// Displays all available parks from ParkRepository, shows their details
// Lets the user favourite/unfavourite them.

class Explore : AppCompatActivity() {
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

        // --- RecyclerView setup for displaying parks ---
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerParks)
        recyclerView.layoutManager = LinearLayoutManager(this) // Vertical scrolling list

        // Create adapter with callback when user clicks favourite button
        val adapter = ParkAdapter { park ->
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
        val allParks = ParkRepository.getAll()

        // Get set of favourited IDs from FavouriteStore
        val favs = FavouriteStore.getFavourites(this)

        // Mark each park with its favourite state (true/false)
        val parksWithFavs = allParks.map { it.copy(isFavourite = favs.contains(it.parkID)) }

        // Pass final list to adapter for display
        adapter.setItems(parksWithFavs)
    }

    override fun onResume() {
        super.onResume()
        // Could refresh favourites here like in Favourites.kt if needed
    }
}
