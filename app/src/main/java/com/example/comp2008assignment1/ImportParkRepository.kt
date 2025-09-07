package com.example.comp2008assignment1

import android.app.Application
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

// Repository object that manages the list of parks
// Responsible for importing parks from the assets file (parks.txt)
// Provides access to the in-memory list of Park objects

object ParkRepository {
    private const val TAG = "ParkRepository"       // Tag for logging
    private const val FILE_NAME = "parks.txt"      // Asset file name

    // Backing storage for Park objects
    private val parks = mutableListOf<Park>()

    // Flag to avoid loading the same data multiple times
    private var loaded = false

    /**
     * Initialise repository by loading parks from assets/parks.txt
     * This should typically be called once (e.g., in Application.onCreate())
     */
    fun init(app: Application) {
        // Prevent reloading if already initialised
        if (loaded) return
        try {
            // Open parks.txt from assets folder
            app.assets.open(FILE_NAME).use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.forEach { raw ->
                        val line = raw.trim()

                        // Ignore blank lines or comment lines starting with "#"
                        if (line.isEmpty() || line.startsWith("#")) return@forEach

                        // Expect line format: id | name | location | activities | features
                        val parts = line.split("|", limit = 5).map { it.trim() }

                        // Skip malformed lines that don't have 5 fields
                        if (parts.size < 5) {
                            Log.w(TAG, "Skipping malformed line: $line")
                            return@forEach
                        }

                        // Extract fields from the line
                        val parkID = parts[0]
                        val name = parts[1]
                        val location = parts[2]
                        val activities = splitList(parts[3])   // Comma-separated values → List<String>
                        val features = splitList(parts[4])     // Comma-separated values → List<String>

                        // Create Park object and add to in-memory list
                        parks.add(
                            Park(
                                parkID = parkID,
                                name = name,
                                location = location,
                                activities = activities,
                                features = features,
                                isFavourite = false // Default when first loaded
                            )
                        )
                    }
                }
            }

            // Mark as loaded to prevent duplicate imports
            loaded = true
            Log.d(TAG, "Loaded ${parks.size} parks from $FILE_NAME")

        } catch (t: Throwable) {
            // Log error if reading/parsing fails
            Log.e(TAG, "Failed to load $FILE_NAME: ${t.message}", t)
        }
    }

    // Returns the full list of loaded parks
    fun getAll(): List<Park> = parks

     // Splits a comma-separated string into a cleaned List<String>
     // Removes blanks and trims spaces around entries
    private fun splitList(field: String): List<String> =
        if (field.isBlank()) emptyList()
        else field.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
