package com.example.moremusic

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)

    // 🔴 CHANGE THIS PART:
    // We now build a custom Gson instance that knows how to handle Uris.
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriAdapter()) // Teach Gson about Uri
        .create()
    // END OF CHANGE

    private val LIKED_SONGS_KEY = "liked_songs"

    // --- Save Liked Songs ---
    fun saveLikedSongs(songs: List<Song>) {
        val json = gson.toJson(songs) // This will now work correctly
        prefs.edit().putString(LIKED_SONGS_KEY, json).apply()
    }

    // --- Load Liked Songs ---
    fun loadLikedSongs(): List<Song> {
        val json = prefs.getString(LIKED_SONGS_KEY, null)
        if (json.isNullOrBlank()) {
            return emptyList()
        }
        val type = object : TypeToken<List<Song>>() {}.type
        return gson.fromJson(json, type) // This will also work correctly now
    }
}

