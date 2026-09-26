package com.example.tts

import android.util.Base64
import com.example.tts.model.GeneratedAudioResult
import com.example.tts.model.TtsException
import com.example.tts.model.VoiceGenerationParams
import com.example.tts.util.AudioChunker
import com.example.tts.util.WavHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiTtsEngine(
    private val apiKeyProvider: () -> String
) : TtsEngine {

    override val id: String = "gemini_tts"
    override val displayName: String = "Gemini 2.5 Flash TTS"
    override val description: String = "High-fidelity AI voice with deep mature American male timbre"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun generate(
        params: VoiceGenerationParams,
        outputDir: File,
        onProgress: (progress: Float, status: String) -> Unit
    ): GeneratedAudioResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw TtsException.ApiAuthException("Gemini API key is not configured.")
        }

        if (params.script.isBlank()) {
            throw TtsException.EmptyScriptException()
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        onProgress(0.05f, "Preparing story sections...")
        val chunks = AudioChunker.chunkStory(params.script)
        val totalChunks = chunks.size
        val chunkAudioFiles = mutableListOf<File>()

        try {
            for ((index, chunkText) in chunks.withIndex()) {
                val currentSection = index + 1
                val sectionProgressStart = 0.1f + (index.toFloat() / totalChunks) * 0.75f
                val statusMsg = if (totalChunks > 1) {
                    "Narrating section $currentSection of $totalChunks..."
                } else {
                    "Synthesizing Old American Man narration..."
                }
                onProgress(sectionProgressStart, statusMsg)

                val audioFile = generateChunkAudio(
                    chunkText = chunkText,
                    params = params,
                    outputDir = outputDir,
                    apiKey = apiKey,
                    index = index
                )
                chunkAudioFiles.add(audioFile)
            }

            onProgress(0.90f, "Combining audio sections...")
            val finalFileName = "narration_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.wav"
            val finalOutputFile = File(outputDir, finalFileName)

            val durationMs = WavHelper.concatenateAudioFiles(chunkAudioFiles, finalOutputFile)

            onProgress(1.0f, "Audio narration ready!")
            GeneratedAudioResult(
                audioFile = finalOutputFile,
                durationMs = if (durationMs > 0) durationMs else WavHelper.getAudioDurationMs(finalOutputFile),
                mimeType = "audio/wav",
                chunkCount = totalChunks
            )
        } catch (e: Exception) {
            // Cleanup partial chunk files
            chunkAudioFiles.forEach { it.delete() }
            throw mapException(e)
        } finally {
            // Delete temp chunk files after successful merge
            chunkAudioFiles.forEach { file ->
                if (file.exists() && file.name.startsWith("chunk_")) {
                    file.delete()
                }
            }
        }
    }

    private fun generateChunkAudio(
        chunkText: String,
        params: VoiceGenerationParams,
        outputDir: File,
        apiKey: String,
        index: Int
    ): File {
        // Map subStyle / voice parameters to system prompt instructions
        val prompt = buildVoicePrompt(chunkText, params)

        // Select voice: "Fenrir" (deep resonant male) or "Charon"
        val voiceName = when (params.subStyle) {
            "Documentary Narrator" -> "Charon"
            "Weathered Cowboy" -> "Fenrir"
            "Frontier Storyteller" -> "Fenrir"
            "Historical Scholar" -> "Charon"
            "Campfire Patriarch" -> "Fenrir"
            else -> "Fenrir"
        }

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val generationConfig = JSONObject().apply {
                val modalities = JSONArray().apply {
                    put("AUDIO")
                }
                put("responseModalities", modalities)

                val speechConfig = JSONObject().apply {
                    val voiceConfig = JSONObject().apply {
                        val prebuiltVoiceConfig = JSONObject().apply {
                            put("voiceName", voiceName)
                        }
                        put("prebuiltVoiceConfig", prebuiltVoiceConfig)
                    }
                    put("voiceConfig", voiceConfig)
                }
                put("speechConfig", speechConfig)
            }
            put("generationConfig", generationConfig)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        // Using gemini-2.5-flash-preview-tts model as defined in gemini-api skill for TTS tasks
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw TtsException.SynthesisException("Empty response from server")

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(responseBody, response.code)
            if (response.code == 400 || response.code == 401 || response.code == 403) {
                throw TtsException.ApiAuthException(errorMsg)
            } else if (response.code == 429) {
                throw TtsException.SynthesisException("API rate limit reached. Please wait a moment.")
            } else {
                throw TtsException.SynthesisException("API error (${response.code}): $errorMsg")
            }
        }

        val base64Data = parseAudioBase64(responseBody)
            ?: throw TtsException.SynthesisException("No audio stream received in Gemini response.")

        val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
        val tempChunkFile = File(outputDir, "chunk_${System.currentTimeMillis()}_$index.wav")

        FileOutputStream(tempChunkFile).use { fos ->
            val isAlreadyWav = audioBytes.size >= 12 &&
                    audioBytes[0] == 'R'.code.toByte() &&
                    audioBytes[1] == 'I'.code.toByte() &&
                    audioBytes[2] == 'F'.code.toByte() &&
                    audioBytes[3] == 'F'.code.toByte()
            val isMp3 = audioBytes.size >= 3 &&
                    audioBytes[0] == 'I'.code.toByte() &&
                    audioBytes[1] == 'D'.code.toByte() &&
                    audioBytes[2] == '3'.code.toByte()

            if (isAlreadyWav || isMp3) {
                fos.write(audioBytes)
            } else {
                // Gemini TTS delivers raw PCM samples (24kHz 16-bit mono)
                val header = WavHelper.createWavHeader(
                    totalAudioLen = audioBytes.size.toLong(),
                    sampleRate = 24000L,
                    channels = 1,
                    bitsPerSample = 16
                )
                fos.write(header)
                fos.write(audioBytes)
            }
        }

        return tempChunkFile
    }

    private fun buildVoicePrompt(text: String, params: VoiceGenerationParams): String {
        val speedDesc = when (params.speed) {
            0.75f -> "slow, deliberate pace"
            1.25f -> "brisk, engaging pace"
            1.5f -> "quickened pace"
            else -> "measured, unhurried, natural storytelling pace"
        }

        val pitchDesc = when (params.pitch) {
            "Low" -> "deepest baritone gravel register"
            "High" -> "slightly higher storytelling register"
            else -> "natural deep weathered baritone"
        }

        val emotionDesc = when (params.emotion) {
            "Serious" -> "serious, reflective, solemn and weighty"
            "Emotional" -> "tender, poignant, nostalgic and deeply moving"
            "Dramatic" -> "cinematic, intense, gripping and suspenseful"
            else -> "calm, grounded, warm and wise"
        }

        val pauseDesc = when (params.pauseControl) {
            "Short" -> "brief pauses between thoughts"
            "Long" -> "pregnant, reflective pauses allowing the drama to sink in"
            else -> "natural human breathing and narrative pauses"
        }

        return buildString {
            if (params.subStyle == "Documentary Narrator") {
                append("Act as an iconic, veteran American male documentary narrator (aged 68-75, reminiscent of Ken Burns, Shelby Foote, and National Geographic historical chronicles). ")
                append("Character: Authoritative, solemn, deeply thoughtful, historical weight, weathered baritone warmth with gravel and reverence for history. ")
            } else {
                append("Act as a professional audio narrator with an authentic Old American Man voice (65-75 years old). ")
                append("Character: Weathered, rugged, warm, raspy American English accent, experienced frontier storyteller. ")
            }
            append("Style: ${params.subStyle}. Pace: $speedDesc. Pitch: $pitchDesc. Emotion: $emotionDesc. ")
            append("Pauses: $pauseDesc. Intensity: ${params.intensity}. Language: ${params.language}. ")
            append("Never sound robotic, tinny, or exaggerated. Do not include any spoken intro, outro, or meta comments. ")
            append("Only speak the following script aloud with clear, vivid articulation:\n\n")
            append(text)
        }
    }

    private fun parseAudioBase64(responseJson: String): String? {
        return try {
            val root = JSONObject(responseJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val inlineData = part.optJSONObject("inlineData")
                if (inlineData != null) {
                    val data = inlineData.optString("data")
                    if (data.isNotBlank()) return data
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseErrorMessage(responseJson: String, statusCode: Int): String {
        return try {
            val root = JSONObject(responseJson)
            val error = root.optJSONObject("error")
            error?.optString("message") ?: "HTTP $statusCode error"
        } catch (_: Exception) {
            "HTTP $statusCode error"
        }
    }

    private fun mapException(e: Exception): Exception {
        return when (e) {
            is TtsException -> e
            is UnknownHostException -> TtsException.NetworkException(e)
            is SocketTimeoutException -> TtsException.TimeoutException()
            is IOException -> TtsException.NetworkException(e)
            else -> TtsException.SynthesisException(e.localizedMessage ?: "Unexpected synthesis error")
        }
    }
}
