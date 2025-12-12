package com.example.moremusic.ui.theme.theme
import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// A basic dark color scheme for your app
private val DarkColorScheme = darkColorScheme(
    background = Color.Black,
    surface = Color(0xFF191919), // A slightly off-black for surfaces
    onBackground = Color.White, // Text on background
    onSurface = Color.White     // Text on surfaces
)

@Composable
fun MoreMusicTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar to be transparent to achieve edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()

            //  ✅ THIS IS THE FIX ✅
            // This line tells the system that the status bar background is NOT light,
            // so it should use light (white) icons for contrast.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
