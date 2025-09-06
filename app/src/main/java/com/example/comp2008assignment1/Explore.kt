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

class Explore : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explore)

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

        // RecyclerView setup
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerParks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ParkAdapter { park ->
            if (park.isFavourite) {
                Toast.makeText(this, "You favourited ${park.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You unfavourited ${park.name}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        // Example park data
        val sampleParks = listOf(
            Park(
                name = "Kings Park",
                location = "Perth, WA",
                activities = listOf("Walking", "Picnics", "Botanic Gardens"),
                features = listOf("Playground", "Lookout", "Cafe")
            ),
            Park(
                name = "Hyde Park",
                location = "Sydney, NSW",
                activities = listOf("Jogging", "Relaxing"),
                features = listOf("Fountain", "Historic Trees")
            ),
            Park(
                name = "Centenary Park",
                location = "Melbourne, VIC",
                activities = emptyList(),
                features = listOf("Dog-friendly", "BBQ area")
            )
        )

        adapter.setItems(sampleParks) // show them
    }
}
