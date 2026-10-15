package com.zubki.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
class Palette(
    val bg: Color,
    val card: Color,
    val cardDone: Color,
    val text: Color,
    val textSoft: Color,
    val track: Color,
    val mint: Color,
    val mintDeep: Color,
    val onMint: Color,
    val sun: Color,
    val moon: Color,
)

private val Light = Palette(
    bg = Color(0xFFFFF8F0),
    card = Color(0xFFFFFFFF),
    cardDone = Color(0xFFE4F5EE),
    text = Color(0xFF3A3330),
    textSoft = Color(0xFF8C7F77),
    track = Color(0xFFF1E6DA),
    mint = Color(0xFF7FD1B9),
    mintDeep = Color(0xFF3FA88B),
    onMint = Color(0xFF2B2623),
    sun = Color(0xFFFFC95C),
    moon = Color(0xFFA9A7E8),
)

private val Dark = Palette(
    bg = Color(0xFF1F1B1A),
    card = Color(0xFF2A2523),
    cardDone = Color(0xFF26403A),
    text = Color(0xFFF5EBE3),
    textSoft = Color(0xFFA89B92),
    track = Color(0xFF3A3331),
    mint = Color(0xFF7FD1B9),
    mintDeep = Color(0xFF4DBB9C),
    onMint = Color(0xFF2B2623),
    sun = Color(0xFFFFC95C),
    moon = Color(0xFFB9B7F0),
)

private val LocalPalette = staticCompositionLocalOf { Light }

// Цвета приложения берём отсюда: palette.bg, palette.text и так далее
val palette: Palette
    @Composable
    @ReadOnlyComposable
    get() = LocalPalette.current

@Composable
fun ZubkiTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) Dark else Light

    CompositionLocalProvider(LocalPalette provides colors) {
        MaterialTheme(
            colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
            content = content,
        )
    }
}
