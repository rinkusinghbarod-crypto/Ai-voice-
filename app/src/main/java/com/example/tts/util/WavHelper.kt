package com.example.tts.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavHelper {

    /**
     * Concatenates multiple audio files (WAV or MP3) into a single continuous file.
     */
    fun concatenateAudioFiles(sourceFiles: List<File>, destinationFile: File): Long {
        if (sourceFiles.isEmpty()) return 0L
        if (sourceFiles.size == 1) {
            sourceFiles[0].copyTo(destinationFile, overwrite = true)
            return getAudioDurationMs(destinationFile)
        }

        // Check if files are WAV by looking at magic bytes
        val isWav = isWavFile(sourceFiles[0])

        if (isWav) {
            return concatenateWavFiles(sourceFiles, destinationFile)
        } else {
            // Concatenate as raw/MP3 stream
            FileOutputStream(destinationFile).use { out ->
                for (file in sourceFiles) {
                    FileInputStream(file).use { input ->
                        input.copyTo(out)
                    }
                }
            }
            return getAudioDurationMs(destinationFile)
        }
    }

    private fun isWavFile(file: File): Boolean {
        if (!file.exists() || file.length() < 12) return false
        return try {
            FileInputStream(file).use { input ->
                val header = ByteArray(12)
                val read = input.read(header)
                if (read >= 12) {
                    val riff = String(header, 0, 4)
                    val wave = String(header, 8, 4)
                    riff == "RIFF" && wave == "WAVE"
                } else false
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun concatenateWavFiles(sourceFiles: List<File>, destinationFile: File): Long {
        var headerBytes: ByteArray? = null
        var totalPcmBytes: Long = 0
        var sampleRate = 24000
        var channels = 1
        var bitsPerSample = 16
        var byteRate = sampleRate * channels * (bitsPerSample / 8)

        // First pass: extract header from first file and count total PCM bytes
        val pcmDataChunks = mutableListOf<ByteArray>()

        for ((index, file) in sourceFiles.withIndex()) {
            if (!file.exists() || file.length() <= 44) continue

            FileInputStream(file).use { input ->
                val fullBytes = input.readBytes()
                val dataOffset = findDataChunkOffset(fullBytes)

                if (index == 0) {
                    val buffer = ByteBuffer.wrap(fullBytes).order(ByteOrder.LITTLE_ENDIAN)
                    channels = buffer.getShort(22).toInt()
                    sampleRate = buffer.getInt(24)
                    byteRate = buffer.getInt(28)
                    bitsPerSample = buffer.getShort(34).toInt()
                    headerBytes = fullBytes.copyOfRange(0, dataOffset)
                }

                val pcmLength = fullBytes.size - dataOffset
                if (pcmLength > 0) {
                    val pcmBytes = fullBytes.copyOfRange(dataOffset, fullBytes.size)
                    pcmDataChunks.add(pcmBytes)
                    totalPcmBytes += pcmBytes.size
                }
            }
        }

        // Build new combined WAV
        FileOutputStream(destinationFile).use { out ->
            // Write RIFF header
            val header = createWavHeader(
                totalAudioLen = totalPcmBytes,
                sampleRate = sampleRate.toLong(),
                channels = channels,
                bitsPerSample = bitsPerSample
            )
            out.write(header)

            // Write all PCM chunks
            for (chunk in pcmDataChunks) {
                out.write(chunk)
            }
        }

        val effectiveByteRate = if (byteRate > 0) byteRate else (sampleRate * channels * (bitsPerSample / 8))
        return if (effectiveByteRate > 0) {
            (totalPcmBytes * 1000L) / effectiveByteRate
        } else {
            0L
        }
    }

    private fun findDataChunkOffset(wavBytes: ByteArray): Int {
        // Find "data" chunk
        for (i in 12 until wavBytes.size - 8) {
            if (wavBytes[i] == 'd'.code.toByte() &&
                wavBytes[i + 1] == 'a'.code.toByte() &&
                wavBytes[i + 2] == 't'.code.toByte() &&
                wavBytes[i + 3] == 'a'.code.toByte()
            ) {
                return i + 8 // 4 bytes for "data" + 4 bytes for chunk length
            }
        }
        return 44 // default standard fallback
    }

    fun createWavHeader(
        totalAudioLen: Long,
        sampleRate: Long = 24000,
        channels: Int = 1,
        bitsPerSample: Int = 16
    ): ByteArray {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF/WAVE header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        buffer.putInt(4, totalDataLen.toInt())
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // 'fmt ' chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        buffer.putInt(16, 16) // Subchunk1Size for PCM
        buffer.putShort(20, 1) // AudioFormat: 1 for PCM
        buffer.putShort(22, channels.toShort())
        buffer.putInt(24, sampleRate.toInt())
        buffer.putInt(28, byteRate.toInt())
        buffer.putShort(32, blockAlign)
        buffer.putShort(34, bitsPerSample.toShort())

        // 'data' chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        buffer.putInt(40, totalAudioLen.toInt())

        return header
    }

    fun getAudioDurationMs(file: File): Long {
        if (!file.exists() || file.length() <= 44L) return 0L
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            val parsed = durationStr?.toLongOrNull() ?: 0L
            if (parsed > 0) parsed else calculateDurationFromFile(file)
        } catch (_: Exception) {
            calculateDurationFromFile(file)
        }
    }

    /**
     * Ensures an audio file has a valid container (WAV or MP3). If it is raw PCM without headers,
     * prepends a proper 24kHz 16-bit mono RIFF header so MediaPlayer can decode it.
     */
    fun ensureValidAudioFile(file: File) {
        if (!file.exists() || file.length() < 4) return
        try {
            val bytes = file.readBytes()
            if (bytes.size < 4) return

            val isRiff = bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
                    bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte()
            val isMp3 = (bytes[0] == 'I'.code.toByte() && bytes[1] == 'D'.code.toByte() && bytes[2] == '3'.code.toByte()) ||
                    (bytes[0] == 0xFF.toByte() && (bytes[1].toInt() and 0xE0) == 0xE0)

            if (!isRiff && !isMp3) {
                val header = createWavHeader(
                    totalAudioLen = bytes.size.toLong(),
                    sampleRate = 24000L,
                    channels = 1,
                    bitsPerSample = 16
                )
                val fixed = ByteArray(header.size + bytes.size)
                System.arraycopy(header, 0, fixed, 0, header.size)
                System.arraycopy(bytes, 0, fixed, header.size, bytes.size)
                file.writeBytes(fixed)
            }
        } catch (_: Exception) {
        }
    }

    private fun calculateDurationFromFile(file: File): Long {
        return try {
            val length = file.length()
            if (length <= 44L) return 0L
            // Assuming 24kHz 16-bit mono (48,000 bytes/sec)
            ((length - 44L) * 1000L) / 48000L
        } catch (_: Exception) {
            0L
        }
    }
}
