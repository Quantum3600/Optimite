package com.trishit.optimite.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.optimite.ui.theme.AppColors
import com.trishit.optimite.ui.theme.EaseInOutSine
import kotlin.math.cos
import kotlin.math.sin

// ─── M3 Expressive spring specs ─────────────────────────────────────────────────
val SpringSnappy  = spring<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
val SpringBouncy: AnimationSpec<Float> = spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
val SpringSmooth  = spring<Float>(dampingRatio = 0.85f, stiffness = Spring.StiffnessMedium)

// ─── GlowCard ────────────────────────────────────────────────────────────────────
@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = AppColors.Primary,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val glowPulse by infiniteTransition.animateFloat(
        0.15f, 0.38f,
        infiniteRepeatable(tween(2800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "card_glow_a"
    )
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = if (elevated) 12.dp else 0.dp,
                    shape = RoundedCornerShape(10.dp),
                    ambientColor = glowColor.copy(0.12f),
                    spotColor = glowColor.copy(0.08f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        0f to AppColors.SurfaceVariant,
                        1f to AppColors.Surface
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .drawBehind {
                    drawRoundRect(
                        color = glowColor.copy(glowPulse),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.3f to Color.White.copy(0.05f),
                            0.7f to Color.White.copy(0.05f),
                            1f to Color.Transparent
                        ),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )
                }
                .padding(16.dp),
            content = content
        )
    }
}

// ─── SectionLabel ────────────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String, color: Color = AppColors.TextMuted) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = color)
}

// ─── StatValue with entrance spring ─────────────────────────────────────────────
@Composable
fun StatValue(value: String, label: String, color: Color = AppColors.TextPrimary) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(if (visible) 1f else 0.6f, SpringBouncy, label = "stat_s")
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(scale)) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted)
    }
}

// ─── UsageBar with spring + shimmer ──────────────────────────────────────────────
@Composable
fun UsageBar(
    percent: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color = AppColors.Primary,
    backgroundColor: Color = AppColors.Border,
    animated: Boolean = true
) {
    val animFraction by animateFloatAsState(
        (percent.coerceIn(0f, 100f)) / 100f, SpringSmooth, label = "bar_frac"
    )
    val displayFraction = if (animated) animFraction else percent / 100f
    val barColor = when { percent >= 90f -> AppColors.Danger; percent >= 70f -> AppColors.Warning; else -> color }
    val animBarColor by animateColorAsState(barColor, tween(400), label = "bar_col")

    Canvas(modifier = modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50))) {
        drawRect(backgroundColor)
        if (displayFraction > 0.01f) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to animBarColor.copy(0.8f), 0.6f to animBarColor, 1f to animBarColor.copy(0.9f),
                    endX = size.width * displayFraction
                ),
                size = Size(size.width * displayFraction, size.height)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.7f to Color.White.copy(0.12f),
                    1f to Color.Transparent,
                    startX = (size.width * displayFraction - size.height * 3).coerceAtLeast(0f),
                    endX = size.width * displayFraction
                ),
                size = Size(size.width * displayFraction, size.height)
            )
            drawCircle(animBarColor.copy(0.6f), size.height * 1.5f, Offset(size.width * displayFraction, size.height / 2))
            drawCircle(animBarColor, size.height * 0.7f, Offset(size.width * displayFraction, size.height / 2))
        }
    }
}

// ─── CircularGauge with M3 spring + glow ─────────────────────────────────────────
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
    val gaugeColor = when { percent >= 90f -> AppColors.Danger; percent >= 70f -> AppColors.Warning; else -> color }
    val animGaugeColor by animateColorAsState(gaugeColor, tween(500), label = "gauge_col")

    val infiniteTransition = rememberInfiniteTransition(label = "gauge_inf")
    val ringPulse by infiniteTransition.animateFloat(
        0.8f, 1f,
        infiniteRepeatable(tween(if (percent >= 90f) 600 else 2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ring_s"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().scale(if (percent >= 90f) ringPulse else 1f)) {
            val sw = 8.dp.toPx()
            val r = this.size.minDimension / 2 - sw
            val c = Offset(this.size.width / 2, this.size.height / 2)
            val sa = 135f; val total = 270f

            // Outer glow track
            drawArc(animGaugeColor.copy(0.07f), sa, total * (animPercent / 100f), false,
                Offset(c.x - r - 4.dp.toPx(), c.y - r - 4.dp.toPx()),
                Size((r + 4.dp.toPx()) * 2, (r + 4.dp.toPx()) * 2),
                style = Stroke(sw + 8.dp.toPx(), cap = StrokeCap.Round))
            // Track
            drawArc(AppColors.Border, sa, total, false,
                Offset(c.x - r, c.y - r), Size(r * 2, r * 2),
                style = Stroke(sw, cap = StrokeCap.Round))
            // Active arc
            if (animPercent > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to animGaugeColor.copy(0.6f),
                        (animPercent / 100f * 0.75f).coerceIn(0f, 1f) to animGaugeColor,
                        center = c
                    ),
                    startAngle = sa, sweepAngle = total * (animPercent / 100f), useCenter = false,
                    topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2),
                    style = Stroke(sw, cap = StrokeCap.Round)
                )
            }
            // End dot
            if (animPercent > 2f) {
                val ea = Math.toRadians((sa + total * (animPercent / 100f)).toDouble())
                val dx = c.x + r * cos(ea).toFloat(); val dy = c.y + r * sin(ea).toFloat()
                drawCircle(animGaugeColor.copy(0.35f), sw * 1.2f, Offset(dx, dy))
                drawCircle(animGaugeColor, sw / 2.8f, Offset(dx, dy))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, color = animGaugeColor, fontSize = 18.sp)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AppColors.TextMuted, fontSize = 9.sp)
        }
    }
}

