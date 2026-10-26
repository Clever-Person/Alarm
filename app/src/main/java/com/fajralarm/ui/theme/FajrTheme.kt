package com.fajralarm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Night = darkColorScheme(primary = Color(0xFFB7D8CC), secondary = Color(0xFFE7BE87), background = Color(0xFF0B1117), surface = Color(0xFF151E26), onPrimary = Color(0xFF102019), onBackground = Color(0xFFE8EEF1), onSurface = Color(0xFFE8EEF1))
private val Day = lightColorScheme(primary = Color(0xFF245F52), secondary = Color(0xFF815A27), background = Color(0xFFF3F6F4), surface = Color.White)

@Composable fun FajrTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Night else Day, content = content)
}
