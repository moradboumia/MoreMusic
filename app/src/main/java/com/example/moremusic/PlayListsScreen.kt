package com.example.moremusic

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.moremusic.model.Playlist

@Composable
fun PlaylistsScreen(nav: NavController, vm: MusicViewModel) {
    val playlists by vm.playlists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
            .padding(top = 40.dp)
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
                Text("Playlists", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            PlaylistItem(
                icon = Icons.Default.Add,
                label = "Create new playlist",
                color = Color(0xFFE40074)
            ) {
                showCreateDialog = true
            }

            Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 20.dp))

            LazyColumn {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistItem(
                        icon = Icons.Default.PlaylistPlay,
                        label = playlist.name,
                        onClick = {
                            nav.navigate("playlist_detail/${playlist.id}")
                        },
                        trailingIcon = {
                            IconButton(onClick = { playlistToDelete = playlist }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete playlist", tint = Color.Gray)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { playlistName ->
                vm.createPlaylist(playlistName)
                showCreateDialog = false
            }
        )
    }
    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete the playlist \"${playlist.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deletePlaylist(playlist)
                        playlistToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFE40074))
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF2C2C2C),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun PlaylistItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.White,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(18.dp))
        Text(label, color = color, fontSize = 18.sp, modifier = Modifier.weight(1f)) // Give text weight to push icon to the end

        if (trailingIcon != null) {
            Spacer(Modifier.width(16.dp))
            trailingIcon()
        }
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2C2C2C),
            tonalElevation = 8.dp
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("New Playlist", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE40074),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFE40074),
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = { onCreate(text) },
                        enabled = text.isNotBlank()
                    ) {
                        Text("Create", color = if (text.isNotBlank()) Color(0xFFE40074) else Color.Gray)
                    }
                }
            }
        }
    }
}
