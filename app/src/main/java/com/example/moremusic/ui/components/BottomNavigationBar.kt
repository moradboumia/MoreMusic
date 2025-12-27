package com.example.moremusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moremusic.ui.theme.backgroundPrime

@Composable
fun BottomNavigationBar(nav: NavHostController, modifier: Modifier = Modifier) {

    val navBackStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = backgroundPrime,
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
                icon = Icons.Default.Album, 
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