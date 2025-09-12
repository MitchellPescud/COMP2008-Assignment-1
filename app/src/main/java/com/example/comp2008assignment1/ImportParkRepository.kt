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
     *
     * Line format (pipes with optional spaces around them):
     * id | name | location | activities | features | description | rating
     */
    fun init(app: Application) {
        if (loaded) return
        try {
            app.assets.open(FILE_NAME).use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.forEachIndexed { idx, raw ->
                        val line = raw.trim()

                        // Ignore blank lines or comment lines starting with "#"
                        if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed

                        // Expect up to 7 fields: id|name|location|activities|features|[description]|[rating]
                        val parts = line.split("|", limit = 7).map { it.trim() }

                        // Must have at least the first 5 fields
                        if (parts.size < 5) {
                            Log.w(TAG, "Skipping malformed line ${idx + 1}: $line")
                            return@forEachIndexed
                        }

                        val parkID = parts[0]
                        val name = parts[1]
                        val location = parts[2]
                        val activities = splitList(parts[3])   // "a, b, c" -> ["a","b","c"]
                        val features = splitList(parts[4])
                        val description = parts.getOrNull(5)?.trim().orEmpty()
                        val rating = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0

                        parks.add(
                            Park(
                                parkID = parkID,
                                name = name,
                                location = location,
                                activities = activities,
                                features = features,
                                description = description,
                                rating = rating,
                                isFavourite = false
                            )
                        )
                    }
                }
            }

            loaded = true
            Log.d(TAG, "Loaded ${parks.size} parks from $FILE_NAME")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load $FILE_NAME: ${t.message}", t)
        }
    }

    // Returns the full list of loaded parks
    fun getAll(): List<Park> = parks

    // Returns a single Park by ID (or null if not found)
    fun getById(parkID: String): Park? {
        return parks.find { it.parkID == parkID }
    }

    // Splits a comma-separated string into a cleaned List<String>
    // Removes blanks and trims spaces around entries
    private fun splitList(field: String): List<String> =
        if (field.isBlank()) emptyList()
        else field.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
