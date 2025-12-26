package com.example.moremusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Long = System.currentTimeMillis(), // Simple unique ID
    val name: String,
    val songIds: List<Long>
) : Parcelable
