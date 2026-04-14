package com.trishit.optimite.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Minimize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.AppTheme
import com.trishit.optimite.ui.theme.GugiFont
import optimite.composeapp.generated.resources.Optimite
import optimite.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource

/**
 * Custom draggable title bar that mimics Mica dark aesthetic.
 * Used on macOS/Linux or as overlay when system title bar isn't dark enough.
 *
 * Pass onDrag callback to move the window via WindowState.position.
 */
@Composable
fun CustomTitleBar(
    title: String = "OPTIMITE",
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {},
    dragModifier: Modifier = Modifier,
    showSystemButtons: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .height(40.dp)
            .then(dragModifier)
    ) {

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.Optimite),
                contentDescription = "Logo",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                fontFamily = GugiFont,
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
            )
        }

        // Window controls (right side)
        if (showSystemButtons) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WinButton(color = Color(0xFFFF5F57), onClick = onClose, symbol = Icons.Rounded.Close)
                WinButton(color = Color(0xFF28C840), onClick = onMaximize, symbol = Icons.Outlined.Fullscreen)
                WinButton(color = Color(0xFFFFBD2E), onClick = onMinimize, symbol = Icons.Rounded.Minimize)
            }
        }
    }
}

@Composable
private fun WinButton(color: Color, onClick: () -> Unit, symbol: ImageVector) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) color else color.copy(0.5f),
        tween(150), label = "btn_bg"
    )

    Box(
        modifier = Modifier
            .size(15.dp)
            .background(bg, CircleShape)
            .hoverable(source)
            .clickable(source, null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (hovered) {
            Icon(
                imageVector = symbol,
                contentDescription = null,
                tint = Color.Black
            )
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
    content: @Composable BoxScope.() -> Unit
) {

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(24.dp, RoundedCornerShape(cornerRadius), ambientColor = AppColors.Primary.copy(0.08f))
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.background),
            content = content
        )
    }
}