package com.trishit.optimite.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.screens.DashboardScreen
import com.trishit.optimite.ui.screens.MemoryScreen
import com.trishit.optimite.ui.screens.ProcessesScreen
import com.trishit.optimite.ui.screens.StorageScreen
import com.trishit.optimite.ui.theme.AppTheme
import com.trishit.optimite.ui.theme.EaseOutCubic
import kotlinx.coroutines.launch
import optimite.composeapp.generated.resources.Res
import optimite.composeapp.generated.resources.left_panel_close
import optimite.composeapp.generated.resources.left_panel_open
import org.jetbrains.compose.resources.painterResource

@Composable
fun App(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()

    AppContent(
        state = state,
        onTabSelected = viewModel::selectTab,
        onOptimize = viewModel::optimize,
        onCleanTempFiles = viewModel::cleanTempFiles,
        onDismissError = viewModel::dismissError
    )
}

@Composable
fun AppContent(
    state: AppUiState,
    onTabSelected: (AppTab) -> Unit,
    onOptimize: () -> Unit,
    onCleanTempFiles: () -> Unit,
    onDismissError: () -> Unit
) {
    AppTheme {
        val colors = MaterialTheme.colorScheme

        Box(modifier = Modifier.fillMaxSize().background(colors.surface)) {
            Row(Modifier.fillMaxSize()) {
                SideNav(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected
                )
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 8.dp, end = 8.dp, bottom = 8.dp)
                    .background(colors.background, shape = RoundedCornerShape(16.dp))
                ) {
                    // M3 Expressive slide + fade transitions between screens
                    AnimatedContent(
                        targetState = state.selectedTab,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally(
                                spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                            ) { (it * direction * 0.08f).toInt() } + fadeIn(tween(220)))
                                .togetherWith(
                                    slideOutHorizontally(tween(160)) { (it * direction * -0.06f).toInt() } + fadeOut(tween(160))
                                )
                        },
                        label = "screen_nav"
                    ) { tab ->
                        when (tab) {
                            AppTab.DASHBOARD -> DashboardScreen(state, onOptimize, onCleanTempFiles)
                            AppTab.MEMORY    -> MemoryScreen(state, onOptimize)
                            AppTab.STORAGE   -> StorageScreen(state, onCleanTempFiles)
                            AppTab.PROCESSES -> ProcessesScreen(state)
                        }
                    }
                }
            }

            // Error snackbar — M3 slide-up
            AnimatedVisibility(
                visible = state.error != null,
                enter = slideInVertically(tween(320, easing = EaseOutCubic)) { it } + fadeIn(tween(220)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)
            ) {
                state.error?.let { err ->
                    Row(
                        modifier = Modifier
                            .background(colors.errorContainer, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.error.copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable { onDismissError() }
                            .padding(12.dp, 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠", color = colors.onErrorContainer, fontSize = 14.sp)
                        Text(err, style = MaterialTheme.typography.bodySmall, color = colors.onErrorContainer)
                        Text("✕", color = colors.onErrorContainer.copy(0.7f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SideNav(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val railState = rememberWideNavigationRailState(initialValue = WideNavigationRailValue.Collapsed)
    val scope = rememberCoroutineScope()
    val expanded = railState.currentValue == WideNavigationRailValue.Expanded

    WideNavigationRail(
        modifier = Modifier.fillMaxHeight(),
        arrangement = Arrangement.spacedBy(24.dp),
        state = railState,
        header = {
            Column(
                modifier = Modifier
                    .absoluteOffset(y = (-24).dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toggle icon button at the very top
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .align(Alignment.Start)
                        .clickable {
                            scope.launch {
                                if (expanded) railState.collapse() else railState.expand()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (expanded) Res.drawable.left_panel_close else Res.drawable.left_panel_open),
                        contentDescription = "Menu",
                        modifier = Modifier.size(24.dp),
                        tint = colors.onSurface,
                    )
                }
            }
        }
    ) {
        AppTab.entries.forEach { tab ->
            WideNavigationRailItem(
                selected = tab == selectedTab,
                modifier = Modifier,
                onClick = { onTabSelected(tab) },
                icon = {
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        Icon(tab.icon, contentDescription = null)
                    }
                },
                label = { Text(tab.label, style = MaterialTheme.typography.titleMedium.copy(fontSize = if(expanded) 16.sp else 12.sp), color = colors.onSurface) },
                railExpanded = expanded
            )
        }
    }
}

@Composable
@Preview
fun AppMock() {
    AppContent(
        state = AppUiState(),
        onTabSelected = {},
        onOptimize = {},
        onCleanTempFiles = {},
        onDismissError = {}
    )
}
