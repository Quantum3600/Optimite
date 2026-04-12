package com.trishit.optimite.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Custom easings (M3 Expressive motion vocabulary) ────────────────────────────
val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
val EaseOutCubic  = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
val EaseOutBack   = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

// ─── Color palette — dark industrial / cyber ─────────────────────────────────────
object AppColors {
    val Background     = Color(0xFF0A0D12)
    val Surface        = Color(0xFF0F1318)
    val SurfaceVariant = Color(0xFF141920)
    val Border         = Color(0xFF1E2733)

    val Primary    = Color(0xFF00D4FF)   // Cyan
    val PrimaryDim = Color(0xFF0096B4)
    val Secondary  = Color(0xFF00FF88)   // Green
    val Warning    = Color(0xFFFFB800)   // Amber
    val Danger     = Color(0xFFFF3D5A)   // Red

    val TextPrimary   = Color(0xFFE8EDF5)
    val TextSecondary = Color(0xFF6B7A90)
    val TextMuted     = Color(0xFF3A4455)

    val GlowCyan  = Color(0x3300D4FF)
    val GlowGreen = Color(0x3300FF88)
}

val AppDarkColorScheme = darkColorScheme(
    primary         = AppColors.Primary,
    secondary       = AppColors.Secondary,
    tertiary        = AppColors.Warning,
    error           = AppColors.Danger,
    background      = AppColors.Background,
    surface         = AppColors.Surface,
    surfaceVariant  = AppColors.SurfaceVariant,
    onBackground    = AppColors.TextPrimary,
    onSurface       = AppColors.TextPrimary,
    onPrimary       = AppColors.Background,
    outline         = AppColors.Border,
    outlineVariant  = AppColors.Border.copy(alpha = 0.5f)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = AppColors.TextPrimary
            ),
            headlineMedium = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AppColors.TextPrimary
            ),
            titleMedium = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                letterSpacing = 1.2.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = AppColors.TextPrimary
            ),
            bodySmall = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = AppColors.TextSecondary
            ),
            labelSmall = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = AppColors.TextMuted,
                letterSpacing = 1.2.sp
            )
        ),
        content = content
    )
}