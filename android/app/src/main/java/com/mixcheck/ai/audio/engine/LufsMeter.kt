
package com.mixcheck.ai.audio.engine

import kotlin.math.*
import com.mixcheck.ai.audio.models.LoudnessMetrics

/**
 * Implementacion ITU-R BS.1770-4 simplificada pero funcional para MVP
 * K-weighting: high-shelf + high-pass biquad
 * Bloques 400ms con 75% overlap, gating absoluto -70 LUFS y relativo -10 LU
 * True Peak via 4x oversampling lineal (MVP) -> mejorar a sinc en Fase2
 */
class LufsMeter {

    // Biquad filter coefficients for K-weighting (48kHz reference, recalcular segun sampleRate en prod)
    // Esta es una version simplificada; en produccion usar filtros IIR precisos
    private class Biquad(val b0: Double, val b1: Double, val b2: Double, val a1: Double, val a2: Double) {
        var z1 = 0.0; var z2 = 0.0
        fun process(x: Double): Double {
            val out = b0 * x + z1
            z1 = b1 * x - a1 * out + z2
            z2 = b2 * x - a2 * out
            return out
        }
        fun reset() { z1=0.0; z2=0.0 }
    }

    fun createKWeightingFilters(sampleRate: Int): Pair<Biquad, Biquad> {
        // High-shelf +4dB @ 1681Hz Q 0.707 (ITU spec) - coeficientes para 48k aproximados
        // Para MVP usamos valores calculados para 48k y 44.1k; en prod calcular dinamicamente
        // Aqui simplificado: pre-emphasis
        // Nota: esto es funcional pero no certificacion EBU, suficiente para MVP profesional
        val highShelf = Biquad(1.53512485958697, -2.69169618940638, 1.19839281085285, -1.69065929318241, 0.73245863656304)
        val highPass = Biquad(1.0, -2.0, 1.0, -1.99004745483398, 0.99007225036621)
        return Pair(highShelf, highPass)
    }

    fun measure(samples: FloatArray, sampleRate: Int, channels: Int): LoudnessMetrics {
        // Si estereo, mezclar a mono para loudness segun ITU (G: L+R) pero con pesos
        val mono = if (channels == 2) {
            FloatArray(samples.size / 2) { i -> (samples[i*2] + samples[i*2+1]) * 0.5f }
        } else if (channels == 1) samples else {
            // multicanal: promedio canales
            FloatArray(samples.size / channels) { idx ->
                var sum = 0f
                for (c in 0 until channels) sum += samples[idx*channels + c]
                sum / channels
            }
        }

        // Aplicar K-weighting
        val (shelf, hp) = createKWeightingFilters(sampleRate)
        val filtered = DoubleArray(mono.size) { i ->
            var x = mono[i].toDouble()
            x = shelf.process(x)
            x = hp.process(x)
            x
        }

        // Bloques 400ms, 100ms hop (75% overlap)
        val blockSize = (sampleRate * 0.4).toInt()
        val hop = (sampleRate * 0.1).toInt()
        val blockLoudness = mutableListOf<Double>()

        var maxMomentary = -70.0
        var maxShort = -70.0

        // Momentary 400ms, Short 3s
        val shortBlocksNeeded = (3.0 / 0.4).toInt()

        var idx = 0
        while (idx + blockSize < filtered.size) {
            val block = filtered.sliceArray(idx until idx+blockSize)
            val meanSquare = block.map { it*it }.average()
            if (meanSquare > 0) {
                val loud = -0.691 + 10 * log10(meanSquare) // ITU formula
                blockLoudness.add(loud)
                if (loud > maxMomentary) maxMomentary = loud
            }
            idx += hop
        }

        // Gating absoluto -70
        val absoluteGated = blockLoudness.filter { it >= -70.0 }
        if (absoluteGated.isEmpty()) {
            return LoudnessMetrics(-70.0, -70.0, -70.0, -70.0, 0.0, -20.0, -20.0, -70.0, 0.0, 0.0)
        }

        // Gating relativo: promedio absoluto -> -10 LU
        val avgAbsolute = absoluteGated.let {
            val meanSquare = it.map { l -> 10.0.pow((l + 0.691)/10.0) }.average()
            -0.691 + 10*log10(meanSquare)
        }
        val relativeThreshold = avgAbsolute - 10.0
        val relativeGated = absoluteGated.filter { it >= relativeThreshold }

        val integrated = if (relativeGated.isEmpty()) avgAbsolute else {
            val ms = relativeGated.map { l -> 10.0.pow((l + 0.691)/10.0) }.average()
            -0.691 + 10*log10(ms)
        }

        // LRA: diferencia percentil 10 y 95 de short-term
        // Short-term: 3s sliding
        val shortTermLoudness = mutableListOf<Double>()
        val shortSize = sampleRate * 3
        var sIdx = 0
        while (sIdx + shortSize < filtered.size) {
            val b = filtered.sliceArray(sIdx until sIdx+shortSize)
            val ms = b.map { it*it }.average()
            if (ms>0) shortTermLoudness.add(-0.691 + 10*log10(ms))
            sIdx += hop
        }
        val sorted = shortTermLoudness.filter { it >= relativeThreshold }.sorted()
        val lra = if (sorted.size >= 2) {
            val p10 = sorted[(sorted.size * 0.10).toInt().coerceIn(0, sorted.size-1)]
            val p95 = sorted[(sorted.size * 0.95).toInt().coerceIn(0, sorted.size-1)]
            p95 - p10
        } else 0.0

        val stMax = shortTermLoudness.maxOrNull() ?: integrated
        val stMin = shortTermLoudness.minOrNull() ?: integrated

        // RMS, Peak, Crest
        val rms = sqrt(mono.map { it*it }.average().toDouble())
        val rmsDb = if (rms>0) 20*log10(rms) else -100.0
        val peak = mono.maxOf { abs(it.toDouble()) }
        val peakDb = if (peak>0) 20*log10(peak) else -100.0
        val crest = peakDb - rmsDb

        // True Peak (MVP: max sample + oversampling simple 4x linear)
        val truePeak = calculateTruePeak(mono, sampleRate)

        return LoudnessMetrics(
            integratedLufs = integrated,
            shortTermMaxLufs = stMax,
            shortTermMinLufs = stMin,
            momentaryMaxLufs = maxMomentary,
            loudnessRangeLra = lra,
            truePeakDbTp = truePeak,
            samplePeakDb = peakDb,
            rmsDb = rmsDb,
            crestFactorDb = crest,
            dynamicRangeDb = lra // aproximado para MVP
        )
    }

    private fun calculateTruePeak(mono: FloatArray, sr: Int): Double {
        // 4x oversampling lineal simple para MVP
        var max = 0.0
        for (i in 0 until mono.size-1) {
            val s0 = mono[i].toDouble()
            val s1 = mono[i+1].toDouble()
            max = maxOf(max, abs(s0))
            // interpolacion lineal 4x
            for (k in 1..3) {
                val interp = s0 + (s1 - s0) * k / 4.0
                max = maxOf(max, abs(interp))
            }
        }
        return if (max>0) 20*log10(max) else -100.0
    }
}
