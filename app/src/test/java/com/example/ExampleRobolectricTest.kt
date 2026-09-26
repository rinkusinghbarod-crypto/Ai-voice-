package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.tts.util.AudioChunker
import com.example.tts.util.WavHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Old Story Voice AI", appName)
    }

    @Test
    fun `audio chunker splits long script cleanly`() {
        val shortScript = "The old cowboy leaned against the split rail fence, watching the sun dip beneath the red rock mesa."
        val shortChunks = AudioChunker.chunkStory(shortScript)
        assertEquals(1, shortChunks.size)

        val longScript = buildString {
            repeat(20) { i ->
                append("Sentence number $i recounts how the western frontier was forged through harsh winters and burning sun. ")
            }
        }
        val longChunks = AudioChunker.chunkStory(longScript)
        assertTrue(longChunks.size > 1)
    }

    @Test
    fun `wav header creation has valid length and magic bytes`() {
        val header = WavHelper.createWavHeader(
            totalAudioLen = 48000,
            sampleRate = 24000,
            channels = 1,
            bitsPerSample = 16
        )
        assertEquals(44, header.size)
        assertEquals('R'.code.toByte(), header[0])
        assertEquals('I'.code.toByte(), header[1])
        assertEquals('F'.code.toByte(), header[2])
        assertEquals('F'.code.toByte(), header[3])
    }

    @Test
    fun `preview voice sample generation creates valid audio file`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val outputDir = File(context.cacheDir, "test_previews").apply { mkdirs() }
        val params = com.example.tts.model.VoiceGenerationParams(
            script = "Well now, gather round out here in these quiet hills, every whisper has a tale.",
            voiceProfile = "Old American Man",
            subStyle = "Frontier Storyteller"
        )
        val result = com.example.tts.util.AcousticNarratorEngine.generate(
            params = params,
            outputDir = outputDir,
            onProgress = { _, _ -> }
        )
        assertTrue(result.audioFile.exists())
        assertTrue(result.audioFile.length() > 1000)
        assertTrue(result.durationMs > 2000)
    }

    @Test
    fun `ensureValidAudioFile adds RIFF header to raw PCM file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testFile = File(context.cacheDir, "test_raw.pcm")
        val rawBytes = ByteArray(1024) { (it % 128).toByte() }
        testFile.writeBytes(rawBytes)

        WavHelper.ensureValidAudioFile(testFile)

        val updatedBytes = testFile.readBytes()
        assertEquals(1024 + 44, updatedBytes.size)
        assertEquals('R'.code.toByte(), updatedBytes[0])
        assertEquals('I'.code.toByte(), updatedBytes[1])
        assertEquals('F'.code.toByte(), updatedBytes[2])
        assertEquals('F'.code.toByte(), updatedBytes[3])
    }
}
