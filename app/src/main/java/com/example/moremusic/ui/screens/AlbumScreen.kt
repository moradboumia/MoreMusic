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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.moremusic.R
import com.example.moremusic.model.Album
import com.example.moremusic.ui.components.BottomNavigationBar
import com.example.moremusic.ui.components.MiniPlayer
import com.example.moremusic.MusicViewModel

@Composable
fun AlbumScreen(nav: NavController, vm: MusicViewModel) {
    val albums by vm.albums.collectAsState()
    val allSongs by vm.songs.collectAsState()

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {        Spacer(Modifier.height(60.dp))

                Text("Albums", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(albums, key = { it.artworkUri.toString() }) { album ->
                    AlbumGridItem(
                        album = album,
                        onClick = {
                            val songsInAlbum = allSongs.filter { it.albumArt == album.artworkUri }
                            if (songsInAlbum.isNotEmpty()) {
                                vm.playSong(songsInAlbum.first(), songsInAlbum)
                                nav.navigate("player")
                            }
                        }
                    )
                }

                item { Spacer(Modifier.height(120.dp)) }
            }
        }

        AnimatedVisibility(
            visible = current != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp, start = 8.dp, end = 8.dp)
        ) {
            current?.let {
                MiniPlayer(
                    song = it,
                    isPlaying = isPlaying,
                    vm = vm
                ) { nav.navigate("player") }
            }
        }
        BottomNavigationBar(nav as NavHostController, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun AlbumGridItem(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = album.artworkUri,
            contentDescription = album.name,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.name,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            color = Color.Gray,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}