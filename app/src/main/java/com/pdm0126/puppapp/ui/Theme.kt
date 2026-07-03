package com.pdm0126.puppapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange10  = Color(0xFF2E1500)
val Orange20  = Color(0xFF512400)
val Orange40  = Color(0xFF964B00)
val Orange60  = Color(0xFFFF823F)
val Orange80  = Color(0xFFFFB894)
val Orange90  = Color(0xFFFFDBCF)
val Orange95  = Color(0xFFFFFFFF)

val Green40   = Color(0xFF059669)
val Amber40   = Color(0xFFD97706)
val Red40     = Color(0xFFDC2626)

private val LightColors = lightColorScheme(
    primary          = Orange60,
    onPrimary        = Color.White,
    primaryContainer = Orange90,
    onPrimaryContainer = Orange10,
    secondary        = Orange40,
    onSecondary      = Color.White,
    secondaryContainer = Orange90,
    onSecondaryContainer = Orange20,
    background       = Orange95,
    onBackground     = Orange10,
    surface          = Color.White,
    onSurface        = Orange10,
    surfaceVariant   = Orange90,
    onSurfaceVariant = Orange40,
    outline          = Orange80,
)

@Composable
fun PupappTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
