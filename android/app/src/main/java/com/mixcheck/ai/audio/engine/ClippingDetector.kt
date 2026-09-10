
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.ClippingAnalysis
import kotlin.math.abs

class ClippingDetector {

    fun detect(mono: FloatArray, sampleRate: Int, truePeakDb: Double): ClippingAnalysis {
        var count = 0
        val locations = mutableListOf<Long>()
        var consecutive = 0
        val threshold = 0.999 // ~ -0.01 dBFS

        for (i in mono.indices) {
            if (abs(mono[i]) >= threshold) {
                consecutive++
                if (consecutive >= 3) {
                    count++
                    locations.add((i * 1000L) / sampleRate)
                    consecutive = 0 // evitar contar cada muestra
                }
            } else {
                consecutive = 0
            }
        }

        val hasClipping = count > 0 || truePeakDb > -0.1

        val desc = when {
            count == 0 && truePeakDb <= -1.0 -> "Sin clipping detectado. True Peak seguro."
            count in 1..5 -> "Clipping leve: $count eventos. Riesgo bajo pero revisa picos."
            count in 6..30 -> "Clipping moderado: $count eventos. Posible distorsion en conversion."
            count > 30 -> "Clipping severo: $count eventos detectados. Alta probabilidad de distorsion audible."
            truePeakDb > -0.1 -> "True Peak elevado (${"%.2f".format(truePeakDb)} dBTP). Riesgo de inter-sample clipping en codecs."
            else -> "Sin clipping digital, pero True Peak cercano a 0."
        }

        return ClippingAnalysis(
            eventCount = count,
            maxTruePeak = truePeakDb,
            hasClipping = hasClipping,
            locationsMs = locations.take(20), // max 20 para UI
            description = desc
        )
    }
}
