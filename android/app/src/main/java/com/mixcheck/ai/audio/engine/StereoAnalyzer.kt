
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.*
import kotlin.math.abs
import kotlin.math.sqrt

class StereoAnalyzer {

    fun analyze(interleaved: FloatArray, channels: Int): StereoAnalysis {
        if (channels < 2 || interleaved.size < 2) {
            return StereoAnalysis(
                correlation = 1.0,
                monoCompatibility = QualityLabel.EXCELENTE,
                width = 0.0,
                balanceLrDb = 0.0,
                balanceLabel = "Mono",
                phaseIssues = emptyList(),
                quality = QualityLabel.EXCELENTE
            )
        }

        val left = FloatArray(interleaved.size / 2) { interleaved[it*2] }
        val right = FloatArray(interleaved.size / 2) { interleaved[it*2+1] }

        // Correlacion
        val correlation = correlation(left, right)

        // Balance L/R en dB
        val rmsL = sqrt(left.map { it*it }.average())
        val rmsR = sqrt(right.map { it*it }.average())
        val balanceDb = if (rmsL>0 && rmsR>0) 20* kotlin.math.log10(rmsL/rmsR) else 0.0
        val balanceLabel = when {
            abs(balanceDb) < 0.5 -> "Centrado"
            balanceDb > 3 -> "Desbalanceado a Izquierda (${String.format("%.1f", balanceDb)} dB)"
            balanceDb < -3 -> "Desbalanceado a Derecha (${String.format("%.1f", balanceDb)} dB)"
            balanceDb > 0 -> "Ligeramente a izquierda"
            else -> "Ligeramente a derecha"
        }

        // Width: 1 - correlation aproximado + mid/side ratio
        val mid = FloatArray(left.size) { (left[it]+right[it])*0.5f }
        val side = FloatArray(left.size) { (left[it]-right[it])*0.5f }
        val rmsMid = sqrt(mid.map { it*it }.average())
        val rmsSide = sqrt(side.map { it*it }.average())
        val width = if (rmsMid+ rmsSide >0) (rmsSide / (rmsMid+rmsSide)).coerceIn(0.0,1.0) * 2.0 else 0.0

        val phaseIssues = mutableListOf<String>()
        if (correlation < -0.1) phaseIssues.add("Correlacion estereo negativa ($correlation). Riesgo alto de cancelacion en mono. Verifica elementos con panorama extremo.")
        if (correlation in -0.1..0.2) phaseIssues.add("Correlacion baja ($correlation). Compatibilidad mono limitada.")
        if (abs(balanceDb) > 3) phaseIssues.add("Desbalance L/R significativo: %.1f dB".format(balanceDb))
        if (width > 1.4) phaseIssues.add("Ancho estereo excesivo puede causar problemas de fase.")

        val quality = when {
            correlation < 0 -> QualityLabel.PROBLEMA
            correlation < 0.2 -> QualityLabel.ATENCION
            abs(balanceDb) > 3 -> QualityLabel.ATENCION
            correlation > 0.7 && abs(balanceDb) <1.5 -> QualityLabel.EXCELENTE
            else -> QualityLabel.BUENO
        }

        val monoCompat = when {
            correlation < 0 -> QualityLabel.PROBLEMA
            correlation < 0.2 -> QualityLabel.ATENCION
            correlation < 0.5 -> QualityLabel.BUENO
            else -> QualityLabel.EXCELENTE
        }

        return StereoAnalysis(
            correlation = correlation,
            monoCompatibility = monoCompat,
            width = width.coerceIn(0.0,2.0),
            balanceLrDb = balanceDb,
            balanceLabel = balanceLabel,
            phaseIssues = phaseIssues,
            quality = quality
        )
    }

    private fun correlation(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size || a.isEmpty()) return 1.0
        var sumA = 0.0; var sumB = 0.0
        for (i in a.indices) { sumA += a[i]; sumB += b[i] }
        val meanA = sumA / a.size
        val meanB = sumB / b.size
        var num = 0.0; var denA = 0.0; var denB = 0.0
        for (i in a.indices) {
            val da = a[i]-meanA; val db = b[i]-meanB
            num += da*db; denA += da*da; denB += db*db
        }
        return if (denA==0.0 || denB==0.0) 1.0 else num / sqrt(denA*denB)
    }
}
