
package com.mixcheck.ai.audio.engine

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.mixcheck.ai.audio.models.AudioFileInfo
import com.mixcheck.ai.audio.models.AudioFormat
import java.io.File
import kotlin.math.abs

class AudioFileValidator(private val context: Context) {

    private val supportedMime = mapOf(
        "audio/wav" to AudioFormat.WAV,
        "audio/x-wav" to AudioFormat.WAV,
        "audio/mpeg" to AudioFormat.MP3,
        "audio/mp3" to AudioFormat.MP3,
        "audio/flac" to AudioFormat.FLAC,
        "audio/x-flac" to AudioFormat.FLAC,
        "audio/mp4" to AudioFormat.M4A,
        "audio/m4a" to AudioFormat.M4A,
        "audio/aac" to AudioFormat.AAC,
        "audio/x-aiff" to AudioFormat.AIFF,
        "audio/aiff" to AudioFormat.AIFF,
        "audio/ogg" to AudioFormat.OGG,
        "audio/vorbis" to AudioFormat.OGG
    )

    fun validate(uri: Uri, fileName: String): AudioFileInfo {
        try {
            val extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)
            if (extractor.trackCount == 0) {
                return invalid(fileName, 0, "No se encontro pista de audio valida")
            }
            val format = extractor.getTrackFormat(0)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L

            val detectedFormat = detectFormat(mime, fileName)

            if (detectedFormat == AudioFormat.UNKNOWN) {
                return invalid(fileName, 0, "Formato no compatible: $mime. Formatos soportados: WAV, MP3, FLAC, M4A, AAC, AIFF, OGG")
            }

            if (sampleRate < 8000 || sampleRate > 192000) {
                return invalid(fileName, 0, "Sample rate no valido: $sampleRate Hz")
            }
            if (channelCount < 1 || channelCount > 8) {
                return invalid(fileName, 0, "Numero de canales no soportado: $channelCount")
            }
            if (durationUs < 1_000_000) { // <1s
                return invalid(fileName, 0, "Archivo demasiado corto (<1s)")
            }
            if (durationUs > 600_000_000) { // >10min warning but allow
                // allow but note
            }

            val fileSize = getFileSize(uri)
            if (fileSize > 300L * 1024 * 1024) {
                return invalid(fileName, fileSize, "Archivo excede 300MB para analisis local")
            }

            // Basic signal checks will be done after decoding in AudioEngine
            extractor.release()

            return AudioFileInfo(
                fileName = fileName,
                fileSizeBytes = fileSize,
                format = detectedFormat.name,
                mimeType = mime,
                sampleRate = sampleRate,
                channels = channelCount,
                bitDepth = if (format.containsKey("bit-width")) format.getInteger("bit-width") else null,
                durationMs = durationUs / 1000,
                durationSeconds = durationUs / 1_000_000.0,
                isValid = true,
                validationError = null
            )
        } catch (e: Exception) {
            return invalid(fileName, 0, "No fue posible analizar el archivo: ${e.message}. Verifica que sea un audio valido no corrupto.")
        }
    }

    private fun detectFormat(mime: String, fileName: String): AudioFormat {
        supportedMime[mime.lowercase()]?.let { return it }
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "wav" -> AudioFormat.WAV
            "mp3" -> AudioFormat.MP3
            "flac" -> AudioFormat.FLAC
            "m4a" -> AudioFormat.M4A
            "aac" -> AudioFormat.AAC
            "aiff", "aif" -> AudioFormat.AIFF
            "ogg" -> AudioFormat.OGG
            else -> AudioFormat.UNKNOWN
        }
    }

    private fun invalid(name: String, size: Long, reason: String): AudioFileInfo {
        return AudioFileInfo(
            fileName = name, fileSizeBytes = size, format = "UNKNOWN", mimeType = null,
            sampleRate = 0, channels = 0, bitDepth = null, durationMs = 0, durationSeconds = 0.0,
            isValid = false, validationError = reason
        )
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: 0L
        } catch (e: Exception) { 0L }
    }
}
