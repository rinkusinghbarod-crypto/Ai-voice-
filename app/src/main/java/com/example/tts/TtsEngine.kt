package com.example.tts

import com.example.tts.model.GeneratedAudioResult
import com.example.tts.model.VoiceGenerationParams
import java.io.File

interface TtsEngine {
    val id: String
    val displayName: String
    val description: String

    suspend fun generate(
        params: VoiceGenerationParams,
        outputDir: File,
        onProgress: (progress: Float, status: String) -> Unit
    ): GeneratedAudioResult
}
