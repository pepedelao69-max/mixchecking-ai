
package com.mixcheck.ai.audio.models

data class AudioFileInfo(
    val fileName: String,
    val fileSizeBytes: Long,
    val format: String, // WAV, MP3, FLAC...
    val mimeType: String?,
    val sampleRate: Int,
    val channels: Int,
    val bitDepth: Int?, // null si lossy
    val durationMs: Long,
    val durationSeconds: Double,
    val isValid: Boolean,
    val validationError: String? = null,
    val hasClippingRisk: Boolean = false,
    val hasSilence: Boolean = false,
    val hasDcOffset: Boolean = false
)

enum class AudioFormat { WAV, MP3, FLAC, M4A, AAC, AIFF, OGG, UNKNOWN }
