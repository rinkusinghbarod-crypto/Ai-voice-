package com.example.tts.util

import com.example.tts.model.GeneratedAudioResult
import com.example.tts.model.VoiceGenerationParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object AcousticNarratorEngine {

    private const val SAMPLE_RATE = 24000

    suspend fun generate(
        params: VoiceGenerationParams,
        outputDir: File,
        onProgress: (Float, String) -> Unit
    ): GeneratedAudioResult = withContext(Dispatchers.IO) {
        onProgress(0.15f, "Crafting Old American Man voice narration...")

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val baseF0 = when (params.pitch) {
            "Low" -> 85.0
            "High" -> 120.0
            else -> 100.0 // Mature 65-75yo male fundamental frequency
        }

        val speedFactor = params.speed.coerceIn(0.75f, 1.5f)
        val words = params.script.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val totalWords = words.size

        val pcmStream = ByteArrayOutputStream()

        // Initial breath intake (natural pause)
        writeSilence(pcmStream, (300 / speedFactor).toInt())

        for ((index, word) in words.withIndex()) {
            val progress = 0.2f + (index.toFloat() / totalWords) * 0.7f
            if (index % 10 == 0) {
                onProgress(progress, "Narrating word ${index + 1} of $totalWords...")
            }

            // Clean word and detect punctuation
            val cleaned = word.replace(Regex("[^a-zA-Z0-9]"), "")
            val isSentenceEnd = word.endsWith(".") || word.endsWith("!") || word.endsWith("?")
            val isClauseEnd = word.endsWith(",") || word.endsWith(";") || word.endsWith(":") || word.endsWith("-")

            val syllables = (cleaned.length / 3).coerceIn(1, 4)
            val wordDurationMs = ((syllables * 180) / speedFactor).toInt()

            // Generate weathered vocal waveform for the word
            generateWordAudio(
                pcmStream = pcmStream,
                durationMs = wordDurationMs,
                baseF0 = baseF0,
                syllables = syllables,
                emotion = params.emotion,
                subStyle = params.subStyle
            )

            // Short inter-word gap
            writeSilence(pcmStream, (55 / speedFactor).toInt())

            // Punctuation pauses
            if (isSentenceEnd) {
                // Natural reflective pause
                val pauseMs = when (params.pauseControl) {
                    "Short" -> 400
                    "Long" -> 900
                    else -> 650
                }
                writeSilence(pcmStream, (pauseMs / speedFactor).toInt())
            } else if (isClauseEnd) {
                writeSilence(pcmStream, (280 / speedFactor).toInt())
            }
        }

        // Outro pause
        writeSilence(pcmStream, 400)

        onProgress(0.95f, "Writing audio file...")

        val rawPcm = pcmStream.toByteArray()
        val finalFileName = "narration_voice_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav"
        val outputFile = File(outputDir, finalFileName)

        FileOutputStream(outputFile).use { fos ->
            val header = WavHelper.createWavHeader(
                totalAudioLen = rawPcm.size.toLong(),
                sampleRate = SAMPLE_RATE.toLong(),
                channels = 1,
                bitsPerSample = 16
            )
            fos.write(header)
            fos.write(rawPcm)
        }

        val durationMs = (rawPcm.size * 1000L) / (SAMPLE_RATE * 2)

        onProgress(1.0f, "Narration ready!")

        GeneratedAudioResult(
            audioFile = outputFile,
            durationMs = durationMs,
            mimeType = "audio/wav",
            chunkCount = 1
        )
    }

    private fun generateWordAudio(
        pcmStream: ByteArrayOutputStream,
        durationMs: Int,
        baseF0: Double,
        syllables: Int,
        emotion: String,
        subStyle: String
    ) {
        val totalSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)

        // Resonant formant frequencies for deep weathered male storytelling voice
        val f1 = 650.0  // First vowel formant (Hz)
        val f2 = 1200.0 // Second vowel formant (Hz)
        val f3 = 2400.0 // Third vowel formant (Hz)

        val raspAmount = when (subStyle) {
            "Documentary Narrator" -> 0.24
            "Weathered Cowboy" -> 0.28
            "Frontier Storyteller" -> 0.22
            else -> 0.18
        }

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / totalSamples

            // Syllable amplitude envelope (rise, hold, fall)
            val env = sin(progress * PI).coerceAtLeast(0.0)

            // Slight pitch cadence inflection per syllable
            val syllablePhase = (progress * syllables) % 1.0
            val pitchMod = when (emotion) {
                "Dramatic" -> 1.0 + 0.12 * sin(progress * 2 * PI)
                "Emotional" -> 1.0 - 0.08 * progress
                "Serious" -> 1.0 - 0.05 * progress
                else -> 1.0 + 0.04 * sin(progress * PI)
            }
            val f0 = baseF0 * pitchMod

            // Voice glottal pulse + acoustic formants
            val glottal = sin(2 * PI * f0 * t)
            val formant1 = 0.50 * sin(2 * PI * f1 * t)
            val formant2 = 0.30 * sin(2 * PI * f2 * t)
            val formant3 = 0.15 * sin(2 * PI * f3 * t)

            // Vocal rasp: subharmonic low gravel + slight turbulent air breath
            val subharmonicGravel = raspAmount * sin(PI * f0 * t)
            val breathNoise = (Math.random() - 0.5) * 0.06

            val sampleVal = (glottal * 0.4 + formant1 + formant2 + formant3 + subharmonicGravel + breathNoise) * env

            val pcmSample = (sampleVal * 16000.0).toInt().coerceIn(-32767, 32767).toShort()

            buffer.clear()
            buffer.putShort(pcmSample)
            pcmStream.write(buffer.array())
        }
    }

    private fun writeSilence(pcmStream: ByteArrayOutputStream, durationMs: Int) {
        val count = (SAMPLE_RATE * durationMs) / 1000
        val zeroBytes = ByteArray(count * 2)
        pcmStream.write(zeroBytes)
    }
}
