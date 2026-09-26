package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayer
import com.example.data.model.NarrationEntity
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SaddleBronze
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.utils.AudioExportHelper

@Composable
fun PlayerScreen(
    audioPlayer: AudioPlayer,
    currentNarration: NarrationEntity?,
    onRegenerateInStudio: (NarrationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentPositionMs by audioPlayer.currentPositionMs.collectAsState()
    val durationMs by audioPlayer.durationMs.collectAsState()
    val playbackSpeed by audioPlayer.playbackSpeed.collectAsState()
    val currentFilePath by audioPlayer.currentFilePath.collectAsState()

    var isTranscriptExpanded by remember { mutableStateOf(false) }

    val effectiveDuration = if (durationMs > 0) durationMs else (currentNarration?.durationMs ?: 1L)
    val progress = if (effectiveDuration > 0) (currentPositionMs.toFloat() / effectiveDuration).coerceIn(0f, 1f) else 0f

    LaunchedEffect(currentNarration?.audioFilePath) {
        val path = currentNarration?.audioFilePath
        if (path != null && currentFilePath != path) {
            audioPlayer.loadAudio(path, autoPlay = true)
        }
    }

    val title = currentNarration?.title ?: "Story Narration"
    val voiceDesc = "${currentNarration?.voiceName ?: "Old American Man"} • ${currentNarration?.voiceStyle ?: "Frontier Storyteller"}"
    val emotion = currentNarration?.emotion ?: "Calm"
    val script = currentNarration?.script ?: ""

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Player Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(AmberGold.copy(alpha = 0.4f), DarkBorder))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = voiceDesc,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmberGoldBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Emotion: $emotion • Lang: ${currentNarration?.language ?: "English"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dynamic Waveform Visualizer
                    WaveformVisualizer(
                        isPlaying = isPlaying,
                        progress = progress,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timeline Scrubber Slider
                    Slider(
                        value = currentPositionMs.toFloat().coerceIn(0f, effectiveDuration.toFloat()),
                        onValueChange = { targetPos ->
                            audioPlayer.seekTo(targetPos.toLong())
                        },
                        valueRange = 0f..effectiveDuration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = AmberGoldBright,
                            activeTrackColor = AmberGold,
                            inactiveTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timeline_slider")
                    )

                    // Current Position & Duration labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = formatTime(effectiveDuration),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Playback Controls Row: Replay, Skip -10, Play/Pause, Skip +10
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Replay
                        IconButton(
                            onClick = { audioPlayer.replay() },
                            modifier = Modifier.size(44.dp).testTag("replay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay from start",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Skip -10s
                        IconButton(
                            onClick = { audioPlayer.skipBackward(10) },
                            modifier = Modifier.size(44.dp).testTag("skip_backward_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Skip back 10 seconds",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Large Play/Pause Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(8.dp, CircleShape, ambientColor = AmberGold, spotColor = AmberGold)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(AmberGold, SaddleBronze))
                                )
                                .clickable {
                                    val path = currentNarration?.audioFilePath
                                    if (currentFilePath == null && path != null) {
                                        audioPlayer.loadAudio(path, autoPlay = true)
                                    } else {
                                        audioPlayer.togglePlayPause()
                                    }
                                }
                                .testTag("play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF191102),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Skip +10s
                        IconButton(
                            onClick = { audioPlayer.skipForward(10) },
                            modifier = Modifier.size(44.dp).testTag("skip_forward_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Skip forward 10 seconds",
                                tint = TextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Spacer placeholder for balance
                        Box(modifier = Modifier.size(44.dp))
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Playback Speed Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Speed:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextTertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                        speeds.forEach { sp ->
                            val isSelected = (playbackSpeed == sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AmberGold else DarkSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isSelected) AmberGoldBright else DarkBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { audioPlayer.setPlaybackSpeed(sp) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${sp}x",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color(0xFF191102) else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Download MP3 & Share Audio
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download Button
                Button(
                    onClick = {
                        val path = currentFilePath ?: currentNarration?.audioFilePath
                        if (path != null) {
                            val res = AudioExportHelper.saveAudioToDevice(context, path, title)
                            if (res.isSuccess) {
                                Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Download failed: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No audio loaded to download", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("download_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = Color(0xFF191102)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download MP3",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        val path = currentFilePath ?: currentNarration?.audioFilePath
                        if (path != null) {
                            AudioExportHelper.shareAudio(context, path, title)
                        } else {
                            Toast.makeText(context, "No audio loaded to share", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("share_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AmberGold)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AmberGoldBright
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Audio",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Regenerate Button
        if (currentNarration != null) {
            item {
                OutlinedButton(
                    onClick = { onRegenerateInStudio(currentNarration) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("regenerate_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit & Regenerate in Studio")
                }
            }
        }

        // Expandable Script Transcript
        if (script.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { isTranscriptExpanded = !isTranscriptExpanded },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Story Script Transcript",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Icon(
                                imageVector = if (isTranscriptExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isTranscriptExpanded) "Collapse" else "Expand",
                                tint = TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = isTranscriptExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = script,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        lineHeight = 22.sp,
                                        fontFamily = FontFamily.Serif
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
