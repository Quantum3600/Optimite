package com.trishit.optimite.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trishit.optimite.domain.model.ProcessInfo
import com.trishit.optimite.ui.AppUiState
import com.trishit.optimite.ui.components.StaggerCard
import com.trishit.optimite.ui.theme.AppColors

@Composable
fun ProcessesScreen(state: AppUiState) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TOP PROCESSES", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${state.processes.size} tracked",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
        }

        // Header row
        StaggerCard(entered = entered, delayMs = 40, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text("PROCESS", Modifier.weight(3f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("PID", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("MEM", Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("CPU%", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                Text("STATUS", Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
            }
        }

        val maxMem = state.processes.maxOfOrNull { it.memoryMb }?.toFloat()?.coerceAtLeast(1f) ?: 1f

        val lastProcessIndex = state.processes.lastIndex
        state.processes.forEachIndexed { idx, proc ->
            ProcessRow(
                proc = proc,
                index = idx,
                maxMem = maxMem,
                isFirst = idx == 0,
                isLast = idx == lastProcessIndex
            )
        }

        if (state.processes.isEmpty()) {
            StaggerCard(entered = entered, delayMs = 80, modifier = Modifier.fillMaxWidth()) {
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
private fun ProcessRow(
    proc: ProcessInfo,
    index: Int,
    maxMem: Float,
    isFirst: Boolean,
    isLast: Boolean
) {
    val memPercent = (proc.memoryMb.toFloat() / maxMem) * 100f
    val rowColor = when {
        memPercent >= 80f -> AppColors.Danger
        memPercent >= 50f -> AppColors.Warning
        else -> AppColors.Border
    }
    val rowShape = RoundedCornerShape(
        topStart = if (isFirst) 20.dp else 4.dp,
        topEnd = if (isFirst) 20.dp else 4.dp,
        bottomStart = if (isLast) 20.dp else 4.dp,
        bottomEnd = if (isLast) 20.dp else 4.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceVariant, rowShape)
    ) {
        // Memory fill indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(memPercent / 100f)
                .background(
                    rowColor.copy(alpha = 0.2f),
                    rowShape
                )
        )
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
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