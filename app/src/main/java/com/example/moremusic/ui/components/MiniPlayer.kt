package com.example.moremusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.moremusic.MusicViewModel
import com.example.moremusic.model.Song
import com.example.moremusic.ui.theme.backgroundPrime

@Composable
fun MiniPlayer(song: Song, isPlaying: Boolean, vm: MusicViewModel, openPlayer: () -> Unit) {
    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = backgroundPrime,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { openPlayer() }
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            val artist = song.artist?.takeIf { it.isNotBlank() } ?: "Unknown"

            Column(Modifier.weight(1f)) {
                Text(
                    text = "${song.title} ($artist)",
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { vm.togglePlayPause() }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
            }
        }
    }
}