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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.domain.model.MemoryInfo
import com.trishit.optimite.ui.AppUiState
import com.trishit.optimite.ui.components.ActionButton
import com.trishit.optimite.ui.components.GlowCard
import com.trishit.optimite.ui.components.MiniSparkline
import com.trishit.optimite.ui.components.SectionLabel
import com.trishit.optimite.ui.components.StatValue
import com.trishit.optimite.ui.components.UsageBar
import com.trishit.optimite.ui.theme.AppColors

@Composable
fun MemoryScreen(state: AppUiState, onOptimize: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MEMORY ANALYSIS", style = MaterialTheme.typography.headlineLarge)

        val mem = state.memoryInfo

        // Big stat row
        GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Primary) {
            SectionLabel("Physical Memory")
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatValue("${mem?.totalMemoryMb ?: "--"}", "Total MB", AppColors.TextPrimary)
                StatValue("${mem?.usedMemoryMb ?: "--"}", "Used MB", AppColors.Danger)
                StatValue("${mem?.freeMemoryMb ?: "--"}", "Free MB", AppColors.Secondary)
            }
            Spacer(Modifier.height(16.dp))
            UsageBar(percent = mem?.usagePercent ?: 0f, height = 10.dp, color = AppColors.Primary)
            Spacer(Modifier.height(6.dp))
            Text(
                "${mem?.usagePercent?.let { "%.1f".format(it) } ?: "0.0"}% utilized",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
        }

        // JVM Heap detail
        GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Warning) {
            SectionLabel("JVM Heap")
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatValue("${mem?.heapUsedMb ?: "--"}", "Used MB", AppColors.Warning)
                StatValue("${mem?.heapMaxMb ?: "--"}", "Max MB", AppColors.TextPrimary)
                val heapPct = mem?.let { (it.heapUsedMb.toFloat() / it.heapMaxMb.coerceAtLeast(1L)) * 100 } ?: 0f
                StatValue("${"%.0f".format(heapPct)}%", "Heap Use", AppColors.Primary)
            }
            Spacer(Modifier.height(12.dp))
            val heapPct = mem?.let { (it.heapUsedMb.toFloat() / it.heapMaxMb.coerceAtLeast(1L)) * 100 } ?: 0f
            UsageBar(percent = heapPct, color = AppColors.Warning)
        }

        // Non-heap & GC
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlowCard(modifier = Modifier.weight(1f), glowColor = AppColors.Secondary) {
                SectionLabel("Non-Heap")
                Spacer(Modifier.height(8.dp))
                Text(
                    "${mem?.nonHeapUsedMb ?: "--"} MB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.Secondary,
                    fontWeight = FontWeight.Bold
                )
                Text("(Metaspace, Code Cache)", style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
            }
            GlowCard(modifier = Modifier.weight(1f)) {
                SectionLabel("GC Statistics")
                Spacer(Modifier.height(8.dp))
                GcStat("Collections", "${mem?.gcCount ?: 0}", AppColors.Primary)
                Spacer(Modifier.height(4.dp))
                GcStat("Total GC Time", "${mem?.gcTimeMs ?: 0} ms", AppColors.Warning)
            }
        }

        // Sparkline history
        if (state.memoryHistory.size > 2) {
            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Primary) {
                SectionLabel("Usage History — Last 60 Readings")
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${state.memoryHistory.minOrNull()?.let { "%.0f".format(it) } ?: 0}%",
                        style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
                    Text("Peak: ${state.memoryHistory.maxOrNull()?.let { "%.0f".format(it) } ?: 0}%",
                        style = MaterialTheme.typography.labelSmall, color = AppColors.Primary)
                }
                Spacer(Modifier.height(4.dp))
                MiniSparkline(
                    data = state.memoryHistory,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    color = AppColors.Primary
                )
            }
        }

        // Recommendations
        mem?.let { info ->
            val recs = buildRecommendations(info)
            if (recs.isNotEmpty()) {
                GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Warning) {
                    SectionLabel("Recommendations")
                    Spacer(Modifier.height(8.dp))
                    recs.forEach { rec ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("▸", color = AppColors.Warning, fontSize = 12.sp)
                            Text(rec, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary)
                        }
                    }
                }
            }
        }

        ActionButton(
            text = "⚡ Run Memory Optimizer",
            onClick = onOptimize,
            isLoading = state.isOptimizing,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GcStat(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
    }
}

private fun buildRecommendations(info: MemoryInfo): List<String> {
    val recs = mutableListOf<String>()
    if (info.usagePercent >= 85f) recs.add("Memory usage is critically high. Consider closing unused applications.")
    if (info.usagePercent >= 70f) recs.add("Consider running GC optimization to reclaim unused heap space.")
    val heapPct = (info.heapUsedMb.toFloat() / info.heapMaxMb.coerceAtLeast(1L)) * 100
    if (heapPct >= 80f) recs.add("JVM heap is nearly full. Increase -Xmx or optimize object allocation.")
    if (info.gcTimeMs > 5000) recs.add("Long GC pause times detected. Review large object allocation patterns.")
    if (info.freeMemoryMb < 512) recs.add("Low free memory. Close unused applications or add more RAM.")
    return recs
}
