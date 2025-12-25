package com.example.moremusic

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE)

    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriAdapter())
        .create()

    private val LIKED_SONGS_KEY = "liked_songs"

    fun saveLikedSongs(songs: List<Song>) {
        val json = gson.toJson(songs)
        prefs.edit().putString(LIKED_SONGS_KEY, json).apply()
    }

    fun loadLikedSongs(): List<Song> {
        val json = prefs.getString(LIKED_SONGS_KEY, null)
        if (json.isNullOrBlank()) {
            return emptyList()
        }
        val type = object : TypeToken<List<Song>>() {}.type
        return gson.fromJson(json, type)
    }
}

