package com.example.moremusic

import com.example.moremusic.ui.theme.theme.MoreMusicTheme
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlin.jvm.java

@Parcelize
data class Playlist(
    val id: Long = System.currentTimeMillis(), // Simple unique ID
    val name: String,
    val songIds: List<Long>
) : Parcelable

data class Album(
    val name: String,
    val artist: String,
    val artworkUri: Uri?
)
data class TopSong(val song: Song, val playCount: Int)
data class TopArtist(val artistName: String, val playCount: Int)
data class TopAlbum(val album: Album, val playCount: Int)
@SuppressLint("ParcelCreator")
@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val uri: Uri,
    val albumArt: Uri?
) :Parcelable

// ===========================================================================
// VIEWMODEL
// ===========================================================================
class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()
    private val _current = MutableStateFlow<Song?>(null)
    val current = _current.asStateFlow()
    private val _playerState = MutableStateFlow<Player?>(null)

    val playerState: StateFlow<Player?> = _playerState
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _playQueue = MutableStateFlow<List<MediaItem>>(emptyList())
    val playQueue: StateFlow<List<MediaItem>> = _playQueue
    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()

    // ... (your existing state variables)
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex = _currentSongIndex.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private val favoritesManager = FavoritesManager(application)
    private val playlistManager = PlaylistManager(application)
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()
    private var pollingStarted = false
    // In MusicViewModel class

    private val _isRepeating = MutableStateFlow(false)
    val isRepeating = _isRepeating.asStateFlow()
    private val _isShuffling = MutableStateFlow(false)
    val isShuffling = _isShuffling.asStateFlow()
    // In MusicViewModel class

    // State for the main song menu
    private val _songForMenu = MutableStateFlow<Pair<Song, Long?>?>(null)
    val songForMenu = _songForMenu.asStateFlow()

    // A. NEW STATE TO CONTROL THE PLAY QUEUE/PLAYLIST SHEET
    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible = _isQueueSheetVisible.asStateFlow()
    var player by mutableStateOf<Player?>(null)
        private set


    private val _songToAdd = MutableStateFlow<Song?>(null)
    val songToAdd = _songToAdd.asStateFlow()
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums = _albums.asStateFlow()
    private val historyManager = HistoryManager(application)
    private val _topSongs = MutableStateFlow<List<TopSong>>(emptyList())
    val topSongs = _topSongs.asStateFlow()

    private val _topArtists = MutableStateFlow<List<TopArtist>>(emptyList())
    val topArtists = _topArtists.asStateFlow()

    private val _topAlbums = MutableStateFlow<List<TopAlbum>>(emptyList())
    val topAlbums = _topAlbums.asStateFlow()

    private val _drawerShouldBeOpen = MutableStateFlow(false)
    val drawerShouldBeOpen = _drawerShouldBeOpen.asStateFlow()

    private val _pendingDeleteRequest = MutableStateFlow<Pair<IntentSender, Song>?>(null)
    val pendingDeleteRequest = _pendingDeleteRequest.asStateFlow()

    fun openDrawer() {
        _drawerShouldBeOpen.value = true
    }

    fun closeDrawer() {
        _drawerShouldBeOpen.value = false
    }

// ...

    fun clearToastMessage() {
        _toastMessage.value = null
    }
    fun showAddToPlaylistSheet(song: Song) {
        _songToAdd.value = song
        _songForMenu.value = null // Hide the first menu
    }

    fun dismissAddToPlaylistSheet() {
        _songToAdd.value = null
    }
