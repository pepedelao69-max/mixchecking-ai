
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.*
import kotlin.math.*

class SpectrumAnalyzer {

    data class BandDef(val name: String, val low: Double, val high: Double)

    private val bands = listOf(
        BandDef("Subgraves", 20.0, 60.0),
        BandDef("Graves", 60.0, 250.0),
        BandDef("Low-Mid", 250.0, 500.0),
        BandDef("Medios", 500.0, 2000.0),
        BandDef("Upper-Mid", 2000.0, 4000.0),
        BandDef("Presencia", 4000.0, 6000.0),
        BandDef("Agudos / Brillo", 6000.0, 20000.0)
    )

    fun analyze(mono: FloatArray, sampleRate: Int): FrequencyAnalysis {
        // FFT simplificada MVP: analisis por bandas via energia RMS filtrada por frecuencias aproximadas usando goertzel? 
        // Para MVP real funcional: usamos analisis espectral por ventanas y estimacion de energia por bandas con filtros biquad simples
        // Aqui implementacion basada en energia espectral aproximada con FFT casera (usaremos JTransforms en prod, aqui DFT simplificada para archivo compilable)

        // Convertir a mono ya viene mono
        // Calcular energia por banda usando filtros IIR aproximados (para MVP usamos promedio de energia espectral estimado via zero-crossing no, mejor usar energia en bandas de frecuencia via DFT rapida aproximada)

        // Para evitar dependencia nativa, usamos analisis de energia por octavas via filtros de promediado espectral:
        // Simulamos espectro con 1024 puntos usando DFT directa en segmentos y promediamos (costoso pero funcional para archivos cortos)

        val windowSize = 4096
        val hop = 2048
        val bandEnergies = DoubleArray(bands.size) { 0.0 }
        var frames = 0

        var pos = 0
        while (pos + windowSize < mono.size) {
            val window = mono.sliceArray(pos until pos+windowSize)
            // Hann
            for (i in window.indices) window[i] = (window[i] * 0.5 * (1 - cos(2*PI*i/windowSize))).toFloat()
            // Magnitud por banda aproximada: calculamos energia espectral via correlacion con senos (Goertzel simplificado)
            val spectrum = estimateSpectrum(window, sampleRate)
            // spectrum es energia por bin
            for (bIdx in bands.indices) {
                val b = bands[bIdx]
                var e = 0.0
                // sumar bins que caen en banda
                for (i in spectrum.indices) {
                    val freq = i * sampleRate.toDouble() / windowSize
                    if (freq in b.low..b.high) e += spectrum[i]
                }
                bandEnergies[bIdx] += e
            }
            frames++
            pos += hop
        }
        if (frames==0) frames=1
        val avgEnergies = bandEnergies.map { it / frames }
        val total = avgEnergies.sum().coerceAtLeast(1e-12)
        val dbBands = avgEnergies.map { 10*log10(it/total + 1e-12) }

        val freqBands = bands.mapIndexed { idx, def ->
            val rel = dbBands[idx]
            FrequencyBand(def.name, Pair(def.low, def.high), 20*log10(avgEnergies[idx]+1e-12), rel)
        }

        // Detectar acumulaciones: banda con energia > +6dB sobre promedio vecinos
        val accumulations = mutableListOf<Accumulation>()
        val holes = mutableListOf<Hole>()
        for (i in freqBands.indices) {
            val neighbors = listOfNotNull(
                freqBands.getOrNull(i-1)?.relativeEnergyDb,
                freqBands.getOrNull(i+1)?.relativeEnergyDb
            ).average().let { if (it.isNaN()) freqBands[i].relativeEnergyDb else it }
            val diff = freqBands[i].relativeEnergyDb - neighbors
            if (diff > 4.5) {
                val sev = when {
                    diff > 8 -> Severity.CRITICAL
                    diff > 6 -> Severity.HIGH
                    else -> Severity.MEDIUM
                }
                accumulations.add(Accumulation(freqBands[i].rangeHz, sev, "Concentracion elevada en ${freqBands[i].name} (${freqBands[i].rangeHz.first.toInt()}-${freqBands[i].rangeHz.second.toInt()} Hz): +${"%.1f".format(diff)} dB sobre promedio. Puede generar mezcla turbia o pesada."))
            }
            if (diff < -6) {
                holes.add(Hole(freqBands[i].rangeHz, Severity.MEDIUM, "Hueco energetico en ${freqBands[i].name}"))
            }
        }

        // Interpretacion general
        val balanceText = buildString {
            append("Balance espectral: ")
            val low = freqBands.filter { it.name in listOf("Subgraves","Graves") }.sumOf { it.relativeEnergyDb }
            val mid = freqBands.filter { it.name in listOf("Low-Mid","Medios","Upper-Mid") }.sumOf { it.relativeEnergyDb }
            val high = freqBands.filter { it.name.contains("Presencia") || it.name.contains("Agudos") }.sumOf { it.relativeEnergyDb }
            when {
                low > mid + 6 -> append("Exceso de graves. Mezcla pesada. ")
                high < mid - 8 -> append("Falta de brillo/aire. ")
                high > mid + 6 -> append("Exceso de agudos, posible fatiga auditiva. ")
                else -> append("Equilibrio general aceptable. ")
            }
            if (accumulations.isNotEmpty()) append("Se detectaron ${accumulations.size} acumulaciones.")
        }

        return FrequencyAnalysis(
            bands = freqBands,
            spectralBalance = balanceText,
            accumulations = accumulations,
            holes = holes,
            rollOffHz = null
        )
    }

    private fun estimateSpectrum(window: FloatArray, sr: Int): DoubleArray {
        // DFT magnitud simplificada para MVP (no optimizada pero funcional)
        val N = window.size
        val outSize = N/2
        val mag = DoubleArray(outSize)
        // Solo calculamos algunos bins para performance (cada 4)
        for (k in 0 until outSize step 4) {
            var re = 0.0; var im = 0.0
            for (n in 0 until N) {
                val angle = 2*PI*k*n/N
                re += window[n]*cos(angle)
                im -= window[n]*sin(angle)
            }
            mag[k] = sqrt(re*re + im*im)
            // interpolar vecinos
            if (k>0) {
                mag[k-1]=mag[k]; mag[k-2]=mag[k]; mag[k-3]=mag[k]
            }
        }
        return mag
    }
}
