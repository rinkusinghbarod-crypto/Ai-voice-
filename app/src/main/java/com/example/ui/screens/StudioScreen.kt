package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.NarrationEntity
import com.example.utils.AudioExportHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tts.model.TtsException
import com.example.ui.components.GenerationProgressDialog
import com.example.ui.components.VoiceCharacteristicCard
import com.example.ui.components.VoiceControlsPanel
import com.example.ui.model.StoryPresets
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SaddleBronze
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.GenerationUiState
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val script by viewModel.scriptText.collectAsState()
    val title by viewModel.storyTitle.collectAsState()
    val subStyle by viewModel.selectedSubStyle.collectAsState()
    val speed by viewModel.selectedSpeed.collectAsState()
    val pitch by viewModel.selectedPitch.collectAsState()
    val emotion by viewModel.selectedEmotion.collectAsState()
    val pauseControl by viewModel.selectedPauseControl.collectAsState()
    val intensity by viewModel.selectedIntensity.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()
    val genState by viewModel.generationState.collectAsState()
    val latestNarration by viewModel.latestGeneratedNarration.collectAsState()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsState()
    val isPreviewLoading by viewModel.isPreviewLoading.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    val wordCount = if (script.isBlank()) 0 else script.trim().split("\\s+".toRegex()).size
    val charCount = script.length
    // Average speech rate is ~130 words per minute
    val estimatedSeconds = if (wordCount == 0) 0 else ((wordCount / (130f * speed)) * 60).toInt()
    val estMin = estimatedSeconds / 60
    val estSec = estimatedSeconds % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            HeaderBranding(onOpenSettings = onNavigateToSettings)
        }

        // Story Presets Row
        item {
            StoryPresetsRow(onSelectPreset = { viewModel.applyPreset(it) })
        }

        // Script Input Section
        item {
            ScriptInputCard(
                title = title,
                onTitleChange = { viewModel.onTitleChange(it) },
                script = script,
                onScriptChange = { viewModel.onScriptChange(it) },
                wordCount = wordCount,
                charCount = charCount,
                estDuration = if (estimatedSeconds > 0) "${estMin}m ${estSec}s" else "--",
                onClear = { viewModel.clearScript() },
                onPaste = {
                    val clip = clipboardManager.getText()
                    if (clip != null) {
                        viewModel.onScriptChange(script + (if (script.isNotBlank()) "\n" else "") + clip.text)
                    }
                }
            )
        }

        // Direct Quick Play & Download Card for newly generated narration
        if (latestNarration != null) {
            item {
                LatestGeneratedAudioCard(
                    narration = latestNarration!!,
                    isPlaying = isPlaying,
                    onPlayPause = {
                        viewModel.audioPlayer.togglePlayPause()
                    },
                    onDownload = {
                        val res = AudioExportHelper.saveAudioToDevice(
                            context,
                            latestNarration!!.audioFilePath,
                            latestNarration!!.title
                        )
                        if (res.isSuccess) {
                            Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Download failed: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onOpenPlayer = {
                        onNavigateToPlayer(latestNarration!!.id)
                    }
                )
            }
        }

        // Error message banner if any
        if (genState is GenerationUiState.Error) {
            val error = genState as GenerationUiState.Error
            item {
                ErrorBanner(
                    message = error.message,
                    canOpenSettings = error.canOpenSettings,
                    onOpenSettings = onNavigateToSettings,
                    onDismiss = { viewModel.resetState() }
                )
            }
        }

        // Dedicated Voice Profile Card
        item {
            VoiceCharacteristicCard(
                selectedSubStyle = subStyle,
                onSubStyleSelected = { viewModel.selectedSubStyle.value = it },
                isPreviewPlaying = isPreviewPlaying,
                isPreviewLoading = isPreviewLoading,
                onPreviewVoiceClick = { viewModel.toggleVoicePreview() }
            )
        }

        // Controls (Speed, Pitch, Emotion, Pause, Intensity, Language)
        item {
            VoiceControlsPanel(
                speed = speed,
                onSpeedChange = { viewModel.selectedSpeed.value = it },
                pitch = pitch,
                onPitchChange = { viewModel.selectedPitch.value = it },
                emotion = emotion,
                onEmotionChange = { viewModel.selectedEmotion.value = it },
                pauseControl = pauseControl,
                onPauseControlChange = { viewModel.selectedPauseControl.value = it },
                intensity = intensity,
                onIntensityChange = { viewModel.selectedIntensity.value = it },
                language = language,
                onLanguageChange = { viewModel.selectedLanguage.value = it }
            )
        }

        // Prominent Large GENERATE AI VOICE Button
        item {
            GenerateButton(
                onClick = {
                    viewModel.generateVoice { narrationId ->
                        onNavigateToPlayer(narrationId)
                    }
                }
            )
        }
    }

    // Progress Dialog when Generating
    if (genState is GenerationUiState.Generating) {
        val gen = genState as GenerationUiState.Generating
        GenerationProgressDialog(
            progress = gen.progress,
            statusMessage = gen.statusMessage
        )
    }
}

