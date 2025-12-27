package com.example.moremusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moremusic.ui.theme.backgroundPrime
import com.example.moremusic.util.openUrl
import com.example.moremusic.MusicViewModel

@Composable
fun AppDrawerContent(nav: NavController, vm: MusicViewModel) {
    val context = LocalContext.current
    val darkPink = Color(0xFFE40074)

    Column(
        Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(backgroundPrime.copy(alpha = 0.98f))
            .padding(start = 24.dp, top = 68.dp, end = 24.dp)
    ) {
        Text(
            text = "MoreMusic",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(48.dp))

        DrawerMenuItem(icon = Icons.Default.Home, text = "Home") {
            nav.navigate("home")
            vm.closeDrawer()
        }
        DrawerMenuItem(icon = Icons.Default.QueryStats, text = "charts") {
            nav.navigate("charts")
            vm.closeDrawer()
        }
        DrawerMenuItem(icon = Icons.Default.Album, text = "albums") {
            nav.navigate("albums")
            vm.closeDrawer()
        }
        DrawerMenuItem(icon = Icons.Default.FavoriteBorder, text = "favorates") {
            nav.navigate("favorates_screen")
            vm.closeDrawer()
        }

        DrawerMenuItem(icon = Icons.Default.MusicNote, text = "My Music") {
            nav.navigate("my_music")
            vm.closeDrawer()
        }
        Divider(
            color = Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        DrawerMenuItem(icon = Icons.Default.QueueMusic, text = "now playing") {
            nav.navigate("player")
            vm.closeDrawer()
        }
        DrawerMenuItem(icon = Icons.Default.PlaylistPlay, text = "Playlists") {
            nav.navigate("playlists")
            vm.closeDrawer()
        }
        DrawerMenuItem(icon = Icons.Default.Star, text = "Help us by rating") {
            val githubUrl = "https://github.com/moradboumia/MoreMusic"
            openUrl(context, githubUrl)
            vm.closeDrawer()
        }
    }
}

@Composable
private fun DrawerMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = Color.LightGray)
        Spacer(Modifier.width(16.dp))
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}