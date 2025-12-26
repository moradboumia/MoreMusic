package com.example.moremusic.model

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val uri: Uri,
    val albumArt: Uri?
) : Parcelable
