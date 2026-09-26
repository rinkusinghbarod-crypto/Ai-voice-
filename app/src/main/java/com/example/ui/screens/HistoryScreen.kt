package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NarrationEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SaddleBronze
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.HistoryViewModel
import com.example.utils.AudioExportHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenInPlayer: (NarrationEntity) -> Unit,
    onRegenerateInStudio: (NarrationEntity) -> Unit,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val narrations by viewModel.filteredNarrations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val currentFilePath by viewModel.audioPlayer.currentFilePath.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    var singleNarrationToDelete by remember { mutableStateOf<NarrationEntity?>(null) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val allFilteredSelected = narrations.isNotEmpty() && selectedIds.size == narrations.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title and Mode Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSelectionMode) "${selectedIds.size} Selected" else "Narration Library",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelectionMode && selectedIds.isNotEmpty()) AmberGoldBright else TextPrimary
                    )
                )
                Text(
                    text = if (isSelectionMode) {
                        "Choose stories to remove from library"
                    } else {
                        "${narrations.size} saved recordings"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            if (narrations.isNotEmpty()) {
                if (!isSelectionMode) {
                    OutlinedButton(
                        onClick = { viewModel.setSelectionMode(true) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        ),
                        modifier = Modifier.testTag("enter_selection_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select",
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    TextButton(
                        onClick = { viewModel.setSelectionMode(false) },
                        modifier = Modifier.testTag("cancel_selection_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel selection",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Done",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selection Action Bar when Selection Mode is Active
        AnimatedVisibility(
            visible = isSelectionMode && narrations.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AmberGold.copy(alpha = 0.5f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Select All / Deselect All Button
                    Button(
                        onClick = { viewModel.toggleSelectAll() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allFilteredSelected) AmberContainer else DarkSurfaceVariant,
                            contentColor = if (allFilteredSelected) AmberGoldBright else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("select_all_button")
                    ) {
                        Icon(
                            imageVector = if (allFilteredSelected) Icons.Default.Close else Icons.Default.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (allFilteredSelected) "Deselect All" else "Select All (${narrations.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Delete Selected Button
                    Button(
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                showBatchDeleteDialog = true
                            }
                        },
                        enabled = selectedIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            contentColor = Color.White,
                            disabledContainerColor = DarkSurfaceVariant,
                            disabledContentColor = TextTertiary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("delete_selected_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delete Selected (${selectedIds.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text("Search stories by title or script...", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AmberGold,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkCharcoalSurface,
                unfocusedContainerColor = DarkCharcoalSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (narrations.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching stories found" else "No saved narrations yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (searchQuery.isNotBlank()) "Try a different search term" else "Bring your first frontier story to life in the studio.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    if (searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF191102))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create A Story", color = Color(0xFF191102), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(narrations, key = { it.id }) { item ->
                    val isCurrent = currentFilePath == item.audioFilePath
                    val isItemPlaying = isCurrent && isPlaying
                    val isSelected = selectedIds.contains(item.id)

                    HistoryItemCard(
                        item = item,
                        isPlaying = isItemPlaying,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onToggleSelect = { viewModel.toggleSelection(item.id) },
                        onLongClick = {
                            viewModel.setSelectionMode(true)
                            viewModel.toggleSelection(item.id)
                        },
                        onPlayClick = { viewModel.playNarration(item) },
                        onCardClick = {
                            if (isSelectionMode) {
                                viewModel.toggleSelection(item.id)
                            } else {
                                onOpenInPlayer(item)
                            }
                        },
                        onDownloadClick = {
                            val res = AudioExportHelper.saveAudioToDevice(context, item.audioFilePath, item.title)
                            if (res.isSuccess) {
                                Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Download failed: ${res.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        onShareClick = {
                            AudioExportHelper.shareAudio(context, item.audioFilePath, item.title)
                        },
                        onRegenerateClick = { onRegenerateInStudio(item) },
                        onDeleteClick = { singleNarrationToDelete = item }
                    )
                }
            }
        }
    }

    // Single Item Delete Confirmation Dialog
    if (singleNarrationToDelete != null) {
        val target = singleNarrationToDelete!!
        AlertDialog(
            onDismissRequest = { singleNarrationToDelete = null },
            title = {
                Text("Delete Narration?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${target.title}\"? The audio file will be permanently removed.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNarration(target)
                        singleNarrationToDelete = null
                        Toast.makeText(context, "Narration deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { singleNarrationToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCharcoalSurface
        )
    }

    // Batch Delete Selected Confirmation Dialog
    if (showBatchDeleteDialog) {
        val count = selectedIds.size
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = {
                Text(
                    text = "Delete $count Selected Stories?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete these $count selected narrations? The audio files will be permanently deleted from your device.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelected { deletedCount ->
                            Toast.makeText(context, "$deletedCount stories deleted from library", Toast.LENGTH_SHORT).show()
                        }
                        showBatchDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.testTag("confirm_delete_selected_button")
                ) {
                    Text("Delete ($count)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCharcoalSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItemCard(
    item: NarrationEntity,
    isPlaying: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongClick: () -> Unit,
    onPlayClick: () -> Unit,
    onCardClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }
    val formattedDuration = remember(item.durationMs) { formatDuration(item.durationMs) }

    val borderColor = if (isSelected) AmberGold else DarkBorder
    val containerBg = if (isSelected) DarkSurfaceHigh else DarkCharcoalSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onCardClick,
                onLongClick = onLongClick
            )
            .testTag("history_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox when in selection mode
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AmberGold,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = Color(0xFF191102)
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("checkbox_${item.id}")
                    )
                }

                // Play/Pause circular icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) AmberGold else DarkSurfaceVariant)
                        .clickable(onClick = onPlayClick)
                        .testTag("history_play_btn_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) Color(0xFF191102) else AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) AmberGoldBright else TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$formattedDate  •  $formattedDuration",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Voice & Emotion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AmberContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.voiceName} • ${item.voiceStyle}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AmberGoldBright,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.emotion} • ${item.speed}x",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Download, Share, Regenerate, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDownloadClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download audio",
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share audio",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onRegenerateClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit & Regenerate",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "--"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
