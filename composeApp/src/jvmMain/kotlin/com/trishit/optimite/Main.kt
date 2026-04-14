package com.trishit.optimite

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jna.Platform
import com.trishit.optimite.data.repository.JvmMemoryRepository
import com.trishit.optimite.data.repository.JvmProcessRepository
import com.trishit.optimite.data.repository.JvmStorageRepository
import com.trishit.optimite.domain.usecase.CleanStorageUseCase
import com.trishit.optimite.domain.usecase.GetTopProcessesUseCase
import com.trishit.optimite.domain.usecase.ObserveSystemStatsUseCase
import com.trishit.optimite.domain.usecase.OptimizeMemoryUseCase
import com.trishit.optimite.platform.WindowEffects
import com.trishit.optimite.ui.AppViewModel

fun main() = application {
    val memRepo = remember { JvmMemoryRepository() }
    val storRepo = remember { JvmStorageRepository() }
    val procRepo = remember { JvmProcessRepository() }

    val viewModel = remember {
        AppViewModel(
            observeSystem = ObserveSystemStatsUseCase(memRepo, storRepo),
            optimizeMemory = OptimizeMemoryUseCase(memRepo),
            getProcesses = GetTopProcessesUseCase(procRepo),
            cleanStorage = CleanStorageUseCase(storRepo)
        )
    }

    val windowState = rememberWindowState(
        size = DpSize(700.dp, 740.dp),
        position = WindowPosition.PlatformDefault
    )
    val useTransparentWindow = !Platform.isWindows()

    // Use undecorated so we can apply full custom chrome + rounded corners
    Window(
        onCloseRequest = {
            viewModel.dispose()
            exitApplication()
        },
        state = windowState,
        title = "Optimite",
        undecorated = true,          // Remove OS title bar entirely
        // Transparent undecorated windows + DWM backdrop can hide Compose content on Windows.
        transparent = useTransparentWindow,
        resizable = true,
        alwaysOnTop = false,
    ) {
        // Apply Windows Mica/dark DWM effects after window is shown
        LaunchedEffect(Unit) {
            // We removed window.addNotify() as it can cause JVM crashes in Compose Desktop.
            // WindowEffects uses reflection with --add-opens to access the peer.
            WindowEffects.applyDarkMica(window)
        }

        // The actual app UI — provides its own custom title bar + rounded surface
        AppShell(
            viewModel = viewModel,
            windowState = windowState,
            onMinimize = { windowState.isMinimized = true },
            onMaximize = {
                windowState.placement = if (windowState.placement == WindowPlacement.Maximized)
                    WindowPlacement.Floating else WindowPlacement.Maximized
            },
            onClose = {
                viewModel.dispose()
                exitApplication()
            }
        )
    }
}