@Composable
private fun HeaderBranding(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AmberGold, SaddleBronze))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Logo",
                    tint = Color(0xFF191102),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Old Story Voice AI",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "Realistic Cinematic Story Narration",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AmberGoldBright,
                        fontSize = 12.sp
                    )
                )
            }
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings & API",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun StoryPresetsRow(onSelectPreset: (com.example.ui.model.StoryPreset) -> Unit) {
    Column {
        Text(
            text = "Story Ideas & Presets",
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextTertiary,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StoryPresets.presets) { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${preset.description} • ${preset.language}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmberGold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptInputCard(
    title: String,
    onTitleChange: (String) -> Unit,
    script: String,
    onScriptChange: (String) -> Unit,
    wordCount: Int,
    charCount: Int,
    estDuration: String,
    onClear: () -> Unit,
    onPaste: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Optional title input
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = {
                    Text(
                        "Story Title (Optional - auto-generated if left blank)",
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Script Box
            OutlinedTextField(
                value = script,
                onValueChange = onScriptChange,
                placeholder = {
                    Text(
                        "Write or paste your story, script, or narrative here...\n\n" +
                                "Example: 'The wind had a bite to it that evening in Deadwood Creek. Fifty years ago, young men traded everything for a glimmer of gold in these hills...'",
                        color = TextTertiary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("script_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stats & Utility actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Character & Word Counter
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "$charCount chars",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                    )
                    Text(
                        text = "$wordCount words",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                    )
                    Text(
                        text = "Est. $estDuration",
                        style = MaterialTheme.typography.labelSmall.copy(color = AmberGoldBright, fontWeight = FontWeight.Bold)
                    )
                }

                // Action buttons: Clear & Paste
                Row {
                    if (script.isNotBlank()) {
                        IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear script",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onPaste, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from clipboard",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    canOpenSettings: Boolean,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorContainer),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ErrorRed))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = ErrorRed,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                )

                if (canOpenSettings) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Configure API / Backend",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF191102),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Dismiss error",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun GenerateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = AmberGold, spotColor = AmberGold)
            .testTag("generate_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AmberGold,
            contentColor = Color(0xFF1B1300)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF1B1300),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "GENERATE AI VOICE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    fontSize = 17.sp
                )
            )
        }
    }
}

@Composable
private fun LatestGeneratedAudioCard(
    narration: NarrationEntity,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onDownload: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AmberGold, DarkBorder))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ Narration Ready",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AmberGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                IconButton(onClick = onOpenPlayer, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Open in full player",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = narration.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = "${narration.voiceName} • ${narration.voiceStyle} • ${narration.emotion}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Button(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) AmberGoldBright else AmberGold,
                        contentColor = Color(0xFF191102)
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "PAUSE" else "PLAY AUDIO",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Download MP3 Button
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = AmberGoldBright
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DOWNLOAD",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
