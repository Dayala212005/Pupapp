package com.pdm0126.puppapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple10  = Color(0xFF1a0a4d)
val Purple20  = Color(0xFF2D1B69)
val Purple40  = Color(0xFF5B21B6)
val Purple60  = Color(0xFF7C3AED)
val Purple80  = Color(0xFFAB8DF5)
val Purple90  = Color(0xFFEDE9FE)
val Purple95  = Color(0xFFF5F3FF)

val Green40   = Color(0xFF059669)
val Amber40   = Color(0xFFD97706)
val Red40     = Color(0xFFDC2626)

private val LightColors = lightColorScheme(
    primary          = Purple60,
    onPrimary        = Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary        = Purple40,
    onSecondary      = Color.White,
    secondaryContainer = Purple90,
    onSecondaryContainer = Purple20,
    background       = Purple95,
    onBackground     = Purple10,
    surface          = Color.White,
    onSurface        = Purple10,
    surfaceVariant   = Purple90,
    onSurfaceVariant = Purple40,
    outline          = Purple80,
)

@Composable
fun PupappTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}