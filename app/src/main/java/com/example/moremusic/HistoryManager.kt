// Create a new file: HistoryManager.kt
package com.example.moremusic

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.toMutableList

class PlayHistoryEvent(
val songId: Long,
val timestamp: Long = System.currentTimeMillis()
)

class HistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "play_history_key"

    fun recordSongPlay(songId: Long) {
        val history = loadHistory().toMutableList()
        history.add(PlayHistoryEvent(songId = songId))
        // Optional: Trim the history to prevent it from getting too large
        if (history.size > 2000) {
            history.removeAt(0)
        }
        saveHistory(history)
    }

    fun loadHistory(): List<PlayHistoryEvent> {
        val json = prefs.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<PlayHistoryEvent>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun saveHistory(history: List<PlayHistoryEvent>) {
        val json = gson.toJson(history)
        prefs.edit().putString(key, json).apply()
    }
}
