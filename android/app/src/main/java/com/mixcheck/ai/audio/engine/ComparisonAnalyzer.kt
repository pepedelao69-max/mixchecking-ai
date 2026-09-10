
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.*

class ComparisonAnalyzer {

    fun compare(target: AnalysisResult, reference: AnalysisResult): ComparisonResult {
        val loudDiff = target.loudness.integratedLufs - reference.loudness.integratedLufs
        val tpDiff = target.loudness.truePeakDbTp - reference.loudness.truePeakDbTp

        val spectralDiffs = target.frequency.bands.mapIndexed { idx, band ->
            val refBand = reference.frequency.bands.getOrNull(idx)
            val diff = if (refBand != null) band.relativeEnergyDb - refBand.relativeEnergyDb else 0.0
            val desc = when {
                diff > 2 -> "Tu mezcla presenta ${"%.1f".format(diff)} dB más energía en ${band.name} que la referencia"
                diff < -2 -> "Tu mezcla presenta ${"%.1f".format(-diff)} dB menos energía en ${band.name} que la referencia"
                else -> "Similar a referencia en ${band.name}"
            }
            SpectralDiff(band.name, diff, desc)
        }

        val summary = buildString {
            append("Comparación: ")
            append("LUFS diff ${"%.1f".format(loudDiff)} dB, ")
            append("TruePeak diff ${"%.1f".format(tpDiff)} dB. ")
            val lowDiff = spectralDiffs.find { it.band=="Graves" }?.diffDb ?: 0.0
            if (lowDiff > 2) append("Exceso de graves vs referencia. ")
            val presDiff = spectralDiffs.find { it.band=="Presencia" }?.diffDb ?: 0.0
            if (presDiff < -2) append("Menos presencia 2-5kHz que referencia. ")
        }

        return ComparisonResult(target, reference, loudDiff, tpDiff, spectralDiffs, summary)
    }
}
