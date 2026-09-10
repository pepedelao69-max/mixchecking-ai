
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.AnalysisResult
import com.mixcheck.ai.audio.models.Severity

data class StreamingIssue(val platform: String, val issue: String, val severity: Severity)

class StreamingSimulator {

    fun simulate(result: AnalysisResult): List<StreamingIssue> {
        val issues = mutableListOf<StreamingIssue>()

        // Simulación de codificación AAC/Opus - no afirma replicar exactamente Spotify/Apple
        if (result.loudness.truePeakDbTp > -1) {
            issues.add(StreamingIssue("Spotify/Apple/YouTube", "True Peak ${"%.2f".format(result.loudness.truePeakDbTp)} dBTP provocará clipping inter-sample tras codificación AAC/Ogg Vorbis. Simulación de codificación indica riesgo de distorsión.", Severity.HIGH))
        }

        if (result.loudness.integratedLufs > -9) {
            issues.add(StreamingIssue("Spotify", "Loudness ${"%.1f".format(result.loudness.integratedLufs)} LUFS será reducido por normalización a -14 LUFS. Pierdes impacto si ya estás muy comprimido.", Severity.MEDIUM))
            issues.add(StreamingIssue("Apple Music", "Apple normaliza a -16 LUFS con Sound Check. Tu mezcla a ${"%.1f".format(result.loudness.integratedLufs)} LUFS será bajada.", Severity.LOW))
        }

        if (result.clipping.hasClipping) {
            issues.add(StreamingIssue("YouTube", "Clipping detectado ${result.clipping.eventCount} eventos. YouTube transcodifica a Opus/AAC y amplificará distorsión.", Severity.HIGH))
        }

        if (result.stereo.correlation < 0.2) {
            issues.add(StreamingIssue("Mono Playback", "Correlación ${"%.2f".format(result.stereo.correlation)} - dispositivos mono (algunos smart speakers) tendrán cancelaciones.", Severity.MEDIUM))
        }

        return issues
    }
}
