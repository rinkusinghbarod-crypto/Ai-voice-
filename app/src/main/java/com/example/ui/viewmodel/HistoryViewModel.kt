package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.OldStoryVoiceApp
import com.example.audio.AudioPlayer
import com.example.data.model.NarrationEntity
import com.example.data.repository.NarrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as OldStoryVoiceApp
    private val repository: NarrationRepository = app.repository
    val audioPlayer: AudioPlayer = app.audioPlayer

    val searchQuery = MutableStateFlow("")

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    val filteredNarrations: StateFlow<List<NarrationEntity>> = combine(
        repository.allNarrations,
        searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { it.title.lowercase().contains(q) || it.script.lowercase().contains(q) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun playNarration(narration: NarrationEntity) {
        if (audioPlayer.currentFilePath.value == narration.audioFilePath) {
            audioPlayer.togglePlayPause()
        } else {
            audioPlayer.loadAudio(narration.audioFilePath, autoPlay = true)
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedIds.value = emptySet()
        }
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
        if (current.isNotEmpty()) {
            _isSelectionMode.value = true
        }
    }

    fun selectAll() {
        val allIds = filteredNarrations.value.map { it.id }.toSet()
        _selectedIds.value = allIds
        if (allIds.isNotEmpty()) {
            _isSelectionMode.value = true
        }
    }

    fun deselectAll() {
        _selectedIds.value = emptySet()
    }

    fun toggleSelectAll() {
        val allIds = filteredNarrations.value.map { it.id }.toSet()
        if (_selectedIds.value.size == allIds.size && allIds.isNotEmpty()) {
            deselectAll()
        } else {
            _selectedIds.value = allIds
            _isSelectionMode.value = true
        }
    }

    fun deleteNarration(narration: NarrationEntity) {
        viewModelScope.launch {
            if (audioPlayer.currentFilePath.value == narration.audioFilePath) {
                audioPlayer.pause()
            }
            repository.deleteNarration(narration)
            val current = _selectedIds.value.toMutableSet()
            current.remove(narration.id)
            _selectedIds.value = current
            if (current.isEmpty()) {
                _isSelectionMode.value = false
            }
        }
    }

    fun deleteSelected(onDeleted: ((Int) -> Unit)? = null) {
        val idsToDelete = _selectedIds.value
        if (idsToDelete.isEmpty()) return

        val itemsToDelete = filteredNarrations.value.filter { idsToDelete.contains(it.id) }
        val count = itemsToDelete.size

        viewModelScope.launch {
            val currentPlayingPath = audioPlayer.currentFilePath.value
            if (currentPlayingPath != null && itemsToDelete.any { it.audioFilePath == currentPlayingPath }) {
                audioPlayer.pause()
            }

            repository.deleteNarrations(itemsToDelete)
            _selectedIds.value = emptySet()
            _isSelectionMode.value = false
            onDeleted?.invoke(count)
        }
    }
}
