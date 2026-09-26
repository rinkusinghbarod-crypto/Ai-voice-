package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SaddleBronze
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCharacteristicCard(
    selectedSubStyle: String,
    onSubStyleSelected: (String) -> Unit,
    isPreviewPlaying: Boolean = false,
    isPreviewLoading: Boolean = false,
    onPreviewVoiceClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val subStyles = listOf(
        "Documentary Narrator",
        "Frontier Storyteller",
        "Weathered Cowboy",
        "Historical Scholar",
        "Campfire Patriarch"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(AmberGold.copy(alpha = 0.5f), DarkBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Voice Icon & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AmberGold, SaddleBronze)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Old American Man Voice",
                        tint = Color(0xFF1B1405),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Old American Man",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AmberContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "65–75 yrs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AmberGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Text(
                        text = "Deep, warm, slightly raspy weathered vocal timbre",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trait badges and Preview Voice button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TraitBadge(text = "American Accent")
                    TraitBadge(text = "Natural Pauses")
                }

                // Small 'Preview Voice' button (5s sample)
                Button(
                    onClick = onPreviewVoiceClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPreviewPlaying) AmberGold else DarkSurfaceVariant,
                        contentColor = if (isPreviewPlaying) Color(0xFF191102) else AmberGoldBright
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isPreviewPlaying) AmberGoldBright else AmberGold.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("preview_voice_button")
                ) {
                    if (isPreviewLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = AmberGoldBright,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    } else if (isPreviewPlaying) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Preview",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Playing (5s)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Preview Voice",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Preview Voice (5s)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Narrator Style Persona",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextTertiary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                subStyles.forEach { style ->
                    val isSelected = style == selectedSubStyle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSubStyleSelected(style) },
                        label = {
                            Text(
                                text = style,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextSecondary,
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = Color(0xFF1B1405)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) AmberGold else DarkBorder,
                            selectedBorderColor = AmberGoldBright,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val styleExplanation = when (selectedSubStyle) {
                "Documentary Narrator" -> "Ken Burns & History Channel style — solemn, authoritative, gravelly historical gravitas."
                "Weathered Cowboy" -> "Rugged frontier grit — gravel-throated, slow drawl from a lifetime on the open trail."
                "Historical Scholar" -> "Reflective, measured, and intellectual — deep contemplative cadence."
                "Campfire Patriarch" -> "Warm, intimate grandfatherly timbre — heartfelt wisdom by evening embers."
                else -> "Expansive, cinematic American storytelling — rich timbre with natural reflective pauses."
            }

            Text(
                text = styleExplanation,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AmberGoldBright.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun TraitBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .border(0.5.dp, DarkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 10.sp
            )
        )
    }
}
