package com.trishit.optimite.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.trishit.optimite.domain.model.DriveInfo
import com.trishit.optimite.domain.model.DriveType
import com.trishit.optimite.ui.AppUiState
import com.trishit.optimite.ui.components.ActionButton
import com.trishit.optimite.ui.components.GlowCard
import com.trishit.optimite.ui.components.SectionLabel
import com.trishit.optimite.ui.components.StatValue
import com.trishit.optimite.ui.components.UsageBar
import com.trishit.optimite.ui.theme.AppColors

@Composable
fun StorageScreen(state: AppUiState, onClean: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("STORAGE ANALYSIS", style = MaterialTheme.typography.headlineLarge)

        val drives = state.storageInfo?.drives ?: emptyList()

        if (drives.isEmpty()) {
            GlowCard(Modifier.fillMaxWidth()) {
                Text("Scanning storage...", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
            }
        } else {
            // Summary
            val totalGb = drives.sumOf { it.totalSpaceGb }
            val usedGb = drives.sumOf { it.usedSpaceGb }
            val freeGb = drives.sumOf { it.usableSpaceGb }

            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Secondary) {
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

            drives.forEach { drive ->
                DriveCard(drive)
            }
        }

        GlowCard(modifier = Modifier.fillMaxWidth()) {
            SectionLabel("Storage Actions")
            Spacer(Modifier.height(8.dp))
            Text(
                "Cleans temporary files older than 24 hours from system temp directories.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
            Spacer(Modifier.height(12.dp))
            ActionButton(
                text = "🗑 Clean Temp Files",
                onClick = onClean,
                isLoading = state.isCleaning,
                color = AppColors.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DriveCard(drive: DriveInfo) {
    val color = when {
        drive.usagePercent >= 90f -> AppColors.Danger
        drive.usagePercent >= 70f -> AppColors.Warning
        else -> AppColors.Secondary
    }

    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = color) {
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