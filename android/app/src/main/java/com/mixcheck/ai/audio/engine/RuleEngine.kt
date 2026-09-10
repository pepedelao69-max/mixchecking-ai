
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.*

class RuleEngine {

    fun generateIssues(
        loudness: LoudnessMetrics,
        freq: FrequencyAnalysis,
        stereo: StereoAnalysis,
        clipping: ClippingAnalysis
    ): List<Issue> {
        val issues = mutableListOf<Issue>()

        if (clipping.eventCount > 0) {
            issues.add(Issue(
                id = "clipping",
                title = "Clipping digital detectado",
                severity = if (clipping.eventCount > 20) Severity.CRITICAL else Severity.HIGH,
                evidence = "${clipping.eventCount} eventos de clipping. True Peak ${"%.2f".format(clipping.maxTruePeak)} dBTP. Ubicaciones aproximadas: ${clipping.locationsMs.take(5).joinToString()} ms",
                explanation = "Muestras consecutivas al maximo. Causa distorsion y sera amplificado por codecs de streaming.",
                recommendation = "Prueba reducir el limitador final entre 0.5 y 1 dB y verifica nuevamente. Asegura True Peak por debajo de -1 dBTP.",
                impactScore = 90
            ))
        }

        if (loudness.truePeakDbTp > -1) {
            issues.add(Issue(
                id="true_peak",
                title="True Peak elevado",
                severity = if (loudness.truePeakDbTp > 0) Severity.CRITICAL else Severity.HIGH,
                evidence = "True Peak ${"%.2f".format(loudness.truePeakDbTp)} dBTP",
                explanation = "Riesgo de distorsion inter-sample tras conversion a AAC/MP3/Ogg.",
                recommendation = "Considera ajustar el techo del limitador a -1.0 dBTP o -1.5 dBTP antes de entregar.",
                impactScore = 85
            ))
        }

        freq.accumulations.forEachIndexed { idx, acc ->
            issues.add(Issue(
                id="freq_acc_$idx",
                title="Acumulacion en ${acc.range.first.toInt()}-${acc.range.second.toInt()} Hz",
                severity = acc.severity,
                evidence = acc.description,
                explanation = "Exceso de energia en esta zona puede producir mezcla pesada, turbia o enmascaramiento entre instrumentos. En regional mexicano revisa conflicto entre voz, acordeon y bajo sexto alrededor de 1.5-3 kHz y low-mid 200-400 Hz.",
                recommendation = "Prueba reducir entre 1 y 2 dB con EQ de banda estrecha alrededor de ${(acc.range.first+acc.range.second)/2} Hz en el elemento que genera la acumulacion y vuelve a comprobar.",
                impactScore = when(acc.severity){ Severity.CRITICAL->80; Severity.HIGH->70; Severity.MEDIUM->50; Severity.LOW->30},
                frequencyRange = acc.range,
                confidence = 0.85
            ))
        }

        if (stereo.correlation < 0.2) {
            issues.add(Issue(
                id="phase",
                title="Correlacion estereo baja",
                severity = if (stereo.correlation < 0) Severity.CRITICAL else Severity.HIGH,
                evidence = "Correlacion ${"%.2f".format(stereo.correlation)}, Width ${"%.2f".format(stereo.width)}",
                explanation = "La mezcla presenta posibles cancelaciones en mono. Causado por elementos muy abiertos o con inversion de polaridad.",
                recommendation = "Verifica instrumentos ampliamente panoramizados, especialmente doblajes, reverbs y efectos. Prueba colapsar a mono y escucha perdidas.",
                impactScore = 75
            ))
        }

        if (kotlin.math.abs(stereo.balanceLrDb) > 2.5) {
            issues.add(Issue(
                id="balance",
                title="Desbalance L/R",
                severity = if (kotlin.math.abs(stereo.balanceLrDb) > 4) Severity.HIGH else Severity.MEDIUM,
                evidence = stereo.balanceLabel,
                explanation = "Energia no centrada puede indicar panoramizacion descompensada o problema de ganancia.",
                recommendation = "Revisa paneos y ganancias de buses. Busca que el balance se mantenga dentro de ±1.5 dB en secciones principales.",
                impactScore = 55
            ))
        }

        if (loudness.loudnessRangeLra < 3 && loudness.crestFactorDb < 7) {
            issues.add(Issue(
                id="dynamics",
                title="Dinamica muy comprimida",
                severity = Severity.MEDIUM,
                evidence = "LRA ${"%.1f".format(loudness.loudnessRangeLra)} LU, Crest ${"%.1f".format(loudness.crestFactorDb)} dB",
                explanation = "La mezcla puede sonar fatigante y sin impacto. Compresion excesiva en bus o limitador agresivo.",
                recommendation = "Considera reducir la compresion del bus master o aumentar ataque del compresor para recuperar transitorios.",
                impactScore = 60
            ))
        }

        if (loudness.integratedLufs > -7) {
            issues.add(Issue(
                id="loudness_high",
                title="Loudness muy alto para mezcla",
                severity = Severity.MEDIUM,
                evidence = "Integrated ${"%.1f".format(loudness.integratedLufs)} LUFS",
                explanation = "Por encima de -7 LUFS dejas poco margen para mastering. Riesgo de distorsion y perdida de dinamica.",
                recommendation = "Para mezcla pre-master apunta a -12 a -8 LUFS con True Peak -3 a -1 dBTP. Deja headroom al mastering.",
                impactScore = 50
            ))
        }

        return issues.sortedByDescending { it.impactScore }
    }

