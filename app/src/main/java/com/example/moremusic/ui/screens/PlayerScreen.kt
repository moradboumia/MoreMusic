package com.example.moremusic.ui.screens

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.moremusic.util.shareSong
import com.example.moremusic.MusicViewModel

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
                current?.let { song ->
                    vm.showMenuForSong(song, null)
                }
            }){
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = White)
            }
        }


        Spacer(Modifier.height(32.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

            Spacer(Modifier.width(16.dp))

            val isLiked = likedSongs.contains(current)

            IconButton(onClick = { vm.toggleLike(current!!) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) DarkPink else White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }


        Spacer(Modifier.height(20.dp))

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

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { vm.toggleShuffleMode()}) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
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
                    tint = if (isRepeating) DarkPink else White
                )
            }
        }

        Spacer(Modifier.height(26.dp))

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
                IconButton(onClick = { vm.showQueueSheet() }) { 
                    Icon(Icons.Default.Menu, tint = White, contentDescription = "Queue")
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

fun getAudioDevice(context: Context): AudioDeviceInfo? {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
    return devices.firstOrNull()
}