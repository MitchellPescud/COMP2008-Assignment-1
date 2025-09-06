// FavouriteStore.kt
package com.example.comp2008assignment1

import android.content.Context
import androidx.core.content.edit

object FavouriteStore {
    private const val PREFS = "parks_prefs"
    private const val KEY_FAVS = "fav_ids"

    fun getFavourites(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_FAVS, emptySet()) ?: emptySet()

    fun isFavourite(context: Context, parkID: String): Boolean =
        getFavourites(context).contains(parkID)

    fun toggleFavourite(context: Context, parkID: String, makeFavourite: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_FAVS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (makeFavourite) current.add(parkID) else current.remove(parkID)
        // Write a *new* Set<String> instance (defensive copy) to avoid Android’s shared reference quirk
        prefs.edit {
            putStringSet(KEY_FAVS, current.toSet())
        }
    }
}