    fun top5(issues: List<Issue>): List<Issue> = issues.take(5)

    fun releaseCheck(
        loudness: LoudnessMetrics,
        clipping: ClippingAnalysis,
        stereo: StereoAnalysis,
        fileInfo: com.mixcheck.ai.audio.models.AudioFileInfo
    ): ReleaseCheckResult {
        val checks = mutableListOf<ReleaseCheck>()

        checks.add(ReleaseCheck("Clipping", !clipping.hasClipping, clipping.description, if (clipping.hasClipping) Severity.CRITICAL else Severity.LOW))
        checks.add(ReleaseCheck("True Peak", loudness.truePeakDbTp <= -1.0, "True Peak ${"%.2f".format(loudness.truePeakDbTp)} dBTP (objetivo <= -1.0)", if (loudness.truePeakDbTp > -1) Severity.HIGH else Severity.LOW))
        checks.add(ReleaseCheck("Loudness", loudness.integratedLufs in -16.0..-7.0, "Integrated ${"%.1f".format(loudness.integratedLufs)} LUFS (mezcla ideal -14 a -8)", if (loudness.integratedLufs !in -16.0..-7.0) Severity.MEDIUM else Severity.LOW))
        checks.add(ReleaseCheck("Fase", stereo.correlation >= 0.2, "Correlacion ${"%.2f".format(stereo.correlation)} (objetivo >=0.2)", if (stereo.correlation <0.2) Severity.HIGH else Severity.LOW))
        checks.add(ReleaseCheck("Balance L/R", kotlin.math.abs(stereo.balanceLrDb) <= 3, stereo.balanceLabel, if (kotlin.math.abs(stereo.balanceLrDb)>3) Severity.MEDIUM else Severity.LOW))
        checks.add(ReleaseCheck("Formato", fileInfo.sampleRate >= 44100, "Sample Rate ${fileInfo.sampleRate} Hz", Severity.LOW))
        checks.add(ReleaseCheck("Duracion", fileInfo.durationSeconds in 20.0..600.0, "Duracion ${"%.1f".format(fileInfo.durationSeconds)} s", Severity.LOW))

        val failedCritical = checks.count { !it.passed && it.severity == Severity.CRITICAL }
        val failedHigh = checks.count { !it.passed && it.severity == Severity.HIGH }

        val status = when {
            failedCritical >0 -> ReleaseStatus.NOT_READY
            failedHigh >0 -> ReleaseStatus.REVIEW
            checks.any { !it.passed } -> ReleaseStatus.REVIEW
            else -> ReleaseStatus.READY
        }

        val explanation = when(status){
            ReleaseStatus.READY -> "La mezcla cumple los criterios tecnicos basicos para entrega."
            ReleaseStatus.REVIEW -> "Hay advertencias que conviene revisar antes de entregar."
            ReleaseStatus.NOT_READY -> "Se detectaron problemas criticos. Corrige antes de distribuir."
        }

        return ReleaseCheckResult(status, checks, explanation)
    }
}
