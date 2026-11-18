package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = OnVioletContainerLight,
    secondary = TangerineSecondary,
    onSecondary = Color.White,
    secondaryContainer = TangerineContainerLight,
    onSecondaryContainer = OnTangerineContainerLight,
    tertiary = SkyTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SkyContainerLight,
    onTertiaryContainer = OnSkyContainerLight,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = RoseWrong,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = Color(0xFF1A0A52),
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = OnVioletContainerDark,
    secondary = TangerineSecondaryDark,
    onSecondary = Color(0xFF3E1800),
    secondaryContainer = TangerineContainerDark,
    onSecondaryContainer = OnTangerineContainerDark,
    tertiary = SkyTertiaryDark,
    onTertiary = Color(0xFF002B45),
    tertiaryContainer = SkyContainerDark,
    onTertiaryContainer = OnSkyContainerDark,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = RoseWrong,
    onError = Color.White
)

val LingoBuddyShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun LingoBuddyTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LingoBuddyTypography,
        shapes = LingoBuddyShapes,
        content = content
    )
}
