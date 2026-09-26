package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.OldStoryVoiceApp
import com.example.audio.AudioPlayer
import com.example.data.model.NarrationEntity
import com.example.data.repository.NarrationRepository
import com.example.tts.TtsManager
import com.example.tts.model.TtsException
import com.example.tts.model.VoiceGenerationParams
import com.example.ui.model.StoryPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface GenerationUiState {
    object Idle : GenerationUiState
    data class Generating(val progress: Float, val statusMessage: String) : GenerationUiState
    data class Success(val narrationId: Long, val audioPath: String, val title: String) : GenerationUiState
    data class Error(val message: String, val errorType: TtsException.ErrorType, val canOpenSettings: Boolean) : GenerationUiState
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as OldStoryVoiceApp
    private val repository: NarrationRepository = app.repository
    private val ttsManager: TtsManager = app.ttsManager
    val audioPlayer: AudioPlayer = app.audioPlayer

    val latestGeneratedNarration = MutableStateFlow<NarrationEntity?>(null)

    val scriptText = MutableStateFlow("")
    val storyTitle = MutableStateFlow("")
    val selectedVoice = MutableStateFlow("Old American Man")
    val selectedSubStyle = MutableStateFlow("Documentary Narrator")
    val selectedSpeed = MutableStateFlow(1.0f) // 0.75f, 1.0f, 1.25f, 1.5f
    val selectedPitch = MutableStateFlow("Normal") // Low, Normal, High
    val selectedEmotion = MutableStateFlow("Serious") // Calm, Serious, Emotional, Dramatic
    val selectedPauseControl = MutableStateFlow("Natural") // Short, Natural, Long
    val selectedIntensity = MutableStateFlow("Balanced") // Subtle, Balanced, Intense
    val selectedLanguage = MutableStateFlow("English (US)") // English (US), Hindi, Hinglish

    val isPreviewLoading = MutableStateFlow(false)
    val isPreviewPlaying = MutableStateFlow(false)
    private var previewAudioPath: String? = null

    init {
        val defaultDocPreset = com.example.ui.model.StoryPresets.presets.first()
        storyTitle.value = defaultDocPreset.title
        scriptText.value = defaultDocPreset.script

        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(audioPlayer.isPlaying, audioPlayer.currentFilePath) { isPlaying, currentPath ->
                isPlaying && currentPath != null && currentPath == previewAudioPath
            }.collect { isPlayingPreview ->
                isPreviewPlaying.value = isPlayingPreview
            }
        }
    }

    fun toggleVoicePreview() {
        if (isPreviewPlaying.value) {
            audioPlayer.pause()
            isPreviewPlaying.value = false
            return
        }

        viewModelScope.launch {
            isPreviewLoading.value = true
            try {
                val previewDir = File(app.filesDir, "previews").apply { if (!exists()) mkdirs() }
                val styleKey = selectedSubStyle.value.lowercase().replace(" ", "_")
                val previewFile = File(previewDir, "preview_old_man_${styleKey}.wav")

                if (!previewFile.exists() || previewFile.length() <= 44L) {
                    val sampleScript = when (selectedSubStyle.value) {
                        "Documentary Narrator" -> "In the summer of 1863, beneath a quiet July sun, the destiny of an entire nation was forged."
                        "Weathered Cowboy" -> "Out here on the open range, the wind never forgets a trail."
                        "Historical Scholar" -> "History whispers from the canyon walls, if you only listen close."
                        "Campfire Patriarch" -> "Pull up a chair by the embers, son. Let me tell you how it was."
                        else -> "Well now, gather 'round... out here in these quiet hills, every whisper has a tale."
                    }
                    val previewParams = VoiceGenerationParams(
                        script = sampleScript,
                        voiceProfile = selectedVoice.value,
                        subStyle = selectedSubStyle.value,
                        speed = selectedSpeed.value,
                        pitch = selectedPitch.value,
                        emotion = selectedEmotion.value,
                        pauseControl = selectedPauseControl.value,
                        intensity = selectedIntensity.value,
                        language = selectedLanguage.value
                    )
                    val generated = com.example.tts.util.AcousticNarratorEngine.generate(
                        params = previewParams,
                        outputDir = previewDir,
                        onProgress = { _, _ -> }
                    )
                    generated.audioFile.copyTo(previewFile, overwrite = true)
                }

                previewAudioPath = previewFile.absolutePath
                audioPlayer.loadAudio(previewFile.absolutePath, autoPlay = true)
                isPreviewPlaying.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPreviewLoading.value = false
            }
        }
    }

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    fun onScriptChange(newScript: String) {
        scriptText.value = newScript
    }

    fun onTitleChange(newTitle: String) {
        storyTitle.value = newTitle
    }

    fun applyPreset(preset: StoryPreset) {
        storyTitle.value = preset.title
        scriptText.value = preset.script
        selectedEmotion.value = preset.recommendedEmotion
        selectedSubStyle.value = preset.recommendedStyle
        selectedLanguage.value = preset.language
    }

    fun clearScript() {
        scriptText.value = ""
        storyTitle.value = ""
        _generationState.value = GenerationUiState.Idle
    }

    fun resetState() {
        _generationState.value = GenerationUiState.Idle
    }

    fun loadFromHistory(narration: NarrationEntity) {
        storyTitle.value = narration.title
        scriptText.value = narration.script
        selectedVoice.value = narration.voiceName
        selectedSubStyle.value = narration.voiceStyle
        selectedSpeed.value = narration.speed
        selectedPitch.value = narration.pitch
        selectedEmotion.value = narration.emotion
        selectedPauseControl.value = narration.pauseControl
        selectedIntensity.value = narration.intensity
        selectedLanguage.value = narration.language
        _generationState.value = GenerationUiState.Idle
    }

    fun generateVoice(onSuccess: (Long) -> Unit) {
        val script = scriptText.value.trim()
        if (script.isEmpty()) {
            _generationState.value = GenerationUiState.Error(
                message = "Please write or paste a script before generating voice narration.",
                errorType = TtsException.ErrorType.EMPTY_SCRIPT,
                canOpenSettings = false
            )
            return
        }

        val effectiveTitle = storyTitle.value.trim().ifBlank {
            deriveTitleFromScript(script)
        }

        val params = VoiceGenerationParams(
            script = script,
            title = effectiveTitle,
            voiceProfile = selectedVoice.value,
            subStyle = selectedSubStyle.value,
            speed = selectedSpeed.value,
            pitch = selectedPitch.value,
            emotion = selectedEmotion.value,
            pauseControl = selectedPauseControl.value,
            intensity = selectedIntensity.value,
            language = selectedLanguage.value
        )

        viewModelScope.launch {
            _generationState.value = GenerationUiState.Generating(0.05f, "Preparing storyteller...")

            val cacheDir = File(app.filesDir, "narrations").apply {
                if (!exists()) mkdirs()
            }

            try {
                val result = ttsManager.generateNarration(
                    params = params,
                    outputDir = cacheDir,
                    allowFallbackToOffline = true
                ) { progress, status ->
                    _generationState.value = GenerationUiState.Generating(progress, status)
                }

                val entity = NarrationEntity(
                    title = effectiveTitle,
                    script = script,
                    audioFilePath = result.audioFile.absolutePath,
                    durationMs = result.durationMs,
                    voiceName = selectedVoice.value,
                    voiceStyle = selectedSubStyle.value,
                    language = selectedLanguage.value,
                    speed = selectedSpeed.value,
                    pitch = selectedPitch.value,
                    emotion = selectedEmotion.value,
                    pauseControl = selectedPauseControl.value,
                    intensity = selectedIntensity.value,
                    timestamp = System.currentTimeMillis(),
                    fileSizeBytes = result.audioFile.length()
                )

                val savedId = repository.saveNarration(entity)
                val savedEntity = entity.copy(id = savedId)
                latestGeneratedNarration.value = savedEntity

                // Load newly generated audio directly into player
                app.audioPlayer.loadAudio(result.audioFile.absolutePath, autoPlay = true)

                _generationState.value = GenerationUiState.Success(
                    narrationId = savedId,
                    audioPath = result.audioFile.absolutePath,
                    title = effectiveTitle
                )

                onSuccess(savedId)
            } catch (e: TtsException) {
                val canOpenSettings = e.errorType == TtsException.ErrorType.API_AUTH_ERROR ||
                        e.errorType == TtsException.ErrorType.PROVIDER_NOT_CONFIGURED
                _generationState.value = GenerationUiState.Error(
                    message = e.localizedMessage ?: "Voice generation failed",
                    errorType = e.errorType,
                    canOpenSettings = canOpenSettings
                )
            } catch (e: Exception) {
                _generationState.value = GenerationUiState.Error(
                    message = e.localizedMessage ?: "Unexpected error during voice generation",
                    errorType = TtsException.ErrorType.SYNTHESIS_ERROR,
                    canOpenSettings = false
                )
            }
        }
    }

    private fun deriveTitleFromScript(script: String): String {
        val firstLine = script.lines().firstOrNull { it.isNotBlank() } ?: "Old Story Narration"
        val sentence = firstLine.split(". ", "! ", "? ").firstOrNull() ?: firstLine
        val words = sentence.trim().split("\\s+".toRegex()).take(6).joinToString(" ")
        return if (words.length > 35) words.take(35) + "..." else words.ifBlank { "Untitled Story" }
    }
}
