package com.example.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.tts.model.GeneratedAudioResult
import com.example.tts.model.TtsException
import com.example.tts.model.VoiceGenerationParams
import com.example.tts.util.AudioChunker
import com.example.tts.util.WavHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

class AndroidSystemTtsEngine(
    private val context: Context
) : TtsEngine {

    override val id: String = "android_system"
    override val displayName: String = "Device Speech Engine (Offline)"
    override val description: String = "Synthesizes voice locally on device using native Android TextToSpeech"

    private var ttsInstance: TextToSpeech? = null
    private val initDeferred = CompletableDeferred<Boolean>()

    init {
        ttsInstance = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                initDeferred.complete(true)
            } else {
                initDeferred.complete(false)
            }
        }
    }

    override suspend fun generate(
        params: VoiceGenerationParams,
        outputDir: File,
        onProgress: (progress: Float, status: String) -> Unit
    ): GeneratedAudioResult = withContext(Dispatchers.IO) {
        if (params.script.isBlank()) {
            throw TtsException.EmptyScriptException()
        }

        val initialized = initDeferred.await()
        if (!initialized || ttsInstance == null) {
            throw TtsException.SynthesisException("Android Speech Engine failed to initialize on this device.")
        }

        val tts = ttsInstance!!

        // Set Language
        val locale = when (params.language) {
            "Hindi" -> Locale.forLanguageTag("hi-IN")
            else -> Locale.US
        }
        val langResult = tts.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.US)
        }

        // Set Pitch (Old American Man: deep pitch)
        val pitchMultiplier = when (params.pitch) {
            "Low" -> 0.70f
            "High" -> 1.05f
            else -> 0.85f // deeper than standard for mature male character
        }
        tts.setPitch(pitchMultiplier)

        // Set Speed / Rate
        val rateMultiplier = when (params.speed) {
            0.75f -> 0.75f
            1.25f -> 1.25f
            1.5f -> 1.5f
            else -> 0.90f // unhurried campfire cadence
        }
        tts.setSpeechRate(rateMultiplier)

        // Try finding a deep male voice if available in voice set
        try {
            val voices = tts.voices
            if (voices != null) {
                val maleVoice = voices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    (name.contains("male") || name.contains("en-us-x-sfg") || name.contains("en-us-x-tpd")) &&
                            !name.contains("female")
                }
                if (maleVoice != null) {
                    tts.voice = maleVoice
                }
            }
        } catch (_: Exception) {
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val chunks = AudioChunker.chunkStory(params.script)
        val totalChunks = chunks.size
        val chunkFiles = mutableListOf<File>()

        try {
            for ((index, chunkText) in chunks.withIndex()) {
                val current = index + 1
                val progressVal = 0.1f + (index.toFloat() / totalChunks) * 0.75f
                val status = if (totalChunks > 1) {
                    "Synthesizing section $current of $totalChunks..."
                } else {
                    "Synthesizing Old American Man narration locally..."
                }
                onProgress(progressVal, status)

                val chunkFile = File(outputDir, "system_chunk_${System.currentTimeMillis()}_$index.wav")
                synthesizeChunkToFile(tts, chunkText, chunkFile)
                chunkFiles.add(chunkFile)
            }

            onProgress(0.90f, "Finalizing story audio...")
            val finalFileName = "narration_system_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav"
            val finalOutputFile = File(outputDir, finalFileName)

            val durationMs = WavHelper.concatenateAudioFiles(chunkFiles, finalOutputFile)

            onProgress(1.0f, "Narration complete!")
            GeneratedAudioResult(
                audioFile = finalOutputFile,
                durationMs = if (durationMs > 0) durationMs else WavHelper.getAudioDurationMs(finalOutputFile),
                mimeType = "audio/wav",
                chunkCount = totalChunks
            )
        } finally {
            chunkFiles.forEach { file ->
                if (file.exists() && file.name.startsWith("system_chunk_")) {
                    file.delete()
                }
            }
        }
    }

    private suspend fun synthesizeChunkToFile(tts: TextToSpeech, text: String, destination: File) = withContext(Dispatchers.IO) {
        val utteranceId = "utt_${System.currentTimeMillis()}_${destination.name}"
        val deferred = CompletableDeferred<Boolean>()

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    deferred.complete(true)
                }
            }

            override fun onError(id: String?) {
                if (id == utteranceId) {
                    deferred.completeExceptionally(TtsException.SynthesisException("TTS synthesis error for utterance $id"))
                }
            }
        })

        val params = Bundle()
        val result = tts.synthesizeToFile(text, params, destination, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            throw TtsException.SynthesisException("synthesizeToFile failed with code $result")
        }

        deferred.await()
    }
}
