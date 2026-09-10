
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.AnalysisResult

data class RecheckDiff(
    val lufsChange: Double,
    val truePeakChange: Double,
    val dynamicsChange: Double,
    val freqScoreChange: Int,
    val stereoScoreChange: Int,
    val overallChange: Int,
    val improvements: List<String>,
    val regressions: List<String>
)

class RecheckManager {

    fun compare(v1: AnalysisResult, v2: AnalysisResult): RecheckDiff {
        val lufsChange = v2.loudness.integratedLufs - v1.loudness.integratedLufs
        val tpChange = v2.loudness.truePeakDbTp - v1.loudness.truePeakDbTp
        val dynChange = v2.loudness.loudnessRangeLra - v1.loudness.loudnessRangeLra
        val freqChange = v2.scores.frequencyBalance - v1.scores.frequencyBalance
        val stereoChange = v2.scores.stereo - v1.scores.stereo
        val overallChange = v2.scores.overall - v1.scores.overall

        val improvements = mutableListOf<String>()
        val regressions = mutableListOf<String>()

        if (v2.clipping.eventCount < v1.clipping.eventCount) improvements.add("Mejoró clipping: ${v1.clipping.eventCount} → ${v2.clipping.eventCount} eventos")
        if (v2.clipping.eventCount > v1.clipping.eventCount) regressions.add("Empeoró clipping: ${v1.clipping.eventCount} → ${v2.clipping.eventCount}")

        v1.frequency.accumulations.forEach { acc1 ->
            val stillPresent = v2.frequency.accumulations.any { it.range == acc1.range }
            if (!stillPresent) improvements.add("Mejoró acumulación ${acc1.range.first.toInt()}-${acc1.range.second.toInt()}Hz")
        }

        if (freqChange > 5) improvements.add("Mejoró balance frecuencial +$freqChange")
        if (freqChange < -5) regressions.add("Empeoró balance frecuencial $freqChange")

        if (v2.loudness.truePeakDbTp <= -1 && v1.loudness.truePeakDbTp > -1) improvements.add("True Peak ahora seguro <= -1 dBTP")
        if (v2.scores.dynamics < v1.scores.dynamics - 5) regressions.add("Dinámica más comprimida: LRA ${"%.1f".format(v1.loudness.loudnessRangeLra)} → ${"%.1f".format(v2.loudness.loudnessRangeLra)}")

        return RecheckDiff(lufsChange, tpChange, dynChange, freqChange, stereoChange, overallChange, improvements, regressions)
    }
}
