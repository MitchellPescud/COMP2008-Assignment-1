package com.example.comp2008assignment1

import android.content.Context
import androidx.core.content.edit

// Singleton object responsible for persisting favourite park IDs.
// Uses SharedPreferences so data is saved even when the app is closed/reopened.
object FavouriteStore {
    private const val PREFS = "parks_prefs"   // Name of SharedPreferences file
    private const val KEY_FAVS = "fav_ids"    // Key for storing favourite park IDs

    /**
     * Retrieves the full set of favourited park IDs.
     * @return a Set<String> of parkIDs that are currently marked as favourite
     */
    fun getFavourites(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_FAVS, emptySet()) // return empty set if nothing stored yet
            ?: emptySet()

    /**
     * Checks if a single park is favourited.
     * @param parkID The unique ID of the park
     * @return true if this park is in the favourites set, false otherwise
     */
    fun isFavourite(context: Context, parkID: String): Boolean =
        getFavourites(context).contains(parkID)

    /**
     * Adds or removes a park from favourites.
     * @param parkID The unique ID of the park
     * @param makeFavourite true to add park to favourites, false to remove it
     */
    fun toggleFavourite(context: Context, parkID: String, makeFavourite: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // Get current favourites set (make mutable copy so we can update it)
        val current = prefs.getStringSet(KEY_FAVS, emptySet())?.toMutableSet() ?: mutableSetOf()

        // Add/remove the parkID depending on action
        if (makeFavourite) current.add(parkID) else current.remove(parkID)

        // Save updated set back into SharedPreferences
        // Important: write a *new* Set instance, because Android’s implementation
        // returns a live reference which must not be reused.
        prefs.edit {
            putStringSet(KEY_FAVS, current.toSet())
        }
    }
}
