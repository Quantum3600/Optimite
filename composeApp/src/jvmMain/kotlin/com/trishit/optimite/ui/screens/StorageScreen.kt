package com.trishit.optimite.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import com.trishit.optimite.domain.model.DriveInfo
import com.trishit.optimite.domain.model.DriveType
import com.trishit.optimite.ui.AppUiState
import com.trishit.optimite.ui.components.ActionButton
import com.trishit.optimite.ui.components.SectionLabel
import com.trishit.optimite.ui.components.SpringSnappy
import com.trishit.optimite.ui.components.StaggerCard
import com.trishit.optimite.ui.components.StatValue
import com.trishit.optimite.ui.components.UsageBar
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.FunnelDisplay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StorageScreen(state: AppUiState, onClean: () -> Unit) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    val scrollState = rememberScrollState()
    val hideFabAtBottom by remember {
        derivedStateOf { scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("STORAGE ANALYSIS", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)

            val drives = state.storageInfo?.drives ?: emptyList()

            if (drives.isEmpty()) {
                StaggerCard(entered = entered, delayMs = 40, modifier = Modifier.fillMaxWidth()) {
                    Text("Scanning storage...", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
                }
            } else {
                // Summary
                val totalGb = drives.sumOf { it.totalSpaceGb }
                val usedGb = drives.sumOf { it.usedSpaceGb }
                val freeGb = drives.sumOf { it.usableSpaceGb }

                StaggerCard(entered = entered, delayMs = 70, modifier = Modifier.fillMaxWidth()) {
                    SectionLabel("All Drives Summary")
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatValue("%.1f".format(totalGb), "Total GB", AppColors.TextPrimary)
                        StatValue("%.1f".format(usedGb), "Used GB", AppColors.Danger)
                        StatValue("%.1f".format(freeGb), "Free GB", AppColors.Secondary)
                    }
                    Spacer(Modifier.height(12.dp))
                    val overallPct = if (totalGb > 0) (usedGb / totalGb * 100).toFloat() else 0f
                    UsageBar(percent = overallPct, color = AppColors.Secondary, height = 8.dp)
                }

                drives.forEachIndexed { index, drive ->
                    DriveCard(drive = drive, entered = entered, delayMs = 100 + (index * 30))
                }
            }

            StaggerCard(entered = entered, delayMs = 220, modifier = Modifier.fillMaxWidth()) {
                SectionLabel("Storage Actions")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cleans temporary files older than 24 hours from system temp directories.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextMuted
                )
                Spacer(Modifier.height(12.dp))
                ActionButton(
                    icon = Icons.Rounded.DeleteForever,
                    text = "Clean Temp Files",
                    onClick = onClean,
                    isLoading = state.isCleaning,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Preserve space so the overlaid FAB does not hide the last row content.
            Spacer(Modifier.height(88.dp))
        }

        AnimatedVisibility(
            visible = !hideFabAtBottom,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = onClean,
                shape = FloatingActionButtonDefaults.largeExtendedFabShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = "Clean Temp Files"
                )
                Spacer(Modifier.width(16.dp))
                Text("Clear", fontFamily =  FunnelDisplay)
            }
        }
    }
}

@Composable
private fun DriveCard(drive: DriveInfo, entered: Boolean, delayMs: Int) {
    val color = when {
        drive.usagePercent >= 90f -> AppColors.Danger
        drive.usagePercent >= 70f -> AppColors.Warning
        else -> AppColors.Secondary
    }

    StaggerCard(entered = entered, delayMs = delayMs, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (drive.type) {
                    DriveType.SYSTEM -> "⬛"
                    DriveType.REMOVABLE -> "◼"
                    DriveType.NETWORK -> "◈"
                    DriveType.UNKNOWN -> "▣"
                }
                Text(icon, fontSize = 16.sp)
                Column {
                    Text(
                        drive.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        drive.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextMuted
                    )
                }
            }
            Text(
                "${"%.1f".format(drive.usagePercent)}%",
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        UsageBar(percent = drive.usagePercent, color = color)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${"%.1f".format(drive.usedSpaceGb)} GB used",
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
            )
            Text(
                "${"%.1f".format(drive.usableSpaceGb)} GB free of ${"%.1f".format(drive.totalSpaceGb)} GB",
                style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted
            )
        }
    }
}
