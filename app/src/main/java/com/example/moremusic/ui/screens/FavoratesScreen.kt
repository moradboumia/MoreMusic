package com.example.moremusic.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.moremusic.model.Song
import com.example.moremusic.MusicViewModel

@Composable
fun FavoratesScreen(nav: NavHostController, vm: MusicViewModel) {
    val likedSongs = vm.likedSongs

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    val DarkPink = Color(0xFFE40074)

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1F1F1F), Color.Black)))
            .padding(top = 40.dp)
    ) {
        Spacer(Modifier.height(36.dp))
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { nav.popBackStack() }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(DarkPink, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Liked Songs Artwork",
                        tint = Color.White,
                        modifier = Modifier.size(70.dp)
                    )
                    Text(
                        text = "${likedSongs.size} songs",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                }

                Spacer(Modifier.width(20.dp))

                Text(
                    text = "Liked songs",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (likedSongs.isNotEmpty()) {
                            vm.playSong(likedSongs.random(), likedSongs.shuffled())
                            nav.navigate("player")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E3E)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shuffle, "Shuffle", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle", color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        if (likedSongs.isNotEmpty()) {
                            vm.playSong(likedSongs.first(), likedSongs)
                            nav.navigate("player")
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E3E)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Play", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (likedSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "You haven't liked any songs yet.",
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(likedSongs, key = { it.uri }) { song ->
                        FavoriteSongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, likedSongs)
                                nav.navigate("player")
                            },
                            onToggleLike = { vm.toggleLike(it) },
                            onShowMenu = { vm.showMenuForSong(it, null) }
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun FavoriteSongRow(
    song: Song,
    onClick: () -> Unit,
    onToggleLike: (Song) -> Unit,
    onShowMenu: (Song) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.GraphicEq, null, tint = Color(0xFFE40074), modifier = Modifier.size(28.dp))

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFFE40074))
            Text(song.artist ?: "Unknown", maxLines = 1, color = Color.Gray)
        }

        Spacer(Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Unlike",
            tint = Color(0xFFE40074),
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleLike(song) }
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More Options",
            tint = Color.Gray,
            modifier = Modifier
                .size(24.dp)
                .clickable { onShowMenu(song) }
        )
    }
}