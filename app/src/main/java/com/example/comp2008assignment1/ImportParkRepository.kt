package com.example.comp2008assignment1

import android.app.Application
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object ParkRepository {
    private const val TAG = "ParkRepository"
    private const val FILE_NAME = "parks.txt"

    private val parks = mutableListOf<Park>()
    private var loaded = false

    fun init(app: Application) {
        if (loaded) return
        try {
            app.assets.open(FILE_NAME).use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.forEach { raw ->
                        val line = raw.trim()
                        if (line.isEmpty() || line.startsWith("#")) return@forEach

                        // Expect 5 fields separated by | : id | name | location | activities | features
                        val parts = line.split("|", limit = 5).map { it.trim() }
                        if (parts.size < 5) {
                            Log.w(TAG, "Skipping malformed line: $line")
                            return@forEach
                        }

                        val parkID = parts[0]
                        val name = parts[1]
                        val location = parts[2]
                        val activities = splitList(parts[3])
                        val features = splitList(parts[4])

                        parks.add(
                            Park(
                                parkID = parkID,
                                name = name,
                                location = location,
                                activities = activities,
                                features = features,
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

    fun getAll(): List<Park> = parks

    private fun splitList(field: String): List<String> =
        if (field.isBlank()) emptyList()
        else field.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}