//...

    fun showQueueSheet() {
        _isQueueSheetVisible.value = true
    }

    fun dismissQueueSheet() {
        _isQueueSheetVisible.value = false
    }
    fun toggleShuffleMode() {
        _isShuffling.value = !_isShuffling.value
        // Apply the new shuffle mode to the player
        player?.shuffleModeEnabled = _isShuffling.value
    }

    fun toggleRepeatMode() {
        _isRepeating.value = !_isRepeating.value
        // Apply the new repeat mode to the player
        player?.repeatMode = if (_isRepeating.value) {
            Player.REPEAT_MODE_ONE // Loop the current song
        } else {
            Player.REPEAT_MODE_OFF // Don't loop
        }
    }
    fun hideQueueSheet() {
        _isQueueSheetVisible.value = false
    }

    fun showMenuForSong(song: Song, playlistId: Long? = null) {
        _songForMenu.value = Pair(song, playlistId)
    }

    fun dismissMenu() {
        _songForMenu.value = null
    }


    var likedSongs = mutableStateListOf<Song>()
        private set
    init {
        // Load liked songs from storage when the ViewModel is created
        likedSongs.addAll(favoritesManager.loadLikedSongs())
        _playlists.value = playlistManager.loadPlaylists()
    }

    // -------------------- Init Player ------------------------
    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(name = name, songIds = emptyList())
        val updatedPlaylists = _playlists.value + newPlaylist
        _playlists.value = updatedPlaylists
        playlistManager.savePlaylists(updatedPlaylists)
    }

    fun addSongToPlaylist(song: Song, playlist: Playlist) {
        // Find the target playlist in the current list of playlists
        val targetPlaylist = _playlists.value.find { it.id == playlist.id } ?: return

        // 1. Check if the song ID is already in the playlist's songIds list
        if (targetPlaylist.songIds.contains(song.id)) {
            // 2. If it exists, post a message to the toast StateFlow
            _toastMessage.value = "Song is already in \"${playlist.name}\""
        } else {
            // 3. If it doesn't exist, proceed with adding the song
            val updatedPlaylists = _playlists.value.map {
                if (it.id == playlist.id) {
                    it.copy(songIds = it.songIds + song.id)
                } else {
                    it
                }
            }
            _playlists.value = updatedPlaylists
            playlistManager.savePlaylists(updatedPlaylists)
            // Show a success message
            _toastMessage.value = "Added to \"${playlist.name}\""
        }
    }



    fun deletePlaylist(playlist: Playlist) {
        // Create a new list excluding the playlist to be deleted
        val updatedPlaylists = _playlists.value.filter { it.id != playlist.id }
        _playlists.value = updatedPlaylists
        // Save the updated list to device storage
        playlistManager.savePlaylists(updatedPlaylists)
    }


    fun initPlayer(p: Player) {
        player = p
        _playerState.value = p

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                _isPlaying.value = isPlayingNow
                if (isPlayingNow && !pollingStarted) startPositionPolling()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _duration.value = player?.duration ?: 0L
                val uri = mediaItem?.localConfiguration?.uri
                val newSong = _songs.value.find { it.uri == uri }
                _current.value = newSong
                _currentSongIndex.value = player?.currentMediaItemIndex ?: 0
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val items = mutableListOf<MediaItem>()
                if (timeline.windowCount > 0) {
                    for (i in 0 until timeline.windowCount) {
                        player?.getMediaItemAt(i)?.let { items.add(it) }
                    }
                }
                _playQueue.value = items
            }
        }

        player?.addListener(listener)

        // ⭐️⭐️⭐️ START: THIS IS THE FIX ⭐️⭐️⭐️
        // Manually read the player's current state upon initialization.
        // This ensures the queue is populated even if we are reconnecting
        // to a session where the timeline hasn't changed.
        player?.let {
            val items = mutableListOf<MediaItem>()
            if (it.mediaItemCount > 0) {
                for (i in 0 until it.mediaItemCount) {
                    items.add(it.getMediaItemAt(i))
                }
            }
            _playQueue.value = items // Directly update the state flow
        }
    }

    fun removeSongFromPlaylist(song: Song, playlistId: Long) {
        val updatedPlaylists = _playlists.value.map {
            // Find the correct playlist
            if (it.id == playlistId) {
                // Create a new list of song IDs excluding the one to be removed
                val updatedSongIds = it.songIds.filter { id -> id != song.id }
                it.copy(songIds = updatedSongIds)
            } else {
                it
            }
        }
        _playlists.value = updatedPlaylists
        playlistManager.savePlaylists(updatedPlaylists)
        // Optionally, show a toast message
        _toastMessage.value = "Removed \"${song.title}\" from playlist"
    }


    // -------------------- Position Poll ------------------------
    private fun startPositionPolling() {
        pollingStarted = true

        viewModelScope.launch {
            while (true) {
                // This code remains the same and works correctly with the Player interface
                val p = player
                if (p != null) {
                    _position.value = p.currentPosition
                    _duration.value = p.duration.coerceAtLeast(0L)
                    _isPlaying.value = p.isPlaying
                }
                delay(300)
            }
        }
    }


    // -------------------- Music Loader ------------------------
    fun loadLocalMusic(context: Context) {
        viewModelScope.launch {
            val list = mutableListOf<Song>()
            val allowedExt = listOf(".mp3", ".m4a", ".aac", ".wav", ".flac", ".ogg")

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (c.moveToNext()) {

                    val displayName = c.getString(nameCol) ?: ""
                    val mime = c.getString(mimeCol) ?: ""
                    val isAllowedExt = allowedExt.any { displayName.lowercase().endsWith(it) }

                    if (mime.startsWith("audio/") || isAllowedExt) {
                        val id = c.getLong(idCol)
                        val title = c.getString(titleCol) ?: displayName
                        val artist = c.getString(artistCol) ?: "Unknown"

                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        val albumId = c.getLong(albumIdColumn)

                        val artworkUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )

                        list.add(Song(id, title, artist, uri, artworkUri))
                    }
                }
            }
            val albumList = list
                .filter { it.albumArt != null } // Only consider songs with album art
                .groupBy { it.albumArt }
                .map { (artworkUri, songsInAlbum) ->
                    val firstSong = songsInAlbum.first()
                    // Attempt to find a proper album name from metadata, fallback to folder name or "Unknown"
                    val albumName = firstSong.artist ?: "Unknown Album" // Often, album metadata is in the artist field
                    Album(
                        name = albumName,
                        artist = firstSong.artist ?: "Unknown Artist",
                        artworkUri = artworkUri
                    )
                }
            _albums.value = albumList
            _songs.value = list
        }
    }


    // -------------------- Control: Play Song ------------------------
    // In MusicViewModel.kt

    // -------------------- Control: Play Song ------------------------
    // In MainActivity.kt -> class MusicViewModel

    fun playSong(clicked: Song, queue: List<Song>) {
        historyManager.recordSongPlay(clicked.id)
        viewModelScope.launch {

            player?.apply {
                clearMediaItems()

                val mediaItems = queue.map { song ->
                    MediaItem.Builder()
                        .setUri(song.uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                // It's also good practice to include the artwork URI here
                                .setArtworkUri(song.albumArt)
                                .build()
                        )
                        // ⭐️⭐️⭐️ THIS IS THE FIX ⭐️⭐️⭐️
                        // Set the mediaId using the song's unique ID.
                        .setMediaId(song.id.toString())
                        .build()
                }

                addMediaItems(mediaItems)

                val idx = queue.indexOfFirst { it.uri == clicked.uri }.coerceAtLeast(0)
                seekTo(idx, 0)
                prepare()
                play()
            }

            _current.value = clicked
            _isPlaying.value = true
        }
    }
    fun generateCharts() {
        viewModelScope.launch {
            val history = historyManager.loadHistory()
            val allSongs = _songs.value
            val allAlbums = _albums.value

            if (history.isEmpty() || allSongs.isEmpty()) return@launch

            // --- Calculate Top Songs ---
            _topSongs.value = history
                .groupingBy { it.songId }
                .eachCount()
                .mapNotNull { (songId, count) ->
                    val song = allSongs.find { it.id == songId }
                    if (song != null) TopSong(song, count) else null
                }
                .sortedByDescending { it.playCount }
                .take(20) // Take the top 20
            _topArtists.value = history
                .mapNotNull { event -> allSongs.find { it.id == event.songId }?.artist }
                .filterNotNull()
                .groupingBy { it }
                .eachCount()
                .map { (artistName, count) -> TopArtist(artistName, count) }
                .sortedByDescending { it.playCount }
                .take(20)

            // --- Calculate Top Albums ---
            _topAlbums.value = history
                .mapNotNull { event -> allSongs.find { it.id == event.songId }?.albumArt }
                .filterNotNull()
                .groupingBy { it }
                .eachCount()
                .mapNotNull { (artworkUri, count) ->
                    val album = allAlbums.find { it.artworkUri == artworkUri }
                    if (album != null) TopAlbum(album, count) else null
                }
                .sortedByDescending { it.playCount }
                .take(20)
        }
    }

    // -------------------- Controls ------------------------
    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) p.pause() else p.play()
        _isPlaying.value = p.isPlaying
    }

    fun toggleLike(song: Song) {
        if (likedSongs.contains(song)) {
            likedSongs.remove(song)
        } else {
            likedSongs.add(song)
        }
        favoritesManager.saveLikedSongs(likedSongs)
    }

    private fun postDeletionCleanup(song: Song) {
        val updatedSongs = _songs.value.filter { it.id != song.id }
        _songs.value = updatedSongs

        if (_current.value?.id == song.id) {
            player?.stop()
            _current.value = null
        }

        val updatedPlaylists = _playlists.value.map { playlist ->
            playlist.copy(songIds = playlist.songIds.filter { it != song.id })
        }
        _playlists.value = updatedPlaylists
        playlistManager.savePlaylists(updatedPlaylists)

        if (likedSongs.contains(song)) {
            likedSongs.remove(song)
            favoritesManager.saveLikedSongs(likedSongs)
        }
    }

    fun deleteSongRequestCancelled() {
        _toastMessage.value = "Deletion was cancelled."
        _pendingDeleteRequest.value = null
    }

    fun finalizeDelete(song: Song) {
        postDeletionCleanup(song)
        _toastMessage.value = "Song deleted."
        _pendingDeleteRequest.value = null
    }

    // In MusicViewModel.kt

    fun deleteSong(context: Context, song: Song) {
        viewModelScope.launch {
            try {
                // Use the ContentResolver to delete the file via its URI
                context.contentResolver.delete(song.uri, null, null)

                // If successful, remove the song from the UI and show a confirmation
                _songs.value = _songs.value.filter { it.id != song.id }
                _toastMessage.value = "Deleted \"${song.title}\""

            } catch (e: SecurityException) {
                // This is the crucial part for Android 10+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableException = e as? RecoverableSecurityException
                    if (recoverableException != null) {
                        // Store the intent sender so the UI can launch it
                        _pendingDeleteRequest.value = Pair(recoverableException.userAction.actionIntent.intentSender, song)
                    } else {
                        // Generic error if the exception is not recoverable
                        _toastMessage.value = "Could not delete file: Permission denied."
                    }
                } else {
                    // For older Android versions, if you get a SecurityException, you likely don't have permission
                    _toastMessage.value = "Could not delete file: Permission denied."
                }
            }
        }
    }

    // Add a function to clear the pending request after it's handled
    fun clearPendingDeleteRequest() {
        _pendingDeleteRequest.value = null
    }



    fun next() = player?.seekToNext()
    fun previous() = player?.seekToPrevious()
    fun seekTo(ms: Long) = player?.seekTo(ms)


    // -------------------- Cleanup ------------------------
    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }
}

class MusicViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// ===========================================================================
// REUSABLE SONG MENU BOTTOM SHEET
// ===========================================================================
@Composable
fun SongMenuSheet(
    song: Song,
    vm: MusicViewModel,
    playlistId: Long?
) {
    val context = LocalContext.current
    Column(
        Modifier
            .background(Color(0xFF2C2C2C)) // Dark background for the sheet
            .padding(top = 8.dp, bottom = 8.dp),        // Center the drag handle
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ⭐️⭐️⭐️ START: THIS IS THE FIX ⭐️⭐️⭐️
        // Add a drag handle at the top of the sheet
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    color = Color.Gray.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(50) // Makes it a fully rounded pill shape
                )
        )
        Spacer(Modifier.height(12.dp))
        // ⭐️⭐️⭐️ END: THIS IS THE FIX ⭐️⭐️⭐️


        // --- Header: Song Info and Close Button ---
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

        // --- Menu Items ---
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

@Composable
fun AppDrawerContent(nav: NavController, vm: MusicViewModel) {
    val context = LocalContext.current
    val darkPink = Color(0xFFE40074)

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF191919).copy(alpha = 0.98f)) // A slightly off-black background
            .padding(start = 24.dp, top = 68.dp, end = 24.dp)
    ) {
        // App Name Header
        Text(
            text = "MoreMusic",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(48.dp))

        // Navigation Items
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

// Helper composable for a single menu item row
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

// ===========================================================================
// ALBUM SCREEN
// ===========================================================================
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
            // --- Top Bar ---
            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {        Spacer(Modifier.height(60.dp))

                Text("Albums", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            // --- Grid of Albums ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f) // Allow grid to take up available space
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

                // Add spacer at the end for padding above mini-player
                item { Spacer(Modifier.height(120.dp)) }
            }
        }

        // ⭐️ --- Mini Player ---
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
        // Album Artwork
        AsyncImage(
            model = album.artworkUri,
            contentDescription = album.name,
            modifier = Modifier
                .aspectRatio(1f) // Make it a square
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background) // Placeholder
        )
        Spacer(Modifier.height(8.dp))
        // Album Name & Artist
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
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(application) as T
        }
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
// ===========================================================================
// MAIN ACTIVITY & UI
// ===========================================================================
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MoreMusicTheme { MusicApp() }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun MusicApp() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val nav = rememberNavController()
    val vm: MusicViewModel = viewModel(
        factory = MusicViewModelFactory(application)
    )
    val isDrawerOpen by vm.drawerShouldBeOpen.collectAsState()
    val drawerState = rememberDrawerState(initialValue = if (isDrawerOpen) DrawerValue.Open else DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isDrawerOpen) {
        if (isDrawerOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) {
            vm.closeDrawer()
        }
    }

    val pendingDeleteRequest by vm.pendingDeleteRequest.collectAsState()
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingDeleteRequest?.second?.let { songToDelete ->
                vm.finalizeDelete(songToDelete)
            }
        } else {
            vm.deleteSongRequestCancelled()
        }
    }

    LaunchedEffect(pendingDeleteRequest) {
        pendingDeleteRequest?.let { (intentSender, _) ->
            val request = IntentSenderRequest.Builder(intentSender).build()
            deleteLauncher.launch(request)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.Transparent // Make container transparent to see our custom background
            ) {
                AppDrawerContent(nav = nav, vm = vm)
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        var hasPermission by remember { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasPermission = granted
            if (granted) vm.loadLocalMusic(context)
        }
        var mediaController by remember { mutableStateOf<Player?>(null) }
        val sessionToken = remember { SessionToken(context, ComponentName(context, PlaybackService::class.java)) }
        val songToAdd by vm.songToAdd.collectAsState()
        val addToPlaylistSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
        val toastMessage by vm.toastMessage.collectAsState()
        LaunchedEffect(toastMessage) {
            toastMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                vm.clearToastMessage() // Clear the message so it doesn't show again on recomposition
            }
        }
        LaunchedEffect(songToAdd) {
            if (songToAdd != null) addToPlaylistSheetState.show() else addToPlaylistSheetState.hide()
        }
        LaunchedEffect(addToPlaylistSheetState.isVisible) {
            if (!addToPlaylistSheetState.isVisible) {
                vm.dismissAddToPlaylistSheet()
            }
        }
        DisposableEffect(sessionToken) {
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture.addListener({
                mediaController = controllerFuture.get()
            }, ContextCompat.getMainExecutor(context))
            onDispose { mediaController?.release() }
        }
        LaunchedEffect(mediaController) {
            mediaController?.let { vm.initPlayer(it) }
        }
        val isPlaying by vm.isPlaying.collectAsState()
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                val intent = Intent(context, PlaybackService::class.java)
                context.startService(intent)
            }
        }
        LaunchedEffect(Unit) {
            val granted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                hasPermission = true
                vm.loadLocalMusic(context)
            } else launcher.launch(permission)
        }
        // --- End of Setup ---


        // ------------------ 🟢 START: CORRECTED BOTTOM SHEET LOGIC 🟢 ------------------

        // 1. State for the Song Options Menu
        val menuState by vm.songForMenu.collectAsState() // This is now a Pair or null
        val songForMenu = menuState?.first
        val playlistIdForMenu = menuState?.second
        val menuSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )
        // In MainActivity.kt, inside your main Composable function (e.g., inside setContent)

