package com.example.moremusic


import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistManager(context: Context) {
    private val prefs = context.getSharedPreferences("playlist_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "playlists_key"

    fun savePlaylists(playlists: List<Playlist>) {
        val json = gson.toJson(playlists)
        prefs.edit().putString(key, json).apply()
    }

    fun loadPlaylists(): List<Playlist> {
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<Playlist>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
