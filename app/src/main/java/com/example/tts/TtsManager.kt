package com.example.tts

import android.content.Context
import com.example.data.settings.AppSettings
import com.example.tts.model.GeneratedAudioResult
import com.example.tts.model.TtsException
import com.example.tts.model.VoiceGenerationParams
import com.example.tts.util.AcousticNarratorEngine
import java.io.File

class TtsManager(
    private val context: Context,
    private val appSettings: AppSettings
) {
    val geminiEngine = GeminiTtsEngine {
        appSettings.getEffectiveGeminiApiKey()
    }

    val customBackendEngine = CustomBackendTtsEngine(
        backendUrlProvider = { appSettings.customBackendUrl },
        authHeaderProvider = { appSettings.customBackendAuth }
    )

    val systemEngine = AndroidSystemTtsEngine(context)

    fun getAvailableEngines(): List<TtsEngine> {
        return listOf(geminiEngine, customBackendEngine, systemEngine)
    }

    fun getActiveEngine(): TtsEngine {
        return when (appSettings.selectedProvider) {
            AppSettings.PROVIDER_BACKEND -> customBackendEngine
            AppSettings.PROVIDER_SYSTEM -> systemEngine
            else -> geminiEngine
        }
    }

    suspend fun generateNarration(
        params: VoiceGenerationParams,
        outputDir: File,
        allowFallbackToOffline: Boolean = true,
        onProgress: (progress: Float, status: String) -> Unit
    ): GeneratedAudioResult {
        val activeEngine = getActiveEngine()

        // 1. Try configured engine
        try {
            return activeEngine.generate(params, outputDir, onProgress)
        } catch (e: Exception) {
            if (!allowFallbackToOffline) {
                throw e
            }

            // 2. Fallback to System TTS or Acoustic Narrator
            onProgress(0.2f, "Falling back to device speech engine...")
            return try {
                systemEngine.generate(params, outputDir, onProgress)
            } catch (systemEx: Exception) {
                onProgress(0.4f, "Generating authentic Old American Man narration...")
                AcousticNarratorEngine.generate(params, outputDir, onProgress)
            }
        }
    }
}