// ... existing code
        val context = LocalContext.current
        val viewModel: MusicViewModel = viewModel() // Get your ViewModel instance
        val pendingDeleteRequest by viewModel.pendingDeleteRequest.collectAsState()

// 1. Create the ActivityResultLauncher
        val deleteRequestLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            // This callback runs after the user responds to the permission dialog
            if (result.resultCode == Activity.RESULT_OK) {
                // The user granted permission!
                // We need to re-attempt the deletion.
                pendingDeleteRequest?.second?.let { songToDelete ->
                    Toast.makeText(context, "Permission granted. Deleting file...", Toast.LENGTH_SHORT).show()
                    viewModel.deleteSong(context, songToDelete)
                }
            } else {
                // The user denied the permission
                Toast.makeText(context, "Permission to delete was denied.", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearPendingDeleteRequest()
        }

        LaunchedEffect(pendingDeleteRequest) {
            pendingDeleteRequest?.let { (intentSender, _) ->
                val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                deleteRequestLauncher.launch(intentSenderRequest)
            }
        }

// ... rest of your UI code

        LaunchedEffect(menuState) {
            if (menuState != null) {
                menuSheetState.show()
            } else {
                if (menuSheetState.isVisible) {
                    menuSheetState.hide()
                }
            }
        }

        LaunchedEffect(menuSheetState.isVisible) {
            if (!menuSheetState.isVisible) {
                vm.dismissMenu() // Resets the ViewModel's state to null
            }
        }


        // 2. State for the "Up Next" Queue Menu
        val isQueueSheetVisible by vm.isQueueSheetVisible.collectAsState()
        val queueSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )

        // This effect shows/hides the sheet based on ViewModel state
        LaunchedEffect(isQueueSheetVisible) {
            if (isQueueSheetVisible) {
                queueSheetState.show()
            } else {
                if (queueSheetState.isVisible) {
                    queueSheetState.hide()
                }
            }
        }

        // ⭐️ CORRECTION 2: Sync ViewModel for the queue sheet as well.
        LaunchedEffect(queueSheetState.isVisible) {
            if (!queueSheetState.isVisible) {
                vm.hideQueueSheet() // Resets the ViewModel's state to false
            }
        }

        ModalBottomSheetLayout(
            sheetState = addToPlaylistSheetState,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            scrimColor = Color.Black.copy(alpha = 0.5f),
            sheetContent = {
                val playlists by vm.playlists.collectAsState()
                AddToPlaylistSheet(
                    playlists = playlists,
                    onPlaylistClick = { playlist ->
                        songToAdd?.let { song ->
                            vm.addSongToPlaylist(song, playlist)
                        }
                        vm.dismissAddToPlaylistSheet()
                    }
                )
            }
        ) {
            ModalBottomSheetLayout(
                sheetState = queueSheetState,
                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                scrimColor = Color.Black.copy(alpha = 0.5f),
                sheetContent = {
                    if (isQueueSheetVisible) {
                        QueueSheet(vm = vm)
                    } else {
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            ) {
                ModalBottomSheetLayout(
                    sheetState = menuSheetState,
                    sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    sheetContent = {
                        songForMenu?.let { song ->
                            SongMenuSheet(song = song, vm = vm, playlistId = playlistIdForMenu)
                        } ?: Spacer(modifier = Modifier.height(1.dp)) // Spacer for when hidden
                    }
                ) {
                    // Your main app content (NavHost)
                    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFE40074))) {
                        NavHost(navController = nav, startDestination = "home") {
                            composable("home") { HomeScreen(nav, vm, hasPermission) }
                            composable("my_music") { MyMusicScreen(nav, vm, hasPermission) }
                            composable("player") { PlayerScreen(nav, vm) }
                            composable("charts") { ChartsScreen(nav, vm) }
                            composable("albums") { AlbumScreen(nav, vm) }
                            composable("library") { LibraryScreen(nav, vm) }
                            composable("playlists") { PlaylistsScreen(nav, vm) }
                            composable("favorates_screen") { FavoratesScreen(nav, vm) }
                            composable("playlist_detail/{playlistId}") { backStackEntry ->
                                val playlistId =
                                    backStackEntry.arguments?.getString("playlistId")?.toLongOrNull()
                                if (playlistId != null) {
                                    PlaylistDetailScreen(playlistId = playlistId, nav = nav, vm = vm)
                                }
                            }
                        }
                    }
                }
            }
        }
        // ------------------ 🟢 END: CORRECTED BOTTOM SHEET LOGIC 🟢 ------------------
    }
}

