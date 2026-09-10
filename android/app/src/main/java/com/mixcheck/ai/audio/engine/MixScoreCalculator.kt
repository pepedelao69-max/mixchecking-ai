
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.*
import kotlin.math.abs

class MixScoreCalculator {

    fun calculate(
        loudness: LoudnessMetrics,
        freq: FrequencyAnalysis,
        stereo: StereoAnalysis,
        clipping: ClippingAnalysis,
        fileInfo: AudioFileInfo
    ): MixScores {

        // Loudness score: ideal integrated -14 a -8 LUFS para mezcla pre-master, true peak < -1
        val loudnessScore = run {
            var s = 100
            val lufs = loudness.integratedLufs
            if (lufs < -20) s -= (-20 - lufs).toInt()*3
            if (lufs > -6) s -= (lufs - (-6)).toInt()*8
            if (loudness.truePeakDbTp > -1) s -= ((loudness.truePeakDbTp +1)*20).toInt().coerceAtLeast(0)
            if (loudness.truePeakDbTp > 0) s -= 20
            s.coerceIn(0,100)
        }

        val dynamicsScore = run {
            var s = 100
            if (loudness.loudnessRangeLra < 3) s -= (3 - loudness.loudnessRangeLra).toInt()*15
            if (loudness.crestFactorDb < 6) s -= (6 - loudness.crestFactorDb).toInt()*5
            if (loudness.crestFactorDb > 18) s -= 10 // muy dinamico para streaming moderno? penalizacion leve
            s.coerceIn(0,100)
        }

        val freqScore = run {
            var s = 100
            s -= freq.accumulations.sumOf {
                when(it.severity){
                    Severity.LOW -> 3; Severity.MEDIUM -> 8; Severity.HIGH -> 15; Severity.CRITICAL -> 25
                }
            }
            s -= freq.holes.size * 5
            s.coerceIn(0,100)
        }

        val stereoScore = run {
            var s = 100
            if (stereo.correlation < 0) s -= 40
            else if (stereo.correlation < 0.2) s -= 20
            else if (stereo.correlation < 0.4) s -= 10
            s -= abs(stereo.balanceLrDb).toInt()*3
            s.coerceIn(0,100)
        }

        val phaseScore = when (stereo.monoCompatibility) {
            QualityLabel.EXCELENTE -> 95
            QualityLabel.BUENO -> 85
            QualityLabel.ATENCION -> 60
            QualityLabel.PROBLEMA -> 30
        }

        val clippingScore = when {
            clipping.eventCount == 0 && clipping.maxTruePeak <= -1 -> 100
            clipping.eventCount == 0 && clipping.maxTruePeak <= -0.3 -> 85
            clipping.eventCount in 1..5 -> 70
            clipping.eventCount in 6..20 -> 45
            else -> 20
        }

        val technical = (clippingScore * 0.5 + phaseScore * 0.3 + (if (fileInfo.hasDcOffset) 60 else 95) * 0.2).toInt()

        val overall = (
            loudnessScore * 0.20 +
            dynamicsScore * 0.15 +
            freqScore * 0.25 +
            stereoScore * 0.15 +
            phaseScore * 0.10 +
            clippingScore * 0.15
        ).toInt().coerceIn(0,100)

        return MixScores(
            overall = overall,
            loudness = loudnessScore,
            dynamics = dynamicsScore,
            frequencyBalance = freqScore,
            stereo = stereoScore,
            phase = phaseScore,
            clipping = clippingScore,
            technicalQuality = technical,
            referenceMatch = null
        )
    }
}
