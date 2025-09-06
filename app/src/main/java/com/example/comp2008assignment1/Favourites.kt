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

class Favourites : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favourites)

        // Handle edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up toolbar with back button
        val toolbar = findViewById<MaterialToolbar>(R.id.Navigation_Bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // show back arrow
        supportActionBar?.setDisplayShowTitleEnabled(false) // since you use custom TextView for title

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // close this activity
        }

        // RecyclerView (local)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerParks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter (local). NOTE: use recyclerView.adapter inside the lambda.
        val adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
                val favsNow = buildFavourites()
                (recyclerView.adapter as ParkAdapter).setItems(favsNow)
                updateEmptyState(favsNow, recyclerView)
            }
        }
        recyclerView.adapter = adapter

        // Initial data + empty state
        val initialItems = buildFavourites()
        adapter.setItems(initialItems)
        updateEmptyState(initialItems, recyclerView)
    }

    override fun onResume() {
        super.onResume()
        // Refresh using locals retrieved again
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerParks)
        val adapter = recyclerView.adapter as ParkAdapter
        val favsNow = buildFavourites()
        adapter.setItems(favsNow)
        updateEmptyState(favsNow, recyclerView)
    }

    // --- Helpers ---

    private fun buildFavourites(): List<Park> =
        ParkRepository.getAll()
            .filter { FavouriteStore.getFavourites(this).contains(it.parkID) }
            .map { it.copy(isFavourite = true) }

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