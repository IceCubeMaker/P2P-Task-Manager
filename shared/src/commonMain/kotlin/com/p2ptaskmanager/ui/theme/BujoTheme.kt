package com.p2ptaskmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColorScheme = lightColorScheme(
    primary = GoldAccent,
    onPrimary = Parchment,
    primaryContainer = ParchmentDark,
    onPrimaryContainer = InkDark,
    secondary = InkMedium,
    onSecondary = Parchment,
    secondaryContainer = ParchmentSurface,
    onSecondaryContainer = InkDark,
    tertiary = ForestGreen,
    onTertiary = Parchment,
    background = Parchment,
    onBackground = InkDark,
    surface = Parchment,
    onSurface = InkDark,
    surfaceVariant = ParchmentSurface,
    onSurfaceVariant = InkMedium,
    outline = InkFaint,
    outlineVariant = InkFaint,
    error = CrimsonError,
    onError = Parchment,
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccentDark,
    onPrimary = Leather,
    primaryContainer = LeatherMid,
    onPrimaryContainer = CreamText,
    secondary = CreamMuted,
    onSecondary = Leather,
    secondaryContainer = LeatherSurface,
    onSecondaryContainer = CreamText,
    background = Leather,
    onBackground = CreamText,
    surface = Leather,
    onSurface = CreamText,
    surfaceVariant = LeatherSurface,
    onSurfaceVariant = CreamMuted,
    outline = CreamFaint,
    outlineVariant = CreamFaint,
    error = CrimsonDark,
    onError = Leather,
)

// Sharp corners — paper-cut aesthetic
private val BujoShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

@Composable
fun BujoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = bujoTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = BujoShapes,
        content = content
    )
}

@Composable
private fun bujoTypography(): Typography {
    val fontFamily = bujoFontFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp),
        displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
        displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.5.sp),
        headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
        titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    )
}

@Composable
expect fun bujoFontFamily(): FontFamily
