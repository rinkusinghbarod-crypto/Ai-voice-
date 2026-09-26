package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceControlsPanel(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    pitch: String,
    onPitchChange: (String) -> Unit,
    emotion: String,
    onEmotionChange: (String) -> Unit,
    pauseControl: String,
    onPauseControlChange: (String) -> Unit,
    intensity: String,
    onIntensityChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language selector
            ControlSection(title = "Language") {
                val languages = listOf("English (US)", "Hindi", "Hinglish")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    languages.forEach { lang ->
                        ControlChip(
                            label = lang,
                            isSelected = lang == language,
                            onClick = { onLanguageChange(lang) }
                        )
                    }
                }
            }

            // Emotion
            ControlSection(title = "Story Emotion") {
                val emotions = listOf("Calm", "Serious", "Emotional", "Dramatic")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    emotions.forEach { emo ->
                        ControlChip(
                            label = emo,
                            isSelected = emo == emotion,
                            onClick = { onEmotionChange(emo) }
                        )
                    }
                }
            }

            // Speed
            ControlSection(title = "Narration Speed") {
                val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    speeds.forEach { sp ->
                        val label = "${sp}x"
                        ControlChip(
                            label = label,
                            isSelected = sp == speed,
                            onClick = { onSpeedChange(sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Pitch
            ControlSection(title = "Pitch Timber") {
                val pitches = listOf("Low", "Normal", "High")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pitches.forEach { p ->
                        ControlChip(
                            label = p,
                            isSelected = p == pitch,
                            onClick = { onPitchChange(p) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Pause Control & Intensity in 2 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pause Rhythm",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val pauses = listOf("Short", "Natural", "Long")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        pauses.forEach { p ->
                            ControlChip(
                                label = p,
                                isSelected = p == pauseControl,
                                onClick = { onPauseControlChange(p) },
                                modifier = Modifier.weight(1f),
                                compact = true
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Intensity",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val intensities = listOf("Subtle", "Balanced", "Intense")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        intensities.forEach { i ->
                            ControlChip(
                                label = i,
                                isSelected = i == intensity,
                                onClick = { onIntensityChange(i) },
                                modifier = Modifier.weight(1f),
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextTertiary,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun ControlChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AmberGold else DarkSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) AmberGoldBright else DarkBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (compact) 6.dp else 12.dp, vertical = if (compact) 6.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF1B1405) else TextSecondary,
                fontSize = if (compact) 10.sp else 12.sp
            )
        )
    }
}
