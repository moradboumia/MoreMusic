package com.example.moremusic


import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause

import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
// ===========================================================================
// IMPORTS
// ===========================================================================
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.IconButton

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// ===========================================================================
// DATA MODEL
// ===========================================================================
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val uri: Uri,
    val albumArt: Uri?
)
data class LikedSong(
    val id: Long,
    val title: String,
    val artist: String
)

// ===========================================================================
// VIEWMODEL
// ===========================================================================
class MusicViewModel : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    private val _likedsongs = MutableStateFlow<List<Song>>(emptyList())
    val likedsongs = _likedsongs.asStateFlow()

    private val _current = MutableStateFlow<Song?>(null)
    val current = _current.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private var player: ExoPlayer? = null
    private var pollingStarted = false

    var likedSongs = mutableStateListOf<Song>()
        private set


    // -------------------- Init Player ------------------------
    fun initPlayer(p: ExoPlayer) {
        player = p

        player?.addListener(object : androidx.media3.common.Player.Listener {

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                _isPlaying.value = isPlayingNow
                if (isPlayingNow && !pollingStarted) startPositionPolling()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _duration.value = player?.duration ?: 0L

                // 🔑 Update current song when track changes
                val uri = mediaItem?.localConfiguration?.uri
                val newSong = _songs.value.find { it.uri == uri }
                _current.value = newSong
            }
        })
    }



    // -------------------- Position Poll ------------------------
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

            _songs.value = list
        }
    }


    // -------------------- Control: Play Song ------------------------
    fun playSong(clicked: Song, queue: List<Song>) {

        viewModelScope.launch {

            player?.apply {
                clearMediaItems()

                queue.forEach { addMediaItem(MediaItem.fromUri(it.uri)) }

                val idx = queue.indexOfFirst { it.uri == clicked.uri }.coerceAtLeast(0)
                seekTo(idx, 0)

                prepare()
                play()
            }

            _current.value = clicked
            _isPlaying.value = true
        }
    }


    // -------------------- Controls ------------------------
    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) p.pause() else p.play()
        _isPlaying.value = p.isPlaying
    }

    //favorate songs
    fun likeSong(song: Song) {
        if (!likedSongs.contains(song)) {
            likedSongs.add(song)
        }
    }
    fun unlikeSong(song: Song) {
        likedSongs.remove(song)
    }

    fun toggleLike(song: Song) {
        if (likedSongs.contains(song)) {
            likedSongs.remove(song)
        } else {
            likedSongs.add(song)
        }
    }

    fun deleteSong(song: Song) {
        val updatedList = _songs.value.toMutableList().apply {
            remove(song)
        }
        _songs.value = updatedList

        // If the deleted song is the current one, clear it
        if (_current.value?.id == song.id) {
            _current.value = null
            player?.stop()
        }
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

// ===========================================================================
// MAIN ACTIVITY
// ===========================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MusicApp() }
    }
}

// ===========================================================================
// APP ROOT
// ===========================================================================
@Composable
fun MusicApp() {
    val context = LocalContext.current
    val nav = rememberNavController()
    val vm: MusicViewModel = viewModel()

    // Player
    val player = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(player) {
        vm.initPlayer(player)
        onDispose { player.release() }
    }

    // Permissions
    val permission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    var hasPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) vm.loadLocalMusic(context)
    }

    LaunchedEffect(Unit) {
        val granted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            hasPermission = true
            vm.loadLocalMusic(context)
        } else launcher.launch(permission)
    }

    // Theme + NavHost
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFE40074))) {
        NavHost(navController = nav, startDestination = "home") {
            composable("home") { HomeScreen(nav, vm, hasPermission) }
            composable("my_music") { MyMusicScreen(nav, vm, hasPermission) }
            composable("player") { PlayerScreen(nav, vm) }
            composable("charts") { ChartsScreen(nav) }
            composable("videos") { VideosScreen(nav) }
            composable("library") { LibraryScreen(nav,vm) }
            composable("favorates_screen") {
                FavoratesScreen(nav, vm)
            }


        }
    }
}

