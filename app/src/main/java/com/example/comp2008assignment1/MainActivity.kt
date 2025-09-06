package com.example.comp2008assignment1

import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Buttons
        val exploreButton = findViewById<MaterialButton>(R.id.btnExplore)
        val favouritesButton = findViewById<MaterialButton>(R.id.btnFavourites)

        // Open ExploreActivity
        exploreButton.setOnClickListener {
            val intent = Intent(this, Explore::class.java)
            startActivity(intent)
        }

        // Open FavouritesActivity
        favouritesButton.setOnClickListener {
            val intent = Intent(this, Favourites::class.java)
            startActivity(intent)
        }
    }
}