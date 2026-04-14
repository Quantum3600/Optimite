package com.trishit.optimite

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.trishit.optimite.ui.App
import com.trishit.optimite.ui.AppViewModel
import com.trishit.optimite.ui.components.CustomTitleBar
import com.trishit.optimite.ui.components.RoundedWindowSurface
import com.trishit.optimite.ui.theme.AppColors

@Composable
fun AppShell(
    viewModel: AppViewModel,
    windowState: WindowState,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit
) {
    // Drag-to-move delta tracking
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                dragOffsetX = 0f
                dragOffsetY = 0f
            },
            onDrag = { change, dragAmount ->
                change.consume()
                if (windowState.placement == WindowPlacement.Floating) {
                    val current = windowState.position
                    windowState.position = WindowPosition(
                        x = current.x + dragAmount.x.dp / density,
                        y = current.y + dragAmount.y.dp / density
                    )
                }
            }
        )
    }

    RoundedWindowSurface(
        cornerRadius = if(windowState.placement == WindowPlacement.Maximized) 0.dp else 12.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            // Custom title bar — always shown since window is undecorated
            CustomTitleBar(
                title = "OPTIMITE",
                onMinimize = onMinimize,
                onMaximize = onMaximize,
                onClose = onClose,
                dragModifier = dragModifier,
                showSystemButtons = true
            )

            // Main app content fills rest of window
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                App(viewModel = viewModel)
            }
        }
    }
}