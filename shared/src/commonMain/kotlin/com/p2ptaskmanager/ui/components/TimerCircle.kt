package com.p2ptaskmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.ui.theme.CrimsonError
import com.p2ptaskmanager.ui.theme.GoldAccent

@Composable
fun TimerCircle(
    elapsedMs: Long,
    estimatedMinutes: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 3.dp
) {
    val elapsedSeconds = elapsedMs / 1000L
    val estimatedSeconds = (estimatedMinutes ?: 0) * 60L

    val fraction = if (estimatedSeconds > 0)
        (elapsedSeconds.toFloat() / estimatedSeconds.toFloat()).coerceAtLeast(0f)
    else 0f

    val isOverrun = fraction > 1f
    val arcSweep = if (estimatedSeconds > 0)
        (fraction * 360f).coerceAtMost(360f)
    else 360f * ((elapsedSeconds % 60) / 60f)

    val arcColor = if (isOverrun) CrimsonError else GoldAccent
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    val mm = (elapsedSeconds / 60).toString().padStart(2, '0')
    val ss = (elapsedSeconds % 60).toString().padStart(2, '0')
    val elapsed = "$mm:$ss"

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            )
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = arcSweep,
                useCenter = false,
                style = stroke,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = elapsed,
                fontSize = (size.value * 0.18f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (estimatedMinutes != null) {
                Text(
                    text = "/ ${estimatedMinutes}:00",
                    fontSize = (size.value * 0.1f).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