// ===========================================================================
// HOME SCREEN
// ===========================================================================
@Composable
fun HomeScreen(nav: NavHostController, vm: MusicViewModel, hasPermission: Boolean) {

    val songs by vm.songs.collectAsState()
    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    // ---------------- Search State ----------------
    var searchQuery by remember { mutableStateOf("") }

    // Filter songs based on the search text
    val filteredSongs = songs.filter { song ->
        song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist?.contains(searchQuery, ignoreCase = true) == true ||
                song.uri.toString().contains(searchQuery, ignoreCase = true)
    }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ) {

        Column(Modifier.fillMaxSize()) {

            Spacer(Modifier.height(18.dp))

            // Top Bar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                Box(
                    Modifier.size(40.dp)
                        .background(Color.Gray, RoundedCornerShape(50))
                )
            }

            Spacer(Modifier.height(12.dp))

            Text("Hi There,", color = Color(0xFFE40074), fontSize = 32.sp, modifier = Modifier.padding(start = 20.dp))

            Spacer(Modifier.height(16.dp))

            // ---------------- SearchBar ----------------
            Box(
                Modifier.padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 14.dp),
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

            Text("All local songs", color = Color(0xFFE40074), fontSize = 20.sp, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))

            // ---------------- SONGS LIST ----------------
            if (!hasPermission) {
                Text(
                    "Permission required to read local audio files.",
                    color = Color.LightGray,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(filteredSongs) { _, song ->
                        SongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, filteredSongs)  // play filtered list
                                nav.navigate("player")
                            },
                            onDelete = { vm.deleteSong(it) }
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
            modifier = Modifier.align(Alignment.BottomCenter)
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
// SONG ROW
// ===========================================================================
@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onDelete: (Song) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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

        // --- THREE DOTS + MENU ---
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Favorites") },
                    onClick = {
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete(song)
                        menuExpanded = false
                    }
                )
            }
        }
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
        color = Color(0xFF480320),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { openPlayer() }
    ) {
        Row(
            Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArt,
                contentDescription = song.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(song.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist ?: "Unknown", color = Color.LightGray, fontSize = 13.sp)
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

    var menuExpanded by remember { mutableStateOf(false) }

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

        Spacer(Modifier.height(16.dp))

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

             IconButton(onClick = { menuExpanded = true }){
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = White)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Favorites") },
                    onClick = {
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                    }
                )
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

        // ---------- SONG TITLE ----------
        Text(
            text = current!!.title,
            color = White,
            fontSize = 24.sp,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = current!!.artist ?: "Unknown",
            color = White.copy(0.8f),
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

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

            IconButton(onClick = { /* shuffle */ }) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = White)
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
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = White)
            }

            IconButton(onClick = { /* repeat */ }) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = White)
            }
        }

        Spacer(Modifier.height(26.dp))

        // ---------- DEVICE + SHARE + QUEUE ----------
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Headphones, tint = DarkPink, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("This Device", color = DarkPink, fontSize = 14.sp)
            }

            Row {
                IconButton(onClick = { /* share */ }) {
                    Icon(Icons.Default.Share, tint = White, contentDescription = null)
                }
                IconButton(onClick = { nav.navigate("my_music")}) {
                    Icon(Icons.Default.Menu, tint = White, contentDescription = null)
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
        modifier = modifier.fillMaxWidth().height(78.dp)
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
                label = "Charts",
                icon = Icons.Default.ShowChart,
                selected = currentRoute == "charts"
            ) { nav.navigate("charts") }

            BottomItem(
                label = "Videos",
                icon = Icons.Default.VideoLibrary,
                selected = currentRoute == "videos"
            ) { nav.navigate("videos") }

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


@Composable
fun ChartsScreen(nav: NavHostController) {
    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ){


        BottomNavigationBar(nav, Modifier.align(Alignment.BottomCenter))

    }
}
@Composable
fun VideosScreen(nav: NavHostController) {
    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ){


        BottomNavigationBar(nav, Modifier.align(Alignment.BottomCenter))

    }
}
@Composable
fun LibraryScreen(nav: NavHostController, vm: MusicViewModel) {

    val current by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ) {

        Column(Modifier.fillMaxSize().padding(top = 20.dp)) {

            // ---------------- Top Bar ----------------
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
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
            LibraryItem(Icons.Default.Notifications, "Subscriptions") {

            }

            LibraryItem(Icons.Default.QueueMusic, "Now Playing") {
                nav.navigate("player")
            }

            LibraryItem(Icons.Default.Refresh, "Last Session") {
                // ...
            }

            LibraryItem(Icons.Default.FavoriteBorder, "Favorites") {
                nav.navigate("favorates_screen")
            }

            LibraryItem(Icons.Default.Folder, "My Music") {
                nav.navigate("my_music")
            }

            LibraryItem(Icons.Default.PlaylistPlay, "Playlists") {
                // ...
            }

            LibraryItem(Icons.Default.ShowChart, "Stats") {
                // ...
            }

            Spacer(Modifier.height(120.dp)) // space for mini-player + bottom bar
        }

        // ---------------- Mini Player ----------------
        AnimatedVisibility(
            visible = current != null,
            modifier = Modifier.align(Alignment.BottomCenter)
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

    // ---------------- Search State ----------------
    var searchQuery by remember { mutableStateOf("") }

    // Filter songs based on the search text
    val filteredSongs = songs.filter { song ->
        song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist?.contains(searchQuery, ignoreCase = true) == true ||
                song.uri.toString().contains(searchQuery, ignoreCase = true)
    }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF111111), Color.Black)))
    ) {

        Column(Modifier.fillMaxSize()) {

            Spacer(Modifier.height(18.dp))

            // Top Bar

            Spacer(Modifier.height(12.dp))

            Text("My Music", color = Color(0xFFE40074), fontSize = 32.sp, modifier = Modifier.padding(start = 20.dp))

            Spacer(Modifier.height(16.dp))

            // ---------------- SearchBar ----------------
            Box(
                Modifier.padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 14.dp),
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
                    itemsIndexed(filteredSongs) { _, song ->
                        SongRow(
                            song = song,
                            onClick = {
                                vm.playSong(song, filteredSongs)  // play filtered list
                                nav.navigate("player")
                            },
                            onDelete = { vm.deleteSong(it) }
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
            modifier = Modifier.align(Alignment.BottomCenter)
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
@Composable
fun FavoratesScreen(nav: NavController, vm: MusicViewModel) {
    val likedsongs by vm.likedsongs.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Favorite Songs",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        if (likedsongs.isEmpty()) {
            Text("No liked songs yet")
        } else {
            LazyColumn {
                items(likedsongs) { song ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable {
                                vm.playSong(song, likedsongs)
                                nav.navigate("player")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(Modifier.weight(1f)) {
                            Text(song.title, fontSize = 18.sp)
                            Text(song.artist ?: "Unknown", fontSize = 14.sp)
                        }

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.clickable {
                                vm.unlikeSong(song)
                            }
                        )
                    }
                }
            }
        }
    }
}


fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

