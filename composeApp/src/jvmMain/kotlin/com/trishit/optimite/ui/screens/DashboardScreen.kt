package com.trishit.optimite.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.AppUiState
import com.trishit.optimite.ui.components.ActionButton
import com.trishit.optimite.ui.components.AnimatedCounter
import com.trishit.optimite.ui.components.CircularGauge
import com.trishit.optimite.ui.components.GlowCard
import com.trishit.optimite.ui.components.MiniSparkline
import com.trishit.optimite.ui.components.SectionLabel
import com.trishit.optimite.ui.components.StatusChip
import com.trishit.optimite.ui.components.UsageBar
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.EaseOutCubic

@Composable
fun DashboardScreen(state: AppUiState, onOptimize: () -> Unit, onClean: () -> Unit) {
    // Staggered entrance: each card slides in with delay
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(300)) + slideInVertically(tween(350)) { -20 }
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("SYSTEM OPTIMIZER", style = MaterialTheme.typography.headlineLarge, color = AppColors.TextPrimary)
                    Text("Real-time performance monitor", style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
                }
                val mem = state.memoryInfo
                val usage = mem?.usagePercent ?: 0f
                val (txt, col) = when {
                    usage >= 90f -> "CRITICAL" to AppColors.Danger
                    usage >= 70f -> "MODERATE" to AppColors.Warning
                    else -> "HEALTHY" to AppColors.Secondary
                }
                StatusChip(txt, col)
            }
        }

        // ── Gauges ───────────────────────────────────────────────────────────────
        StaggerCard(entered, delayMs = 60) {
            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Primary) {
                SectionLabel("System Overview")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    val mem = state.memoryInfo
                    CircularGauge(
                        percent = mem?.usagePercent ?: 0f,
                        label = "Memory",
                        value = "${mem?.usagePercent?.toInt() ?: 0}%",
                        color = AppColors.Primary, size = 130.dp
                    )
                    CircularGauge(
                        percent = state.storageInfo?.drives?.firstOrNull()?.usagePercent ?: 0f,
                        label = "Storage",
                        value = "${state.storageInfo?.drives?.firstOrNull()?.usagePercent?.toInt() ?: 0}%",
                        color = AppColors.Secondary, size = 130.dp
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        AnimatedCounter(
                            value = mem?.heapUsedMb ?: 0L,
                            suffix = " MB",
                            color = AppColors.Warning,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp)
                        )
                        Text("JVM HEAP", style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted, fontSize = 9.sp)
                        Spacer(Modifier.height(10.dp))
                        AnimatedCounter(
                            value = mem?.gcCount ?: 0L,
                            color = AppColors.Secondary,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp)
                        )
                        Text("GC RUNS", style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted, fontSize = 9.sp)
                    }
                }
            }
        }

        // ── Memory bar card ───────────────────────────────────────────────────────
        StaggerCard(entered, delayMs = 120) {
            state.memoryInfo?.let { mem ->
                GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Primary) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SectionLabel("Memory")
                        Text(
                            "${mem.usedMemoryMb} / ${mem.totalMemoryMb} MB",
                            style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    UsageBar(percent = mem.usagePercent, color = AppColors.Primary)
                    Spacer(Modifier.height(12.dp))
                    if (state.memoryHistory.size > 2) {
                        SectionLabel("Usage history (last 60s)")
                        Spacer(Modifier.height(4.dp))
                        MiniSparkline(state.memoryHistory, Modifier.fillMaxWidth().height(44.dp), AppColors.Primary)
                        Spacer(Modifier.height(12.dp))
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
                        MemStat("Free", "${mem.freeMemoryMb}", "MB", AppColors.Secondary, Modifier.weight(1f))
                        MemStat("Max", "${mem.maxMemoryMb}", "MB", AppColors.Warning, Modifier.weight(1f))
                        MemStat("Non-Heap", "${mem.nonHeapUsedMb}", "MB", AppColors.Primary, Modifier.weight(1f))
                    }
                }
            }
        }

        // ── Storage summary ────────────────────────────────────────────────────────
        StaggerCard(entered, delayMs = 180) {
            state.storageInfo?.drives?.firstOrNull()?.let { drive ->
                GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = AppColors.Secondary) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        SectionLabel("Primary Storage")
                        Text(
                            "${"%.1f".format(drive.usedSpaceGb)} / ${"%.1f".format(drive.totalSpaceGb)} GB",
                            style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    UsageBar(percent = drive.usagePercent, color = AppColors.Secondary)
                    Spacer(Modifier.height(8.dp))
                    Text("${"%.1f".format(drive.usableSpaceGb)} GB available",
                        style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
                }
            }
        }

        // ── Actions ────────────────────────────────────────────────────────────────
        StaggerCard(entered, delayMs = 240) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                SectionLabel("Actions")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    ActionButton("⚡ Optimize Memory", onOptimize, Modifier.weight(1f),
                        isLoading = state.isOptimizing, color = AppColors.Primary)
                    ActionButton("🗑 Clean Temp Files", onClean, Modifier.weight(1f),
                        isLoading = state.isCleaning, color = AppColors.Secondary)
                }
            }
        }

        // ── Result toast ──────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.lastOptimization != null,
            enter = expandVertically(tween(350, easing = EaseOutCubic)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
        ) {
            state.lastOptimization?.let { result ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Secondary.copy(0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, AppColors.Secondary.copy(0.4f), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text("✓ ${result.message}", style = MaterialTheme.typography.bodyMedium, color = AppColors.Secondary)
                        if (result.freedMemoryMb > 0) {
                            Text("Freed ${result.freedMemoryMb} MB · GC runs: ${result.gcRunCount}",
                                style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggerCard(entered: Boolean, delayMs: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = entered,
        enter = fadeIn(tween(350, delayMillis = delayMs)) +
                slideInVertically(tween(400, delayMillis = delayMs, easing = EaseOutCubic)) { 24 }
    ) { content() }
}

@Composable
private fun MemStat(label: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = color.copy(0.6f), fontSize = 9.sp)
        }
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
    }
}