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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.moremusic.ui.components.BottomNavigationBar
import com.example.moremusic.ui.components.MiniPlayer
import com.example.moremusic.MusicViewModel
import com.example.moremusic.ui.theme.background

@Composable
fun LibraryScreen(nav: NavHostController, vm: MusicViewModel) {

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(Modifier
            .fillMaxSize()
             ) {

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
                Text(
                    "Library",
                    fontSize = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(28.dp)) 
            }

            Spacer(Modifier.height(28.dp))


            LibraryItem(Icons.Default.QueueMusic, "Now Playing") {
                nav.navigate("player")
            }

            LibraryItem(Icons.Default.FavoriteBorder, "Favorites") {
                nav.navigate("favorates_screen")
            }

            LibraryItem(Icons.Default.Folder, "My Music") {
                nav.navigate("my_music")
            }

            LibraryItem(Icons.Default.PlaylistPlay, "Playlists") {
                nav.navigate("playlists")            }

            LibraryItem(Icons.Default.ShowChart, "Stats") {
                nav.navigate("charts")
            }

            Spacer(Modifier.height(120.dp)) 
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

        BottomNavigationBar(
            nav = nav,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
@Composable
fun LibraryItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(18.dp))
        Text(
            label,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}