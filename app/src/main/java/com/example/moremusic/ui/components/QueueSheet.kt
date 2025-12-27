package com.example.moremusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moremusic.MusicViewModel
import com.example.moremusic.model.Song
import com.example.moremusic.ui.theme.backgroundPrime

@Composable
fun QueueSheet(vm: MusicViewModel) {
    val allSongs by vm.songs.collectAsState()
    val mediaItemsInQueue by vm.playQueue.collectAsState()
    val player by vm.playerState.collectAsState()
    val currentSongIndex by vm.currentSongIndex.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
            .background(backgroundPrime)
            .padding(top = 2.dp, bottom = 0.dp),
    ) {
        if (mediaItemsInQueue.isEmpty() || allSongs.isEmpty()) {
            Box( ) { Text("Queue is empty" ) }
        } else {
            LazyColumn {
                itemsIndexed(mediaItemsInQueue) { index, mediaItem ->
                    val song = allSongs.find { it.id.toString() == mediaItem.mediaId }

                    if (song != null) {
                        val isCurrent = index == currentSongIndex
                        QueueItem(song = song, isCurrent = isCurrent) {
                            player?.seekTo(index, 0)
                            if (player?.isPlaying == false) {
                                player?.play()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItem(song: Song, isCurrent: Boolean, onClick: () -> Unit) {
    val primaryColor = Color(0xFFE40074)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isCurrent) primaryColor.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.GraphicEq,
            contentDescription = null,
            tint = if (isCurrent) primaryColor else Color.Gray,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = primaryColor,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist ?: "Unknown Artist",
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}