// In MainActivity.kt

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun QueueSheet(vm: MusicViewModel) {
    // Observe all necessary states from the ViewModel
    val allSongs by vm.songs.collectAsState()
    val mediaItemsInQueue by vm.playQueue.collectAsState()
    val player by vm.playerState.collectAsState()
    val currentSongIndex by vm.currentSongIndex.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
            .background(Color(0xFF1C1C1C))
            .padding(top = 2.dp, bottom = 0.dp),
    ) {
        // ...
        if (mediaItemsInQueue.isEmpty() || allSongs.isEmpty()) {
            Box(/*...*/) { Text("Queue is empty" /*...*/) }
        } else {
            LazyColumn {
                itemsIndexed(mediaItemsInQueue) { index, mediaItem ->
                    // This logic is correct: Find the Song using the mediaId
                    val song = allSongs.find { it.id.toString() == mediaItem.mediaId }

                    if (song != null) {
                        val isCurrent = index == currentSongIndex
                        QueueItem(song = song, isCurrent = isCurrent) {
                            player?.seekTo(index, 0)
                            if (player?.isPlaying == false) {
                                player?.play()
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun QueueItem(song: Song, isCurrent: Boolean, onClick: () -> Unit) {
    val primaryColor = Color(0xFFE40074)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            // ⭐️ 1. Add a subtle background highlight for the current song
            .background(if (isCurrent) primaryColor.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⭐️ 2. Use the GraphicEq icon, just like in FavoriteSongRow
        Icon(
            Icons.Default.GraphicEq,
            contentDescription = null,
            // Make the icon pink if it's the current song
            tint = if (isCurrent) primaryColor else Color.Gray,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.width(16.dp))

        // Title and Artist column
        Column(Modifier.weight(1f)) {
            Text(
                text = song.title,
                // ⭐️ 3. The title is now always pink for consistency
                color = primaryColor,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist ?: "Unknown Artist",
                // ⭐️ 4. The artist is always gray
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// In MainActivity.kt

// ===========================================================================
// HOME SCREEN (and other screens...)
// ===========================================================================
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
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
            .padding(top = 40.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            // --- Top Bar (Stays static at the top) ---
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

            // ⭐️⭐️⭐️ THIS IS THE FIX ⭐️⭐️⭐️
            // The LazyColumn now contains EVERYTHING that needs to scroll.
            LazyColumn(modifier = Modifier.weight(1f)) {

                // --- Header Item 1: "Hi There" ---
                item {
                    Text(
                        "Hi There,",
                        color = Color(0xFFE40074),
                        fontSize = 32.sp,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // --- Header Item 2: Search Bar ---
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

                // --- Header Item 3: "All local songs" title ---
                item {
                    Text(
                        "All local songs",
                        color = Color(0xFFE40074),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                }

                // --- SONGS LIST ---
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

                // Add space at the end of the list for the mini player and bottom nav
                item {
                    Spacer(Modifier.height(110.dp))
                }
            }
        }

        // --- Mini Player (Stays static at the bottom) ---
        AnimatedVisibility(
            visible = current != null,
            // ... (rest of mini player is correct)
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 78.dp)
        ) {
            current?.let {
                MiniPlayer(it, isPlaying, vm) { nav.navigate("player") }
            }
        }

        // --- Bottom Navigation (Stays static at the bottom) ---
        BottomNavigationBar(nav, Modifier.align(Alignment.BottomCenter))
    }
}


// ===========================================================================
// SONG ROW
// ===========================================================================
@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onToggleLike: ((Song) -> Unit)? = null, // Make this optional
    onDelete: ((Song) -> Unit)? = null,     // Make this optional too
    onShowMenu: ((Song) -> Unit)? = null, // Add this parameter
    isLiked: Boolean = false                 // Pass the liked status
) {

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // --- ALBUM ART ---
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(14.dp))

        // --- TITLE + ARTIST ---
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist ?: "Unknown",
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(14.dp))

        if (onShowMenu != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = Color.Gray,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onShowMenu(song) } // <-- Call the function here
            )
        }
    }
}
@Composable
fun PlaylistSongRow(
    song: Song,
    onClick: () -> Unit,
    onShowMenu: (Song) -> Unit, // This is not optional
    isLiked: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- ALBUM ART ---
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(14.dp))

        // --- TITLE + ARTIST ---
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist ?: "Unknown",
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(14.dp))

        // This Icon's onClick will always have the correct playlistId context
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



// ===========================================================================
// MINI PLAYER
// ===========================================================================
@Composable
fun MiniPlayer(song: Song, isPlaying: Boolean, vm: MusicViewModel, openPlayer: () -> Unit) {
    Surface(
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF3D1427),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { openPlayer() }
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            val artist = song.artist?.takeIf { it.isNotBlank() } ?: "Unknown"

            Column(Modifier.weight(1f)) {
                Text(
                    text = "${song.title} ($artist)",
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { vm.togglePlayPause() }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
            }
        }
    }
}


// ===========================================================================
// PLAYER SCREEN
// ===========================================================================
@Composable
fun PlayerScreen(nav: NavHostController, vm: MusicViewModel) {

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val position by vm.position.collectAsState()
    val duration by vm.duration.collectAsState()

    val isRepeating by vm.isRepeating.collectAsState()
    val isShuffling by vm.isShuffling.collectAsState()
    val likedSongs = vm.likedSongs


    val context = LocalContext.current
    val audioDevice = remember { getAudioDevice(context) }

    if (current == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }

    val DarkPink = Color(0xFFB30059)
    val White = Color.White
    val Black = Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(46.dp))

        // ---------- TOP BAR ----------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLAYING FROM YOUR PHONE", color = White.copy(0.6f), fontSize = 12.sp)
                Text("Local Songs", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            IconButton(onClick = {
                // Show the menu for the currently playing song
                current?.let { song ->
                    vm.showMenuForSong(song, null)
                }
            }){
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = White)
            }
        }


        Spacer(Modifier.height(32.dp))

        // ---------- ALBUM ART ----------
        Box(
            modifier = Modifier
                .size(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(current!!.albumArt)
                    .crossfade(true)
                    .build(),
                contentDescription = current!!.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(28.dp))

        // ---------- SONG TITLE & LIKE BUTTON ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column to hold Title and Artist, taking up most of the space
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = current!!.title,
                    color = White,
                    fontSize = 24.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = current!!.artist ?: "Unknown",
                    color = White.copy(0.8f),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Spacer to ensure there's some distance
            Spacer(Modifier.width(16.dp))

            // Check if the current song is in the liked list
            val isLiked = likedSongs.contains(current)

            // The Like button itself
            IconButton(onClick = { vm.toggleLike(current!!) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    // Use your theme color for liked, and white for not liked
                    tint = if (isLiked) DarkPink else White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }


        Spacer(Modifier.height(20.dp))

        // ---------- SLIDER ----------
        val dur = if (duration <= 0L) 1L else duration
        Slider(
            value = (position.coerceAtMost(dur).toFloat() / dur.toFloat()),
            onValueChange = { vm.seekTo((it * dur).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = DarkPink,
                activeTrackColor = DarkPink,
                inactiveTrackColor = White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(position), color = White.copy(0.7f), fontSize = 13.sp)
            Text(formatTime(duration), color = White.copy(0.7f), fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))

        // ---------- CONTROLS ----------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { vm.toggleShuffleMode()}) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    // Change the color based on the isShuffling state
                    tint = if (isShuffling) DarkPink else White
                )
            }

            IconButton(onClick = { vm.previous() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = White)
            }

            IconButton(onClick = { vm.togglePlayPause() }) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = White,
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = DarkPink,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = White)
            }

            IconButton(onClick = { vm.toggleRepeatMode() }) {
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    // Change the color based on the isRepeating state
                    tint = if (isRepeating) DarkPink else White
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        // ---------- DEVICE + SHARE + QUEUE ----------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val deviceIcon: ImageVector
            val deviceName: String

            when (audioDevice?.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                    deviceIcon = Icons.Default.Headphones
                    deviceName = "Headphones"
                }
                else -> {
                    deviceIcon = Icons.Default.Smartphone
                    deviceName = "This Device"
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(deviceIcon, contentDescription = "Audio Device", tint = DarkPink)
                Spacer(Modifier.width(8.dp))
                Text(deviceName, color = DarkPink, fontSize = 14.sp)
            }

            Row {
                IconButton(onClick = {
                    shareSong(context, current!!)
                }) {
                    Icon(Icons.Default.Share, tint = White, contentDescription = "Share")
                }
                IconButton(onClick = { vm.showQueueSheet() }) { // Changed from nav.navigate
                    Icon(Icons.Default.Menu, tint = White, contentDescription = "Queue")
                }
            }
        }
    }
}

