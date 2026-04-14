package com.trishit.optimite.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// ─── Custom easings (M3 Expressive motion vocabulary) ────────────────────────────
val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
val EaseOutCubic  = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
val EaseOutBack   = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7AD9FF),
    onPrimary = Color(0xFF003547),
    primaryContainer = Color(0xFF004D66),
    onPrimaryContainer = Color(0xFFC4EDFF),
    secondary = Color(0xFF7FDDB0),
    onSecondary = Color(0xFF003821),
    tertiary = Color(0xFFFFD36A),
    onTertiary = Color(0xFF3E2E00),
    error = Color(0xFFFFB3B8),
    onError = Color(0xFF680016),
    errorContainer = Color(0xFF930025),
    onErrorContainer = Color(0xFFFFD9DC),
    background = Color(0xFF0B0E13),
    onBackground = Color(0xFFE3E8F1),
    surface = Color(0xFF0F1319),
    onSurface = Color(0xFFE3E8F1),
    surfaceVariant = Color(0xFF202733),
    onSurfaceVariant = Color(0xFFB4C1D4),
    outline = Color(0xFF8C97A8),
    outlineVariant = Color(0xFF3B4656)
)

private val AppLightColorScheme = lightColorScheme(
    primary = Color(0xFF006686),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFE9FF),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF2F6B4B),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF755A00),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF171C24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171C24),
    surfaceVariant = Color(0xFFDFE6F1),
    onSurfaceVariant = Color(0xFF434C5C),
    outline = Color(0xFF737C8D),
    outlineVariant = Color(0xFFC3CADA)
)

private data class LegacyPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val primary: Color,
    val primaryDim: Color,
    val secondary: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val glowCyan: Color,
    val glowGreen: Color
)

private val DarkLegacyPalette = LegacyPalette(
    background = AppDarkColorScheme.background,
    surface = AppDarkColorScheme.surface,
    surfaceVariant = AppDarkColorScheme.surfaceVariant,
    border = AppDarkColorScheme.outlineVariant,
    primary = AppDarkColorScheme.primary,
    primaryDim = AppDarkColorScheme.primaryContainer,
    secondary = AppDarkColorScheme.secondary,
    warning = AppDarkColorScheme.tertiary,
    danger = AppDarkColorScheme.error,
    textPrimary = AppDarkColorScheme.onSurface,
    textSecondary = AppDarkColorScheme.onSurfaceVariant,
    textMuted = AppDarkColorScheme.outline,
    glowCyan = AppDarkColorScheme.primary.copy(alpha = 0.2f),
    glowGreen = AppDarkColorScheme.secondary.copy(alpha = 0.2f)
)

private val LightLegacyPalette = LegacyPalette(
    background = AppLightColorScheme.background,
    surface = AppLightColorScheme.surface,
    surfaceVariant = AppLightColorScheme.surfaceVariant,
    border = AppLightColorScheme.outlineVariant,
    primary = AppLightColorScheme.primary,
    primaryDim = AppLightColorScheme.primaryContainer,
    secondary = AppLightColorScheme.secondary,
    warning = AppLightColorScheme.tertiary,
    danger = AppLightColorScheme.error,
    textPrimary = AppLightColorScheme.onSurface,
    textSecondary = AppLightColorScheme.onSurfaceVariant,
    textMuted = AppLightColorScheme.outline,
    glowCyan = AppLightColorScheme.primary.copy(alpha = 0.2f),
    glowGreen = AppLightColorScheme.secondary.copy(alpha = 0.2f)
)

private var currentLegacyPalette = DarkLegacyPalette

// Compatibility bridge while the rest of the UI migrates from custom tokens to Material roles.
object AppColors {
    val Background: Color get() = currentLegacyPalette.background
    val Surface: Color get() = currentLegacyPalette.surface
    val SurfaceVariant: Color get() = currentLegacyPalette.surfaceVariant
    val Border: Color get() = currentLegacyPalette.border

    val Primary: Color get() = currentLegacyPalette.primary
    val PrimaryDim: Color get() = currentLegacyPalette.primaryDim
    val Secondary: Color get() = currentLegacyPalette.secondary
    val Warning: Color get() = currentLegacyPalette.warning
    val Danger: Color get() = currentLegacyPalette.danger

    val TextPrimary: Color get() = currentLegacyPalette.textPrimary
    val TextSecondary: Color get() = currentLegacyPalette.textSecondary
    val TextMuted: Color get() = currentLegacyPalette.textMuted

    val GlowCyan: Color get() = currentLegacyPalette.glowCyan
    val GlowGreen: Color get() = currentLegacyPalette.glowGreen
}

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = GugiFont,
        fontSize = 26.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GugiFont,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FunnelDisplay,
        fontSize = 14.sp,
        letterSpacing = 1.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FunnelDisplay,
        fontSize = 13.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FunnelDisplay,
        fontSize = 11.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FunnelDisplay,
        fontSize = 10.sp,
    )
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    currentLegacyPalette = if (darkTheme) DarkLegacyPalette else LightLegacyPalette

    MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppLightColorScheme,
        typography = AppTypography,
        content = content
    )
}