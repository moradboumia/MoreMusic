package com.example.moremusic

import android.app.Application
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import com.example.moremusic.FavoritesManager
import com.example.moremusic.HistoryManager
import com.example.moremusic.PlaylistManager
import com.example.moremusic.model.Album
import com.example.moremusic.model.Playlist
import com.example.moremusic.model.Song
import com.example.moremusic.model.TopAlbum
import com.example.moremusic.model.TopArtist
import com.example.moremusic.model.TopSong
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.RecoverableSecurityException
import android.content.ContentUris


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

    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex = _currentSongIndex.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private val favoritesManager = FavoritesManager(application)
    private val playlistManager = PlaylistManager(application)
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()
    private var pollingStarted = false

    private val _isRepeating = MutableStateFlow(false)
    val isRepeating = _isRepeating.asStateFlow()
    private val _isShuffling = MutableStateFlow(false)
    val isShuffling = _isShuffling.asStateFlow()

    private val _songForMenu = MutableStateFlow<Pair<Song, Long?>?>(null)
    val songForMenu = _songForMenu.asStateFlow()

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


    fun clearToastMessage() {
        _toastMessage.value = null
    }
    fun showAddToPlaylistSheet(song: Song) {
        _songToAdd.value = song
        _songForMenu.value = null
    }

    fun dismissAddToPlaylistSheet() {
        _songToAdd.value = null
    }

    fun showQueueSheet() {
        _isQueueSheetVisible.value = true
    }

    fun dismissQueueSheet() {
        _isQueueSheetVisible.value = false
    }
    fun toggleShuffleMode() {
        _isShuffling.value = !_isShuffling.value
        player?.shuffleModeEnabled = _isShuffling.value
    }

    fun toggleRepeatMode() {
        _isRepeating.value = !_isRepeating.value
        player?.repeatMode = if (_isRepeating.value) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
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
        likedSongs.addAll(favoritesManager.loadLikedSongs())
        _playlists.value = playlistManager.loadPlaylists()
    }

    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(name = name, songIds = emptyList())
        val updatedPlaylists = _playlists.value + newPlaylist
        _playlists.value = updatedPlaylists
        playlistManager.savePlaylists(updatedPlaylists)
    }

    fun addSongToPlaylist(song: Song, playlist: Playlist) {
        val targetPlaylist = _playlists.value.find { it.id == playlist.id } ?: return

        if (targetPlaylist.songIds.contains(song.id)) {
            _toastMessage.value = "Song is already in \"${playlist.name}\""
        } else {
            val updatedPlaylists = _playlists.value.map {
                if (it.id == playlist.id) {
                    it.copy(songIds = it.songIds + song.id)
                } else {
                    it
                }
            }
            _playlists.value = updatedPlaylists
            playlistManager.savePlaylists(updatedPlaylists)
            _toastMessage.value = "Added to \"${playlist.name}\""
        }
    }



    fun deletePlaylist(playlist: Playlist) {
        val updatedPlaylists = _playlists.value.filter { it.id != playlist.id }
        _playlists.value = updatedPlaylists
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
        player?.let {
            val items = mutableListOf<MediaItem>()
            if (it.mediaItemCount > 0) {
                for (i in 0 until it.mediaItemCount) {
                    items.add(it.getMediaItemAt(i))
                }
            }
            _playQueue.value = items
        }
    }

    fun removeSongFromPlaylist(song: Song, playlistId: Long) {
        val updatedPlaylists = _playlists.value.map {
            if (it.id == playlistId) {
                val updatedSongIds = it.songIds.filter { id -> id != song.id }
                it.copy(songIds = updatedSongIds)
            } else {
                it
            }
        }
        _playlists.value = updatedPlaylists
        playlistManager.savePlaylists(updatedPlaylists)
        _toastMessage.value = "Removed \"${song.title}\" from playlist"
    }

    private fun startPositionPolling() {
        pollingStarted = true

        viewModelScope.launch {
            while (true) {
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
                .filter { it.albumArt != null }
                .groupBy { it.albumArt }
                .map { (artworkUri, songsInAlbum) ->
                    val firstSong = songsInAlbum.first()
                    val albumName = firstSong.artist ?: "Unknown Album"
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
                                .setArtworkUri(song.albumArt)
                                .build()
                        )
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

            _topSongs.value = history
                .groupingBy { it.songId }
                .eachCount()
                .mapNotNull { (songId, count) ->
                    val song = allSongs.find { it.id == songId }
                    if (song != null) TopSong(song, count) else null
                }
                .sortedByDescending { it.playCount }
                .take(20)
            _topArtists.value = history
                .mapNotNull { event -> allSongs.find { it.id == event.songId }?.artist }
                .filterNotNull()
                .groupingBy { it }
                .eachCount()
                .map { (artistName, count) -> TopArtist(artistName, count) }
                .sortedByDescending { it.playCount }
                .take(20)

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

    fun deleteSong(context: Context, song: Song) {
        viewModelScope.launch {
            try {
                context.contentResolver.delete(song.uri, null, null)

                _songs.value = _songs.value.filter { it.id != song.id }
                _toastMessage.value = "Deleted \"${song.title}\""

            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableException = e as? RecoverableSecurityException
                    if (recoverableException != null) {
                        _pendingDeleteRequest.value = Pair(recoverableException.userAction.actionIntent.intentSender, song)
                    } else {
                        _toastMessage.value = "Could not delete file: Permission denied."
                    }
                } else {
                    _toastMessage.value = "Could not delete file: Permission denied."
                }
            }
        }
    }

    fun clearPendingDeleteRequest() {
        _pendingDeleteRequest.value = null
    }



    fun next() = player?.seekToNext()
    fun previous() = player?.seekToPrevious()
    fun seekTo(ms: Long) = player?.seekTo(ms)


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
