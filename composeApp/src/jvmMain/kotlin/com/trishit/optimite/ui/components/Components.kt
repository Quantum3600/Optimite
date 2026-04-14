package com.trishit.optimite.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.EaseInOutSine
import org.jetbrains.compose.resources.stringResource

// ─── M3 Expressive spring specs ─────────────────────────────────────────────────
val SpringSnappy = spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
val SpringBouncy = spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
val SpringSmooth = spring<Float>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)

// ─── SectionLabel ────────────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String, color: Color = AppColors.TextMuted) {
    Text(text.uppercase(), style = MaterialTheme.typography.bodySmall, color = color)
}

// ─── StatValue with entrance spring ─────────────────────────────────────────────
@Composable
fun StatValue(value: String, label: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(if (visible) 1f else 0.6f, SpringBouncy, label = "stat_s")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

// ─── UsageBar with M3 Wavy Progress Indicator ───────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UsageBar(
    percent: Float,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    color: Color = AppColors.Primary,
    backgroundColor: Color = AppColors.Border,
    animated: Boolean = true
) {
    val animFraction by animateFloatAsState(
        (percent.coerceIn(0f, 100f)) / 100f, SpringSmooth, label = "bar_frac"
    )
    val displayFraction = if (animated) animFraction else percent / 100f
    val barColor = when {
        percent >= 90f -> AppColors.Danger; percent >= 70f -> AppColors.Warning; else -> color
    }
    val animBarColor by animateColorAsState(barColor, tween(400), label = "bar_col")

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { (height / 2f).toPx() }
    val amplitudePx = with(density) { (height / 3f).toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "wave_inf")
    val wavePhase by infiniteTransition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart), label = "wave_phase"
    )

    LinearWavyProgressIndicator(
        progress = { displayFraction },
        modifier = modifier.fillMaxWidth().height(height),
        color = animBarColor,
        trackColor = backgroundColor,
        stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        wavelength = 30.dp,
        waveSpeed = 8.dp,
        amplitude = { _ -> amplitudePx * (0.5f + 0.5f * wavePhase) }
    )
}

@Composable
fun StaggerCard(
    entered: Boolean,
    delayMs: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = entered,
        enter = fadeIn(tween(300, delayMillis = delayMs)) +
                slideInVertically(tween(360, delayMillis = delayMs)) { 20 }
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceDim)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
        }
    }
}

// ─── CircularGauge with M3 Wavy Progress Indicator ──────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircularGauge(
    percent: Float,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Primary,
    size: Dp = 120.dp
) {
    val animPercent by animateFloatAsState(
        percent.coerceIn(0f, 100f),
        spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow), label = "gauge_pct"
    )
    val gaugeColor = when {
        percent >= 90f -> AppColors.Danger; percent >= 70f -> AppColors.Warning; else -> color
    }
    val animGaugeColor by animateColorAsState(gaugeColor, tween(500), label = "gauge_col")

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 5.dp.toPx() }
    val amplitudePx = with(density) { (if (percent >= 90f) 6.dp else 4.dp).toPx() }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { animPercent / 100f },
            modifier = Modifier.fillMaxSize(),
            color = animGaugeColor,
            trackColor = AppColors.Border,
            stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            gapSize = 8.dp,
            wavelength = 32.dp,
            amplitude = { _ -> amplitudePx }
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, color = animGaugeColor, fontSize = 18.sp)
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

// ─── Sparkline ────────────────────────────────────────────────────────────────────
@Composable
fun MiniSparkline(
    data: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Primary
) {
    if (data.size < 2) return
    Canvas(modifier = modifier) {
        val w = size.width;
        val h = size.height
        val step = w / (data.size - 1).coerceAtLeast(1)
        val max = data.max().coerceAtLeast(1f);
        val min = data.min()
        val path = Path()
        data.forEachIndexed { i, v ->
            val x = i * step
            val norm = if (max > min) (v - min) / (max - min) else 0.5f
            val y = h - norm * h * 0.85f - h * 0.07f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val fillPath = Path().apply {
            addPath(path); lineTo((data.size - 1) * step, h); lineTo(
            0f,
            h
        ); close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(0f to color.copy(0.35f), 1f to Color.Transparent)
        )
        drawPath(
            path,
            color = color,
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        val lx = (data.size - 1) * step
        val ln = if (max > min) (data.last() - min) / (max - min) else 0.5f
        val ly = h - ln * h * 0.85f - h * 0.07f
        drawCircle(color.copy(0.4f), 5.dp.toPx(), Offset(lx, ly))
        drawCircle(color, 2.5.dp.toPx(), Offset(lx, ly))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(48.dp),
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    LoadingIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                    )
                } else {
                    Icon(icon, null)
                }
                Text(
                    if (isLoading) "PROCESSING..." else text.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

// ─── ConnectedButtonGroup ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedButtonGroup(
    options: List<ActionOption>,
    modifier: Modifier = Modifier
) {
    ButtonGroup(
        overflowIndicator = {_ -> },
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        expandedRatio = ButtonGroupDefaults.ExpandedRatio,
        horizontalArrangement = ButtonGroupDefaults.HorizontalArrangement,
        content = {
            options.forEach { opt ->
                clickableItem(
                    enabled = !opt.isLoading,
                    onClick = opt.onClick,
                    icon = {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (opt.isLoading) {
                                    LoadingIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons
                                    )
                                } else {
                                    Icon(opt.icon, null)
                                }
                                Text(
                                    if (opt.isLoading) "WAIT..." else opt.text.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if(opt.isLoading) Color.White else opt.color,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    label = "",
                    weight = 1f
                )
            }
        }
    )
}

data class ActionOption(
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit,
    val isLoading: Boolean = false,
    val color: Color = AppColors.Primary
)

// ─── StatusChip ──────────────────────────────────────────────────────────────────
@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "chip_inf")
    val pulse by infiniteTransition.animateFloat(
        0.5f,
        1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "chip_p"
    )
    AssistChip(
        onClick = {},
        modifier = modifier,
        label = {
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        },
        leadingIcon = {
            Canvas(Modifier.size(8.dp)) {
                drawCircle(color.copy(pulse * 0.5f), size.minDimension)
                drawCircle(color, size.minDimension * 0.45f)
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color,
            leadingIconContentColor = color
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.35f)
        )
    )
}

// ─── AnimatedCounter ─────────────────────────────────────────────────────────────
@Composable
fun AnimatedCounter(
    value: Long, suffix: String = "", prefix: String = "",
    color: Color = AppColors.TextPrimary,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    val animValue by animateFloatAsState(
        value.toFloat(),
        spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "counter"
    )
    Text(
        "$prefix${animValue.toLong()}$suffix",
        style = style,
        color = color,
        fontWeight = FontWeight.Bold
    )
}