// ─── Sparkline ────────────────────────────────────────────────────────────────────
@Composable
fun MiniSparkline(data: List<Float>, modifier: Modifier = Modifier, color: Color = AppColors.Primary) {
    if (data.size < 2) return
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val step = w / (data.size - 1).coerceAtLeast(1)
        val max = data.max().coerceAtLeast(1f); val min = data.min()
        val path = Path()
        data.forEachIndexed { i, v ->
            val x = i * step
            val norm = if (max > min) (v - min) / (max - min) else 0.5f
            val y = h - norm * h * 0.85f - h * 0.07f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val fillPath = Path().apply { addPath(path); lineTo((data.size - 1) * step, h); lineTo(0f, h); close() }
        drawPath(fillPath, brush = Brush.verticalGradient(0f to color.copy(0.35f), 1f to Color.Transparent))
        drawPath(path, color = color, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        val lx = (data.size - 1) * step
        val ln = if (max > min) (data.last() - min) / (max - min) else 0.5f
        val ly = h - ln * h * 0.85f - h * 0.07f
        drawCircle(color.copy(0.4f), 5.dp.toPx(), Offset(lx, ly))
        drawCircle(color, 2.5.dp.toPx(), Offset(lx, ly))
    }
}

// ─── M3 Expressive ActionButton ──────────────────────────────────────────────────
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    color: Color = AppColors.Primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        when { isPressed -> 0.94f; isHovered -> 1.02f; else -> 1f }, SpringSnappy, label = "btn_s"
    )
    val bgAlpha by animateFloatAsState(
        when { isPressed -> 0.28f; isHovered -> 0.22f; isLoading -> 0.10f; else -> 0.13f }, tween(180), label = "btn_bg"
    )
    val borderAlpha by animateFloatAsState(
        when { isPressed -> 0.9f; isHovered -> 0.8f; else -> 0.5f }, tween(180), label = "btn_bo"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "btn_inf")
    val shimmer by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart), label = "shim")
    val pulseAlpha by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pa")
    val spinAngle by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "spin")

    Box(
        modifier = modifier.scale(scale)
            .hoverable(interactionSource)
            .clickable(interactionSource, indication = null, enabled = !isLoading, onClick = onClick)
            .drawBehind {
                val r = CornerRadius(8.dp.toPx())
                drawRoundRect(color.copy(bgAlpha), cornerRadius = r)
                drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(0.06f), 1f to Color.Transparent), cornerRadius = r)
                drawRoundRect(color.copy(borderAlpha), cornerRadius = r, style = Stroke(1.dp.toPx()))
                if (isLoading) {
                    val ss = size.width * (shimmer - 0.3f)
                    val se = size.width * shimmer
                    drawRoundRect(
                        brush = Brush.horizontalGradient(0f to Color.Transparent, 0.5f to color.copy(0.25f), 1f to Color.Transparent, startX = ss, endX = se),
                        cornerRadius = r
                    )
                }
            }
            .padding(horizontal = 20.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                Canvas(Modifier.size(12.dp).rotate(spinAngle)) {
                    drawArc(color.copy(pulseAlpha), 0f, 270f, false, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            Text(
                if (isLoading) "PROCESSING..." else text.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = color.copy(if (isLoading) pulseAlpha else 1f),
                fontSize = 12.sp, letterSpacing = 1.5.sp
            )
        }
    }
}

// ─── StatusChip ──────────────────────────────────────────────────────────────────
@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "chip_inf")
    val pulse by infiniteTransition.animateFloat(
        0.5f, 1f, infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse), label = "chip_p"
    )
    Row(
        modifier = modifier
            .background(color.copy(0.12f), RoundedCornerShape(50))
            .border(1.dp, color.copy(0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Canvas(Modifier.size(6.dp)) {
            drawCircle(color.copy(pulse * 0.5f), size.minDimension)
            drawCircle(color, size.minDimension * 0.45f)
        }
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

// ─── AnimatedCounter ─────────────────────────────────────────────────────────────
@Composable
fun AnimatedCounter(
    value: Long, suffix: String = "", prefix: String = "",
    color: Color = AppColors.TextPrimary,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    val animValue by animateFloatAsState(
        value.toFloat(), spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow), label = "counter"
    )
    Text("$prefix${animValue.toLong()}$suffix", style = style, color = color, fontWeight = FontWeight.Bold)
}