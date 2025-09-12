package com.example.comp2008assignment1

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ItemDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_ID = "extra_id"          // a stable id/string for the park
        const val EXTRA_RATING = "extra_rating"  // optional starting rating
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        // Views in activity_item_detail.xml
        val imgHero = findViewById<ImageView>(R.id.imgHero)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDesc  = findViewById<TextView>(R.id.tvDescription)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnVisited = findViewById<Button>(R.id.btnVisited)

        // Get data from Intent (Strings, not Parcelable)
        val name       = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val description= intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()
        val imageUrl   = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty()
        val id         = intent.getStringExtra(EXTRA_ID).orEmpty()
        val startRating= intent.getFloatExtra(EXTRA_RATING, 0f)

        // Populate UI
        tvTitle.text = name
        tvDesc.text = description
        ratingBar.rating = startRating

        // Persist visited + rating (very simple local storage)
        val prefs = getSharedPreferences("item_detail", Context.MODE_PRIVATE)
        val visitedKey = "visited_$id"
        val ratingKey  = "rating_$id"

        // Restore previous saved state (if any)
        val previousRating = prefs.getFloat(ratingKey, startRating)
        ratingBar.rating = previousRating
        val alreadyVisited = prefs.getBoolean(visitedKey, false)
        btnVisited.text = if (alreadyVisited) "Visited ✓" else "Mark Visited"

        // Save visited toggle
        btnVisited.setOnClickListener {
            val newVisited = !prefs.getBoolean(visitedKey, false)
            prefs.edit().putBoolean(visitedKey, newVisited).apply()
            btnVisited.text = if (newVisited) "Visited ✓" else "Mark Visited"
            Toast.makeText(this, if (newVisited) "Marked visited" else "Unmarked visited", Toast.LENGTH_SHORT).show()
        }

        // Save rating on change
        ratingBar.setOnRatingBarChangeListener { _, newRating, _ ->
            prefs.edit().putFloat(ratingKey, newRating).apply()
        }
    }
}
