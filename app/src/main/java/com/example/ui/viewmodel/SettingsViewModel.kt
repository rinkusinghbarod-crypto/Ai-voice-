package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.OldStoryVoiceApp
import com.example.data.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as OldStoryVoiceApp
    val settings: AppSettings = app.settings

    private val _selectedProvider = MutableStateFlow(settings.selectedProvider)
    val selectedProvider: StateFlow<String> = _selectedProvider.asStateFlow()

    private val _customGeminiKey = MutableStateFlow(settings.customGeminiApiKey)
    val customGeminiKey: StateFlow<String> = _customGeminiKey.asStateFlow()

    private val _backendUrl = MutableStateFlow(settings.customBackendUrl)
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _backendAuth = MutableStateFlow(settings.customBackendAuth)
    val backendAuth: StateFlow<String> = _backendAuth.asStateFlow()

    private val _cacheSizeMb = MutableStateFlow(0.0)
    val cacheSizeMb: StateFlow<Double> = _cacheSizeMb.asStateFlow()

    private val _isKeyConfigured = MutableStateFlow(settings.isGeminiKeyConfigured())
    val isKeyConfigured: StateFlow<Boolean> = _isKeyConfigured.asStateFlow()

    init {
        updateCacheSize()
    }

    fun selectProvider(providerId: String) {
        settings.selectedProvider = providerId
        _selectedProvider.value = providerId
    }

    fun updateCustomGeminiKey(key: String) {
        settings.customGeminiApiKey = key
        _customGeminiKey.value = key
        _isKeyConfigured.value = settings.isGeminiKeyConfigured()
    }

    fun updateBackendUrl(url: String) {
        settings.customBackendUrl = url
        _backendUrl.value = url
    }

    fun updateBackendAuth(auth: String) {
        settings.customBackendAuth = auth
        _backendAuth.value = auth
    }

    fun updateCacheSize() {
        viewModelScope.launch {
            val size = withContext(Dispatchers.IO) {
                calculateNarrationsDirSize()
            }
            _cacheSizeMb.value = size
        }
    }

    fun clearAudioCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dir = File(app.filesDir, "narrations")
                if (dir.exists()) {
                    dir.listFiles()?.forEach { it.delete() }
                }
            }
            updateCacheSize()
        }
    }

    private fun calculateNarrationsDirSize(): Double {
        val dir = File(app.filesDir, "narrations")
        if (!dir.exists()) return 0.0
        val bytes = dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        return bytes / (1024.0 * 1024.0)
    }
}