// ===========================================================================
// BOTTOM NAV
// ===========================================================================
@Composable
fun BottomNavigationBar(nav: NavHostController, modifier: Modifier = Modifier) {

    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = Color.Black,
        tonalElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomItem(
                label = "Home",
                icon = Icons.Default.Home,
                selected = currentRoute == "home"
            ) { nav.navigate("home") }

            BottomItem(
                label = "Stats",
                icon = Icons.Default.ShowChart,
                selected = currentRoute == "charts"
            ) { nav.navigate("charts") }

            BottomItem(
                label = "Albums",
                icon = Icons.Default.Album, // Use a more appropriate icon
                selected = currentRoute == "albums"
            ) { nav.navigate("albums") }

            BottomItem(
                label = "Library",
                icon = Icons.Default.LibraryMusic,
                selected = currentRoute == "library"
            ) { nav.navigate("library") }
        }
    }
}
@Composable
fun BottomItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFFE40074) else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, color = color, fontSize = 12.sp)
    }
}

// In MainActivity.kt, replace the old ChartsScreen

@Composable
fun ChartsScreen(nav: NavHostController, vm: MusicViewModel) {
    val topSongs by vm.topSongs.collectAsState()
    val topArtists by vm.topArtists.collectAsState()
    val topAlbums by vm.topAlbums.collectAsState()

    // Generate the charts when the screen is first composed
    LaunchedEffect(Unit) {
        vm.generateCharts()
    }

    // For the mini player
    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ) {
            LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp,top=30.dp) // Space for mini-player + nav bar
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
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

            // --- Top Artists Section ---
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

            // --- Top Albums Section ---
            item {
                ChartSection(title = "Top Albums") {
                    // Display albums in a horizontal row
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(topAlbums) { topAlbum ->
                            ChartAlbumItem(
                                album = topAlbum.album,
                                playCount = topAlbum.playCount,
                                onClick = { /* TODO: Navigate to album detail */ }
                            )
                        }
                    }
                }
            }
        }

        // --- Mini Player & Bottom Nav ---
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

