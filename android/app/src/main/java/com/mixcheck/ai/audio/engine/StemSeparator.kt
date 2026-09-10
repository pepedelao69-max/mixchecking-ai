
package com.mixcheck.ai.audio.engine

/**
 * Interfaz para separación de fuentes - Fase 3
 * Arquitectura permite Demucs, MDX, Open-Unmix
 * Todo etiquetado como ESTIMACIÓN con confianza
 */

enum class StemType { VOCALS, BASS, DRUMS, GUITAR, PIANO, ACCORDION, OTHER }

data class SeparatedStem(
    val type: StemType,
    val pcmData: FloatArray,
    val confidence: Double,
    val isEstimate: Boolean = true,
    val note: String = "Estimación automática, no medición exacta"
)

interface StemSeparator {
    suspend fun separate(mono: FloatArray, sampleRate: Int, onProgress: (Float)->Unit): List<SeparatedStem>
    fun isAvailable(): Boolean
}

// Implementación local dummy para MVP - en Fase 3 real usar backend con Demucs
class DummyStemSeparator : StemSeparator {
    override suspend fun separate(mono: FloatArray, sampleRate: Int, onProgress: (Float)->Unit): List<SeparatedStem> {
        onProgress(0.5f)
        // No separación real en MVP local, retornar vacío con nota
        onProgress(1f)
        return emptyList()
    }
    override fun isAvailable(): Boolean = false
}

// Backend separator - llama a /api/separate
class BackendStemSeparator(private val backendUrl: String) : StemSeparator {
    override suspend fun separate(mono: FloatArray, sampleRate: Int, onProgress: (Float)->Unit): List<SeparatedStem> {
        // TODO: subir archivo a backend, backend usa torch + demucs
        // POST /api/separate con archivo
        // Retornar stems con confidence
        onProgress(1f)
        return emptyList()
    }
    override fun isAvailable(): Boolean = true
}
