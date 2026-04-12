package com.trishit.optimite.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.EaseInOutSine

/**
 * Custom draggable title bar that mimics Mica dark aesthetic.
 * Used on macOS/Linux or as overlay when system title bar isn't dark enough.
 *
 * Pass onDrag callback to move the window via WindowState.position.
 */
@Composable
fun CustomTitleBar(
    title: String = "MEMORY OPTIMIZER",
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {},
    dragModifier: Modifier = Modifier,
    showSystemButtons: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "title_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glow_a"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .drawBehind {
                // Subtle mica noise-like background
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color(0xFF0F1318),
                        1f to Color(0xFF0A0D12)
                    )
                )
                // Bottom border glow line
                drawLine(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.2f to AppColors.Primary.copy(glowAlpha * 0.5f),
                        0.8f to AppColors.Primary.copy(glowAlpha * 0.5f),
                        1f to Color.Transparent
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            }
            .then(dragModifier)
    ) {
        // Center: title + icon
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Animated logo hex
            val scale by infiniteTransition.animateFloat(
                1f, 1.08f,
                infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "logo_scale"
            )
            Text(
                "⬡",
                fontSize = 14.sp,
                color = AppColors.Primary.copy(glowAlpha),
                modifier = Modifier.scale(scale)
            )
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Window controls (right side)
        if (showSystemButtons) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WinButton(color = Color(0xFFFFBD2E), onClick = onMinimize, symbol = "−")
                WinButton(color = Color(0xFF28C840), onClick = onMaximize, symbol = "+")
                WinButton(color = Color(0xFFFF5F57), onClick = onClose, symbol = "×")
            }
        }
    }
}

@Composable
private fun WinButton(color: Color, onClick: () -> Unit, symbol: String) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) color else color.copy(0.5f),
        tween(150), label = "btn_bg"
    )

    Box(
        modifier = Modifier
            .size(13.dp)
            .background(bg, CircleShape)
            .hoverable(source)
            .clickable(source, null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (hovered) {
            Text(symbol, fontSize = 9.sp, color = Color.Black.copy(0.7f), lineHeight = 9.sp)
        }
    }
}

/**
 * Full-window rounded container that clips content to rounded corners.
 * Used when window is undecorated (no system chrome at all).
 */
@Composable
fun RoundedWindowSurface(
    cornerRadius: Dp = 12.dp,
    borderColor: Color = AppColors.Border,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(24.dp, RoundedCornerShape(cornerRadius), ambientColor = AppColors.Primary.copy(0.08f))
            .clip(RoundedCornerShape(cornerRadius))
            .background(AppColors.Background)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
        content = content
    )
}