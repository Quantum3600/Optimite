package com.trishit.optimite.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.components.SpringBouncy
import com.trishit.optimite.ui.components.SpringSnappy
import com.trishit.optimite.ui.screens.DashboardScreen
import com.trishit.optimite.ui.screens.MemoryScreen
import com.trishit.optimite.ui.screens.ProcessesScreen
import com.trishit.optimite.ui.screens.StorageScreen
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.AppTheme
import com.trishit.optimite.ui.theme.EaseInOutSine
import com.trishit.optimite.ui.theme.EaseOutCubic

@Composable
fun App(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()

    AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
            Row(Modifier.fillMaxSize()) {
                SideNav(
                    selectedTab = state.selectedTab,
                    onTabSelected = viewModel::selectTab
                )
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
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
                            AppTab.DASHBOARD -> DashboardScreen(state, viewModel::optimize, viewModel::cleanTempFiles)
                            AppTab.MEMORY    -> MemoryScreen(state, viewModel::optimize)
                            AppTab.STORAGE   -> StorageScreen(state, viewModel::cleanTempFiles)
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
                            .background(AppColors.Danger.copy(0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, AppColors.Danger.copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.dismissError() }
                            .padding(12.dp, 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠", color = AppColors.Danger, fontSize = 14.sp)
                        Text(err, style = MaterialTheme.typography.bodySmall, color = AppColors.Danger)
                        Text("✕", color = AppColors.Danger.copy(0.6f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SideNav(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "nav_glow")
    val logoGlow by infiniteTransition.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logo_glow"
    )

    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .drawBehind {
                // Nav background gradient
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color(0xFF0D1117),
                        1f to Color(0xFF0A0D12)
                    )
                )
                // Right border
                drawLine(
                    color = AppColors.Border,
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                // Active tab accent stripe on right edge
            }
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Animated logo
        Box(
            modifier = Modifier
                .size(42.dp)
                .drawBehind {
                    drawCircle(AppColors.Primary.copy(logoGlow * 0.15f), radius = size.minDimension)
                }
                .background(AppColors.Primary.copy(0.12f), RoundedCornerShape(12.dp))
                .border(1.dp, AppColors.Primary.copy(logoGlow * 0.6f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("⬡", fontSize = 20.sp, color = AppColors.Primary.copy(logoGlow))
        }

        Spacer(Modifier.height(24.dp))

        AppTab.entries.forEach { tab ->
            NavItem(tab = tab, isSelected = tab == selectedTab, onClick = { onTabSelected(tab) })
        }
    }
}

@Composable
private fun NavItem(tab: AppTab, isSelected: Boolean, onClick: () -> Unit) {
    // M3 Expressive: spring-driven scale + color on selection
    val bgAlpha by animateFloatAsState(if (isSelected) 0.18f else 0f, SpringSnappy, label = "ni_bg")
    val iconColor by animateColorAsState(
        if (isSelected) AppColors.Primary else AppColors.TextMuted, tween(250), label = "ni_col"
    )
    val scale by animateFloatAsState(if (isSelected) 1.06f else 1f, SpringBouncy, label = "ni_scale")
    val borderAlpha by animateFloatAsState(if (isSelected) 0.45f else 0f, tween(250), label = "ni_bor")
    val textAlpha by animateFloatAsState(if (isSelected) 1f else 0.45f, tween(200), label = "ni_ta")

    // Active indicator stripe (left side of card)
    Box(modifier = Modifier.width(56.dp).height(52.dp)) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .height(28.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            0f to AppColors.Primary.copy(0f),
                            0.5f to AppColors.Primary,
                            1f to AppColors.Primary.copy(0f)
                        ),
                        shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 6.dp)
                .scale(scale)
                .background(AppColors.Primary.copy(bgAlpha), RoundedCornerShape(10.dp))
                .border(1.dp, AppColors.Primary.copy(borderAlpha), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tab.icon, fontSize = 18.sp, color = iconColor)
            Text(
                tab.label.take(4).uppercase(),
                fontSize = 7.sp,
                color = iconColor.copy(textAlpha),
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}