package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = NileSapphire,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E7F7),
    onPrimaryContainer = NileSapphireDark,
    secondary = DesertTeal,
    onSecondary = Color.White,
    secondaryContainer = DesertTealContainer,
    onSecondaryContainer = Color(0xFF043635),
    tertiary = PapyrusGold,
    onTertiary = Color(0xFF281900),
    tertiaryContainer = PapyrusGoldContainer,
    onTertiaryContainer = Color(0xFF3E2900),
    background = SandBackground,
    onBackground = InkPrimary,
    surface = SandSurface,
    onSurface = InkPrimary,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = Color(0xFFCBD5E1),
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonErrorBg
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5EB5E8),
    onPrimary = Color(0xFF002438),
    primaryContainer = NileSapphire,
    onPrimaryContainer = Color(0xFFD3E7F7),
    secondary = Color(0xFF4FD1C5),
    onSecondary = Color(0xFF002928),
    secondaryContainer = Color(0xFF0B4F4E),
    onSecondaryContainer = DesertTealContainer,
    tertiary = PapyrusGoldLight,
    onTertiary = Color(0xFF281900),
    tertiaryContainer = Color(0xFF5C3E00),
    onTertiaryContainer = PapyrusGoldContainer,
    background = MidnightBackground,
    onBackground = LightInkPrimary,
    surface = MidnightSurface,
    onSurface = LightInkPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = LightInkSecondary,
    outline = Color(0xFF334155),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A)
)

private val SmartStudyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SmartStudyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isArabic: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = smartStudyTypography(isArabic),
            shapes = SmartStudyShapes,
            content = content
        )
    }
}
