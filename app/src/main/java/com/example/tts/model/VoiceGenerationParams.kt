package com.example.tts.model

import java.io.File

data class VoiceGenerationParams(
    val script: String,
    val title: String = "",
    val voiceProfile: String = "Old American Man",
    val subStyle: String = "Frontier Storyteller",
    val speed: Float = 1.0f,
    val pitch: String = "Normal",
    val emotion: String = "Calm",
    val pauseControl: String = "Natural",
    val intensity: String = "Balanced",
    val language: String = "English (US)"
)

data class GeneratedAudioResult(
    val audioFile: File,
    val durationMs: Long,
    val mimeType: String = "audio/wav",
    val chunkCount: Int = 1
)

sealed class TtsException(message: String, val errorType: ErrorType) : Exception(message) {
    enum class ErrorType {
        EMPTY_SCRIPT,
        NETWORK_ERROR,
        API_AUTH_ERROR,
        TIMEOUT_ERROR,
        UNSUPPORTED_TEXT,
        SYNTHESIS_ERROR,
        PROVIDER_NOT_CONFIGURED
    }

    class EmptyScriptException : TtsException("Script is empty. Please enter your story text.", ErrorType.EMPTY_SCRIPT)
    class NetworkException(cause: Throwable?) : TtsException("Network connection failed. Please check your internet connection.", ErrorType.NETWORK_ERROR)
    class ApiAuthException(msg: String) : TtsException("API authentication failed: $msg. Check your Gemini API key or backend credentials in Settings.", ErrorType.API_AUTH_ERROR)
    class TimeoutException : TtsException("Request timed out while generating voice. Try a shorter section or check server status.", ErrorType.TIMEOUT_ERROR)
    class UnsupportedTextException(msg: String) : TtsException("Unsupported text format or characters: $msg", ErrorType.UNSUPPORTED_TEXT)
    class SynthesisException(msg: String) : TtsException("Generation failed: $msg", ErrorType.SYNTHESIS_ERROR)
    class ProviderNotConfiguredException(msg: String) : TtsException(msg, ErrorType.PROVIDER_NOT_CONFIGURED)
}
