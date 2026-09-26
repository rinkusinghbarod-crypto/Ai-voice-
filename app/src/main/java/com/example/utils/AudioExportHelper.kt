package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object AudioExportHelper {

    fun shareAudio(context: Context, filePath: String, title: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "Audio file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val mimeType = if (file.name.endsWith(".mp3", ignoreCase = true)) "audio/mp3" else "audio/wav"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Listen to \"$title\" narrated by Old Story Voice AI.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Story Narration")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share audio: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun saveAudioToDevice(context: Context, filePath: String, suggestedName: String): Result<String> {
        val sourceFile = File(filePath)
        if (!sourceFile.exists()) {
            return Result.failure(IllegalArgumentException("Source audio file does not exist"))
        }

        val extension = if (sourceFile.name.endsWith(".mp3", ignoreCase = true)) "mp3" else "wav"
        val mimeType = if (extension == "mp3") "audio/mp3" else "audio/wav"

        val sanitizedTitle = suggestedName
            .replace(Regex("[^a-zA-Z0-9._ -]"), "_")
            .take(40)
            .ifBlank { "old_story_narration" }

        val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}.$extension"

        // Strategy 1: Android 10+ MediaStore Downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/OldStoryVoice")
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    return Result.success("Saved to Downloads/OldStoryVoice/$fileName")
                }
            } catch (_: Exception) {
                // Fallback to MediaStore Audio
            }

            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/OldStoryVoice")
                    put(MediaStore.Audio.Media.IS_MUSIC, 1)
                }

                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(sourceFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    return Result.success("Saved to Music/OldStoryVoice/$fileName")
                }
            } catch (_: Exception) {
            }
        }

        // Strategy 2: Direct public Downloads directory
        try {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, "OldStoryVoice")
            if (!targetDir.exists()) targetDir.mkdirs()

            val targetFile = File(targetDir, fileName)
            sourceFile.copyTo(targetFile, overwrite = true)

            // Trigger MediaScanner so file is indexed
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf(mimeType),
                null
            )

            return Result.success("Saved to Downloads/OldStoryVoice/$fileName")
        } catch (_: Exception) {
        }

        // Strategy 3: App's external files directory (always accessible without permission)
        try {
            val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val targetFile = File(externalDir, fileName)
            sourceFile.copyTo(targetFile, overwrite = true)
            return Result.success("Saved to ${targetFile.name}")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
