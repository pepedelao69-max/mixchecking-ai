
package com.mixcheck.ai.audio.engine

import com.mixcheck.ai.audio.models.*

/**
 * Perfil especializado Regional Mexicano
 * Norteño, Banda, Sierreño, Corridos, Mariachi
 * Elementos: acordeon, bajo sexto, bajo, tololoche, bateria, tarola, tambora, sax, guitarras, voz
 * No reglas rigidas, deteccion de conflictos de frecuencia posibles
 */
class RegionalMexicanoProfile {

    data class Conflict(val instruments: List<String>, val range: Pair<Double,Double>, val description: String)

    fun analyze(freq: FrequencyAnalysis, stereo: StereoAnalysis): List<Issue> {
        val issues = mutableListOf<Issue>()

        // Conflicto tipico 1: low-mid 200-350 Hz entre bajo sexto, tololoche, bombo
        val lowMidAcc = freq.accumulations.filter { it.range.first in 150.0..400.0 }
        if (lowMidAcc.isNotEmpty()) {
            issues.add(Issue(
                id="regional_lowmid",
                title="Posible acumulacion low-mid (caracteristica regional)",
                severity = Severity.MEDIUM,
                evidence = "Energia elevada 180-350 Hz. Comun en bajo sexto + tololoche/bajo + bombo.",
                explanation = "En regional mexicano esta zona define cuerpo. Exceso produce mezcla pesada/barrosa, falta de definicion en bajo y voz.",
                recommendation = "Verifica con EQ sidechain o corte sutil: prueba atenuar 1-1.5 dB en 220-300 Hz en bajo sexto o guitarras, dejando protagonismo a bajo/tololoche. Compara en mono.",
                impactScore = 65,
                frequencyRange = Pair(180.0, 350.0),
                confidence = 0.75
            ))
        }

        // Conflicto 1.5-3kHz voz vs acordeon vs bajo sexto
        val presenceAcc = freq.accumulations.filter { it.range.first in 1200.0..3500.0 }
        if (presenceAcc.isNotEmpty()) {
            issues.add(Issue(
                id="regional_presence",
                title="Posible conflicto voz / acordeon / bajo sexto en medios",
                severity = Severity.MEDIUM,
                evidence = "Concentracion ${presenceAcc.firstOrNull()?.range?.let { "${it.first.toInt()}-${it.second.toInt()} Hz" } ?: "1.5-3 kHz"}",
                explanation = "Rango critico para inteligibilidad de voz y brillo de acordeon y bajo sexto. Acumulacion causa enmascaramiento.",
                recommendation = "Prueba tecnica de separacion frecuencial: voz principal centrada con boost sutil 2.5-3.5 kHz, acordeon ligeramente atenuado en esa zona y con paneo ligero. No apliques regla rigida, escucha contexto.",
                impactScore = 70,
                frequencyRange = Pair(1500.0, 3000.0),
                confidence = 0.70
            ))
        }

        // Tarola / tambora ataque 4-6k
        val highMid = freq.bands.find { it.name=="Presencia" }
        if (highMid != null && highMid.relativeEnergyDb > 2) {
            issues.add(Issue(
                id="regional_transients",
                title="Transitorios de tarola/tambora prominentes",
                severity = Severity.LOW,
                evidence = "Presencia elevada ${"%.1f".format(highMid.relativeEnergyDb)} dB relativa",
                explanation = "Puede ser intencional en banda/sierreño para pegada, pero exceso genera fatiga.",
                recommendation = "Si es intencional, manten. Si no, considera transient shaper o EQ dinamico en 4-6 kHz.",
                impactScore = 35,
                frequencyRange = Pair(4000.0, 6000.0),
                confidence = 0.6
            ))
        }

        return issues
    }
}
