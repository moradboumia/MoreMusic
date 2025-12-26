package com.example.moremusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moremusic.model.Song
import com.example.moremusic.util.shareSong
import com.example.moremusic.MusicViewModel

@Composable
fun SongMenuSheet(
    song: Song,
    vm: MusicViewModel,
    playlistId: Long?
) {
    val context = LocalContext.current
    Column(
        Modifier
            .background(Color(0xFF2C2C2C))
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(50)
                )
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist ?: "Unknown", color = Color.LightGray, fontSize = 14.sp)
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Menu",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { vm.dismissMenu() }
            )
        }

        Spacer(Modifier.height(8.dp))
        Divider(color = Color.Gray.copy(alpha = 0.3f))

        val isLiked = vm.likedSongs.contains(song)
        MenuItem(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
            text = if (isLiked) "Unlike" else "Like",
            tint = if (isLiked) Color(0xFFE40074) else Color.White
        ) {
            vm.toggleLike(song)
            vm.dismissMenu()
        }
        MenuItem(icon = Icons.Default.Share, text = "Share") {
            shareSong(context, song)
            vm.dismissMenu() }
        MenuItem(icon = Icons.AutoMirrored.Filled.PlaylistAdd, text = "Add to playlist") { vm.showAddToPlaylistSheet(song) }
        if (playlistId != null) {
            MenuItem(
                icon = Icons.Default.PlaylistRemove,
                text = "Remove from playlist",
                isDestructive = true
            ) {
                vm.removeSongFromPlaylist(song, playlistId)
                vm.dismissMenu()
            }
        } else {
            MenuItem(
                icon = Icons.Default.Delete,
                text = "Delete from device",
                isDestructive = true
            ) {
                vm.dismissMenu()
                vm.deleteSong(context, song)
            }
        }
    }
}


@Composable
private fun MenuItem(
    icon: ImageVector,
    text: String,
    tint: Color = Color.White,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) Color(0xFF9F0000) else tint
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = contentColor)
        Spacer(Modifier.width(16.dp))
        Text(text, color = contentColor, fontSize = 16.sp)
    }
}