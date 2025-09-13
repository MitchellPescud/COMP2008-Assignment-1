package com.example.comp2008assignment1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import android.widget.RatingBar
import android.widget.Button
import android.widget.ImageView
import android.util.Log
import java.util.Locale

class ParkDetailedView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_park)

        // Handle edge-to-edge padding for system bars (status/nav bar areas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Toolbar setup ---
        val toolbar = findViewById<MaterialToolbar>(R.id.Navigation_Bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)      // show back arrow
        supportActionBar?.setDisplayShowTitleEnabled(false)    // using custom title view

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()            // pop this activity, go back to previous
        }

        // --- Get park id from Intent ---
        val parkId = intent.getStringExtra("PARK_ID")
        if (parkId == null) {
            Toast.makeText(this, "Park id missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- Lookup park ---
        val park = ParkRepository.getById(parkId)
        if (park == null) {
            Toast.makeText(this, "Park not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // --- Bind UI ---
        findViewById<TextView>(R.id.textName).text = park.name
        findViewById<TextView>(R.id.textLocation).text = park.location
        findViewById<TextView>(R.id.textActivities).text =
            "Activities: ${park.activities.joinToString(" • ")}"
        findViewById<TextView>(R.id.textFeatures).text =
            "Features: ${park.features.joinToString(" • ")}"
        findViewById<TextView>(R.id.textDescription).text = park.description
        findViewById<TextView>(R.id.textRating).text = "Average Rating: ${park.rating}/5.0"

        val avgRatingBar = findViewById<RatingBar>(R.id.ratingBar)
        val userRatingBar = findViewById<RatingBar>(R.id.ratingBarUser)
        val btnVisited = findViewById<Button>(R.id.btnVisited)
        val parkImage = ParkRepository.getById(parkId)

        // --- Park header image (with fallback) ---
        val imageView = findViewById<ImageView>(R.id.imagePark)
        val resName = park.parkID.lowercase(Locale.ROOT)
        val photoResId = resources.getIdentifier(resName, "drawable", packageName)

        if (photoResId != 0) {
            imageView.setImageResource(photoResId)
        } else {
            Log.w("ParkDetailedView", "Drawable not found for parkID='$resName'. Using sample_park.")
            imageView.setImageResource(R.drawable.sample_park) // fallback/default
        }

        // --- Persist visited + rating ---
        val prefs = getSharedPreferences("item_detail", Context.MODE_PRIVATE)
        val visitedKey = "visited_$parkId"
        val userRatingKey = "user_rating_$parkId"

        // --- Show average rating ---
        avgRatingBar.rating = park.rating.toFloat()
        avgRatingBar.setIsIndicator(true)

        // --- Restore user’s saved rating ---
        val savedUserRating = prefs.getFloat(userRatingKey, 0f)
        userRatingBar.rating = savedUserRating

        // --- Save when user changes it ---
        userRatingBar.setOnRatingBarChangeListener { _, newRating, _ ->
            prefs.edit().putFloat(userRatingKey, newRating).apply()
            Toast.makeText(this, "Your rating saved: $newRating", Toast.LENGTH_SHORT).show()
        }

        val alreadyVisited = prefs.getBoolean(visitedKey, false)
        btnVisited.text = if (alreadyVisited) "Visited ✓" else "Mark Visited"

        btnVisited.setOnClickListener {
            val newVisited = !prefs.getBoolean(visitedKey, false)
            prefs.edit().putBoolean(visitedKey, newVisited).apply()
            btnVisited.text = if (newVisited) "Visited ✓" else "Mark Visited"
            Toast.makeText(
                this,
                if (newVisited) "Marked visited" else "Unmarked visited",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}