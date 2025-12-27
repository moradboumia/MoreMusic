package com.example.moremusic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.moremusic.ui.components.AppDrawerContent
import com.example.moremusic.ui.components.QueueSheet
import com.example.moremusic.ui.components.SongMenuSheet
import com.example.moremusic.ui.screens.AlbumScreen
import com.example.moremusic.ui.screens.ChartsScreen
import com.example.moremusic.ui.screens.FavoratesScreen
import com.example.moremusic.ui.screens.HomeScreen
import com.example.moremusic.ui.screens.LibraryScreen
import com.example.moremusic.ui.screens.MyMusicScreen
import com.example.moremusic.ui.screens.PlayerScreen
import com.example.moremusic.ui.screens.PlaylistDetailScreen
import com.example.moremusic.ui.screens.PlaylistsScreen
import com.example.moremusic.ui.theme.background
import com.example.moremusic.MusicViewModel
import com.example.moremusic.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = background.toArgb()
        window.navigationBarColor = background.toArgb()
        setContent {
           MusicApp()
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
        factory = ViewModelFactory(application)
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
                drawerContainerColor = Color.Transparent
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
                vm.clearToastMessage()
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

        val menuState by vm.songForMenu.collectAsState()
        val songForMenu = menuState?.first
        val playlistIdForMenu = menuState?.second
        val menuSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )

        val context = LocalContext.current
        val viewModel: MusicViewModel = viewModel()
        val pendingDeleteRequest by viewModel.pendingDeleteRequest.collectAsState()

        val deleteRequestLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                pendingDeleteRequest?.second?.let { songToDelete ->
                    Toast.makeText(context, "Permission granted. Deleting file...", Toast.LENGTH_SHORT).show()
                    viewModel.deleteSong(context, songToDelete)
                }
            } else {
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
                vm.dismissMenu()
            }
        }


        val isQueueSheetVisible by vm.isQueueSheetVisible.collectAsState()
        val queueSheetState = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true
        )

        LaunchedEffect(isQueueSheetVisible) {
            if (isQueueSheetVisible) {
                queueSheetState.show()
            } else {
                if (queueSheetState.isVisible) {
                    queueSheetState.hide()
                }
            }
        }

        LaunchedEffect(queueSheetState.isVisible) {
            if (!queueSheetState.isVisible) {
                vm.hideQueueSheet()
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
                        } ?: Spacer(modifier = Modifier.height(1.dp))
                    }
                ) {
                    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFE40074))) {
                        NavHost(
                            navController = nav, 
                            startDestination = "home",
                            enterTransition = { EnterTransition.None },
                            exitTransition = { ExitTransition.None }
                        ) {
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
                                    PlaylistDetailScreen(
                                        playlistId = playlistId,
                                        nav = nav,
                                        vm = vm
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
