
package com.mixcheck.ai.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF7C4DFF),
    background = Color(0xFF0A0A0F),
    surface = Color(0xFF12121A),
    onBackground = Color(0xFFEDE7F6),
    onSurface = Color(0xFFEDE7F6),
    error = Color(0xFFFF5252)
)

@Composable
fun MixCheckTheme(content: @Composable ()->Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography(), content = content)
}
