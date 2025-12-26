package com.example.moremusic.model

import android.net.Uri

data class Album(
    val name: String,
    val artist: String,
    val artworkUri: Uri?
)
