package com.example.moremusic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.moremusic.model.Album
import com.example.moremusic.model.Song
import com.example.moremusic.ui.components.BottomNavigationBar
import com.example.moremusic.ui.components.MiniPlayer
import com.example.moremusic.MusicViewModel
import com.example.moremusic.ui.theme.background

@Composable
fun ChartsScreen(nav: NavHostController, vm: MusicViewModel) {
    val topSongs by vm.topSongs.collectAsState()
    val topArtists by vm.topArtists.collectAsState()
    val topAlbums by vm.topAlbums.collectAsState()

    LaunchedEffect(Unit) {
        vm.generateCharts()
    }

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
            LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 10.dp,top=10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Stats", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                ChartSection(title = "Top Songs") {
                    topSongs.forEachIndexed { index, topSong ->
                        ChartSongItem(
                            rank = index + 1,
                            song = topSong.song,
                            playCount = topSong.playCount,
                            onClick = { vm.playSong(topSong.song, listOf(topSong.song)) }
                        )
                    }
                }
            }

            item {
                ChartSection(title = "Top Artists") {
                    topArtists.forEachIndexed { index, topArtist ->
                        ChartArtistItem(
                            rank = index + 1,
                            artistName = topArtist.artistName,
                            playCount = topArtist.playCount
                        )
                    }
                }
            }

            item {
                ChartSection(title = "Top Albums") {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(topAlbums) { topAlbum ->
                            ChartAlbumItem(
                                album = topAlbum.album,
                                playCount = topAlbum.playCount,
                                onClick = {  }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = current != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp, start = 8.dp, end = 8.dp)
        ) {
            current?.let { MiniPlayer(song = it, isPlaying = isPlaying, vm = vm) { nav.navigate("player") } }
        }
        BottomNavigationBar(nav = nav, modifier = Modifier.align(Alignment.BottomCenter))
    }

}

@Composable
fun ChartSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun ChartSongItem(rank: Int, song: Song, playCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp)
        )
        AsyncImage(
            model = song.albumArt,
            contentDescription = song.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist ?: "Unknown", color = Color.Gray, fontSize = 13.sp)
        }
        Text("$playCount plays", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ChartArtistItem(rank: Int, artistName: String, playCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp)
        )
        Icon(
            Icons.Default.Person,
            contentDescription = artistName,
            tint = Color.Gray,
            modifier = Modifier
                .size(48.dp)
                .background(Color.DarkGray, RoundedCornerShape(50))
                .padding(8.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = artistName,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text("$playCount plays", color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun ChartAlbumItem(album: Album, playCount: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = album.artworkUri,
            contentDescription = album.name,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Text(album.name, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("$playCount plays", color = Color.Gray, fontSize = 12.sp)
    }
}
