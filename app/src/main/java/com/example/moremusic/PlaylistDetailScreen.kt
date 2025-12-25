// Create a new file: PlaylistDetailScreen.kt
package com.example.moremusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    nav: NavController,
    vm: MusicViewModel
) {
    val playlists by vm.playlists.collectAsState()
    val allSongs by vm.songs.collectAsState()

    val playlist = playlists.find { it.id == playlistId }

    val songsInPlaylist = playlist?.songIds?.mapNotNull { songId ->
        allSongs.find { it.id == songId }
    } ?: emptyList()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
            .padding(top=40.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable { nav.popBackStack() }
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    playlist?.name ?: "Playlist",
                    fontSize = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (songsInPlaylist.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("This playlist is empty.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(songsInPlaylist, key = { it.id }) { song ->
                        PlaylistSongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, songsInPlaylist)
                                nav.navigate("player")
                            },
                            onShowMenu = { vm.showMenuForSong(it, playlistId) },
                            isLiked = vm.likedSongs.contains(song)
                        )
                    }
                }
            }
        }
    }
}