// Helper composable for a section title
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

// Helper for a top song item
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

// Helper for a top artist item
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

// Helper for a top album item
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


@Composable
fun LibraryScreen(nav: NavHostController, vm: MusicViewModel) {

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ) {
        Column(Modifier
            .fillMaxSize()
            .padding(top = 40.dp)) {

            // ---------------- Top Bar ----------------
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
                Spacer(Modifier.width(28.dp)) // empty right side for symmetry
            }

            Spacer(Modifier.height(28.dp))

            // ---------------- Library Items ----------------

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

            Spacer(Modifier.height(120.dp)) // space for mini-player + bottom bar
        }

        // ---------------- Mini Player ----------------
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

        // ---------------- Bottom Navigation ----------------
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

@Composable
fun MyMusicScreen(nav: NavHostController, vm: MusicViewModel, hasPermission: Boolean) {

    val songs by vm.songs.collectAsState()
    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val context = LocalContext.current

    // ---------------- Search State ----------------
    var searchQuery by remember { mutableStateOf("") }

    // Filter songs based on the search text
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

            // ---------------- SearchBar ----------------
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

            // ---------------- SONGS LIST ----------------
            if (!hasPermission) {
                Text(
                    "Permission required to read local audio files.",
                    color = Color.LightGray,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // Inside MyMusicScreen's LazyColumn
                    itemsIndexed(songs) { _, song ->
                        SongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, songs)
                                nav.navigate("player")
                            },
                            onDelete = { vm.deleteSong(context,it) },
                            onToggleLike = { vm.toggleLike(it) },
                            onShowMenu = { vm.showMenuForSong(it, null) }, // <-- Pass the ViewModel function
                            isLiked = vm.likedSongs.contains(song)
                        )
                    }
                }
            }

            Spacer(Modifier.height(110.dp)) // Mini-player space
        }

        // ---------------- Mini Player ----------------
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

        // ---------------- Bottom Navigation ----------------
        BottomNavigationBar(nav, Modifier.align(Alignment.BottomCenter))
    }
}


