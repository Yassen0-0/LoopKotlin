package com.loop.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    background = LoopColor.LightBackground,
    onBackground = LoopColor.LightForeground,
    surface = LoopColor.LightCard,
    onSurface = LoopColor.LightForeground,
    surfaceVariant = LoopColor.LightSurface,
    onSurfaceVariant = LoopColor.LightMutedFg,
    primary = LoopColor.LightPrimary,
    onPrimary = LoopColor.LightPrimaryFg,
    secondary = LoopColor.LightSurface,
    onSecondary = LoopColor.LightForeground,
    outline = LoopColor.LightBorder,
    error = LoopColor.Destructive,
)

private val DarkColorScheme = darkColorScheme(
    background = LoopColor.DarkBackground,
    onBackground = LoopColor.DarkForeground,
    surface = LoopColor.DarkCard,
    onSurface = LoopColor.DarkForeground,
    surfaceVariant = LoopColor.DarkSurface,
    onSurfaceVariant = LoopColor.DarkMutedFg,
    primary = LoopColor.DarkPrimary,
    onPrimary = LoopColor.DarkPrimaryFg,
    secondary = LoopColor.DarkSurface,
    onSecondary = LoopColor.DarkForeground,
    outline = LoopColor.DarkBorder,
    error = LoopColor.DestructiveDark,
)

private val LoopTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
    ),
)

private val LoopShapes = Shapes(
    extraSmall = RoundedCornerShape(LoopRadius.sm),
    small = RoundedCornerShape(LoopRadius.md),
    medium = RoundedCornerShape(LoopRadius.lg),
    large = RoundedCornerShape(LoopRadius.xl),
    extraLarge = RoundedCornerShape(LoopRadius.xxl),
)

@Composable
fun LoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = LoopTypography,
        shapes = LoopShapes,
        content = content,
    )
}
