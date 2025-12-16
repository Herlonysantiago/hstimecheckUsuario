package com.hs.solutions.hstimecheck_2_0.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// CORES BÁSICAS – você pode ajustar depois
private val LightColors = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    secondary = Color(0xFF4E7BBF),
    onSecondary = Color.White,
    background = Color(0xFFF2F2F2),
    surface = Color.White
)

@Composable
fun HsTimeCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
