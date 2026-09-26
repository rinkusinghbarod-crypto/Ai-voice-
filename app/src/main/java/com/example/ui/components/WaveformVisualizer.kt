package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.SaddleBronze
import com.example.ui.theme.WaveformInactive
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 36
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(3f)
        val barSpacing = barWidth * 0.5f
        val startX = (width - (barCount * (barWidth + barSpacing) - barSpacing)) / 2f

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            val barX = startX + i * (barWidth + barSpacing)

            // Calculate height variation simulating speech audio spectrum
            val baseWave = sin(normalizedIndex * 3.1415f)
            val animatedHeightFraction = if (isPlaying) {
                val wave1 = (sin(normalizedIndex * 8f + phase) + 1f) * 0.25f
                val wave2 = (sin(normalizedIndex * 14f - phase * 1.3f) + 1f) * 0.20f
                (baseWave * 0.5f + wave1 + wave2).coerceIn(0.15f, 0.95f)
            } else {
                (baseWave * 0.6f + 0.15f).coerceIn(0.12f, 0.75f)
            }

            val barHeight = height * animatedHeightFraction
            val barTop = (height - barHeight) / 2f

            val isPassed = normalizedIndex <= progress
            val brush = if (isPassed) {
                Brush.verticalGradient(
                    colors = listOf(AmberGoldBright, AmberGold, SaddleBronze),
                    startY = barTop,
                    endY = barTop + barHeight
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(WaveformInactive.copy(alpha = 0.8f), WaveformInactive.copy(alpha = 0.4f)),
                    startY = barTop,
                    endY = barTop + barHeight
                )
            }

            drawRoundRect(
                brush = brush,
                topLeft = Offset(barX, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
