package com.trishit.optimite.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trishit.optimite.domain.model.ProcessInfo
import com.trishit.optimite.ui.AppUiState
import com.trishit.optimite.ui.components.GlowCard
import com.trishit.optimite.ui.theme.AppColors

@Composable
fun ProcessesScreen(state: AppUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOP PROCESSES", style = MaterialTheme.typography.headlineLarge)
            Text(
                "${state.processes.size} tracked",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
        }

        // Header row
        GlowCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text("PROCESS", Modifier.weight(3f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("PID", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("MEM", Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("CPU%", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("STATUS", Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
            }
        }

        val maxMem = state.processes.maxOfOrNull { it.memoryMb }?.toFloat()?.coerceAtLeast(1f) ?: 1f

        state.processes.forEachIndexed { idx, proc ->
            ProcessRow(proc, idx, maxMem)
        }

        if (state.processes.isEmpty()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Fetching process list...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextMuted
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "* Process list refreshes every 3 seconds. Memory shown in MB.",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextMuted
        )
    }
}

@Composable
private fun ProcessRow(proc: ProcessInfo, index: Int, maxMem: Float) {
    val memPercent = (proc.memoryMb.toFloat() / maxMem) * 100f
    val rowColor = when {
        memPercent >= 80f -> AppColors.Danger
        memPercent >= 50f -> AppColors.Warning
        else -> AppColors.Border
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceVariant, RoundedCornerShape(6.dp))
            .border(1.dp, rowColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
    ) {
        // Memory fill indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(memPercent / 100f)
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Transparent,
                        1f to rowColor.copy(alpha = 0.06f)
                    ),
                    RoundedCornerShape(6.dp)
                )
        )
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                "${index + 1}.",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextMuted,
                modifier = Modifier.width(20.dp)
            )
            Text(
                proc.name.take(28),
                Modifier.weight(3f),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextPrimary,
                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "${proc.pid}",
                Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextMuted
            )
            Text(
                "${proc.memoryMb} MB",
                Modifier.weight(1.2f),
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    memPercent >= 80f -> AppColors.Danger
                    memPercent >= 50f -> AppColors.Warning
                    else -> AppColors.Primary
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                "${"%.1f".format(proc.cpuPercent)}%",
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
            Text(
                proc.status.take(8),
                Modifier.weight(1.2f),
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.Secondary
            )
        }
    }
}