// ===========================================================================
// FAVORITES SCREEN (Refactored to match UI)
// ===========================================================================
@Composable
fun FavoratesScreen(nav: NavHostController, vm: MusicViewModel) {
    // Observe the list of liked songs directly from the ViewModel.
    // This works because mutableStateListOf is already observable by Compose.
    val likedSongs = vm.likedSongs

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    // Define colors from your theme for cleaner code
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
                .padding(horizontal = 20.dp) // Add horizontal padding to the whole column
        ) {

            // --- Top Bar with Back Arrow ---
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
                        .clickable { nav.popBackStack() } // Go back to the previous screen
                )
            }

            // --- Header Section (Artwork and Title) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Big Artwork Box
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
                    // "1 songs" label at the top-left
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

                // "Liked songs" Text
                Text(
                    text = "Liked songs",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(30.dp))

            // --- Shuffle and Play Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shuffle Button
                Button(
                    onClick = {
                        // Play the liked songs list in a random order
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

                // Play Button
                Button(
                    onClick = {
                        // Play the liked songs list from the beginning
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

            // --- Songs List ---
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
                        // Using a new SongRow variant for this screen
                        FavoriteSongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, likedSongs)
                                nav.navigate("player")
                            },
                            onToggleLike = { vm.toggleLike(it) },
                            onShowMenu = { vm.showMenuForSong(it, null) } // <-- Pass the ViewModel function
                        )
                    }
                }
            }
        }

        // MiniPlayer can be added here if needed, above the Bottom Nav
    }
}

// A new, specific SongRow for the Favorites screen to match the screenshot
@Composable
fun FavoriteSongRow(
    song: Song,
    onClick: () -> Unit,
    onToggleLike: (Song) -> Unit,
    onShowMenu: (Song) -> Unit // Add this parameter
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp), // Adjust padding to match screenshot
        verticalAlignment = Alignment.CenterVertically
    ) {
        // You can add a visualizer icon or album art here if you want
        Icon(Icons.Default.GraphicEq, null, tint = Color(0xFFE40074), modifier = Modifier.size(28.dp))

        Spacer(Modifier.width(16.dp))

        // Title and Artist
        Column(Modifier.weight(1f)) {
            Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFFE40074))
            Text(song.artist ?: "Unknown", maxLines = 1, color = Color.Gray)
        }

        Spacer(Modifier.width(16.dp))

        // Like button
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Unlike",
            tint = Color(0xFFE40074),
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleLike(song) }
        )

        Spacer(Modifier.width(8.dp))

        // More options button (optional)
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




fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}



// Add this function to your MainActivity.kt file
fun getAudioDevice(context: Context): AudioDeviceInfo? {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
    return devices.firstOrNull() // The first device is usually the active one
}

// Add this new function to your MainActivity.kt
fun shareSong(context: Context, song: Song) {
    //1. Create the text content to be shared
    val shareText = "Check out this song: ${song.title} by ${song.artist}"

    // 2. Create the Share Intent
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    // 3. Create a chooser to show the user a list of apps
    val shareIntent = Intent.createChooser(sendIntent, "Share Song")

    // 4. Launch the chooser
    // We need to add a flag because we are calling this from outside an Activity
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(shareIntent)
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
