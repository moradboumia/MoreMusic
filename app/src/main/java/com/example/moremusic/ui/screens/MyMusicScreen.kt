package com.example.moremusic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.moremusic.ui.components.BottomNavigationBar
import com.example.moremusic.ui.components.MiniPlayer
import com.example.moremusic.ui.components.SongRow
import com.example.moremusic.MusicViewModel

@Composable
fun MyMusicScreen(nav: NavHostController, vm: MusicViewModel, hasPermission: Boolean) {

    val songs by vm.songs.collectAsState()
    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = songs.filter { song ->
        song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist?.contains(searchQuery, ignoreCase = true) == true ||
                song.uri.toString().contains(searchQuery, ignoreCase = true)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
            .padding(top = 40.dp)
    ) {
        Column(Modifier.fillMaxSize()) {

            Text("My Music", color = Color(0xFFE40074), fontSize = 32.sp, modifier = Modifier.padding(start = 20.dp))

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFE40074))
                    Spacer(Modifier.width(12.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Songs, albums or artists",
                                color = Color.LightGray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            if (!hasPermission) {
                Text(
                    "Permission required to read local audio files.",
                    color = Color.LightGray,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(songs) { _, song ->
                        SongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, songs)
                                nav.navigate("player")
                            },
                            onDelete = { vm.deleteSong(context,it) },
                            onToggleLike = { vm.toggleLike(it) },
                            onShowMenu = { vm.showMenuForSong(it, null) }, 
                            isLiked = vm.likedSongs.contains(song)
                        )
                    }
                }
            }

            Spacer(Modifier.height(110.dp)) 
        }

        AnimatedVisibility(
            visible = current != null,
            enter = slideInVertically(tween(300)) { it } + fadeIn(),
            exit  = slideOutVertically(tween(200)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 78.dp)
        ) {
            current?.let {
                MiniPlayer(it, isPlaying, vm) { nav.navigate("player") }
            }
        }

        BottomNavigationBar(nav, Modifier.align(Alignment.BottomCenter))
    }
}