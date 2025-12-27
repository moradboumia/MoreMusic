package com.example.moremusic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.moremusic.ui.components.BottomNavigationBar
import com.example.moremusic.ui.components.MiniPlayer
import com.example.moremusic.ui.components.SongRow
import com.example.moremusic.MusicViewModel
import com.example.moremusic.ui.theme.background

@Composable
fun HomeScreen(nav: NavHostController, vm: MusicViewModel, hasPermission: Boolean) {

    val songs by vm.songs.collectAsState()
    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredSongs = songs.filter { song ->
        song.title.contains(searchQuery, ignoreCase = true) ||
                (song.artist?.contains(searchQuery, ignoreCase = true) == true)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { vm.openDrawer() }
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {

                item {
                    Text(
                        "Hi There,",
                        color = Color(0xFFE40074),
                        fontSize = 32.sp,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
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
                                placeholder = { Text("Songs, albums or artists", color = Color.LightGray) },
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
                }

                item {
                    Text(
                        "All local songs",
                        color = Color(0xFFE40074),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                }

                if (!hasPermission) {
                    item {
                        Text(
                            "Permission required to read local audio files.",
                            color = Color.LightGray,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    items(filteredSongs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, filteredSongs)
                                nav.navigate("player")
                            },
                            onShowMenu = { vm.showMenuForSong(it, null) },
                            isLiked = vm.likedSongs.contains(song)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(110.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = current != null,
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