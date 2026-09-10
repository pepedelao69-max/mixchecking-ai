
package com.mixcheck.ai.audio.models

data class LoudnessMetrics(
    val integratedLufs: Double,
    val shortTermMaxLufs: Double,
    val shortTermMinLufs: Double,
    val momentaryMaxLufs: Double,
    val loudnessRangeLra: Double,
    val truePeakDbTp: Double,
    val samplePeakDb: Double,
    val rmsDb: Double,
    val crestFactorDb: Double,
    val dynamicRangeDb: Double
)

data class FrequencyBand(
    val name: String, // subgraves, graves, low-mid...
    val rangeHz: Pair<Double, Double>,
    val energyDb: Double,
    val relativeEnergyDb: Double
)

data class FrequencyAnalysis(
    val bands: List<FrequencyBand>,
    val spectralBalance: String, // interpretacion
    val accumulations: List<Accumulation>,
    val holes: List<Hole>,
    val rollOffHz: Double?
)

data class Accumulation(val range: Pair<Double,Double>, val severity: Severity, val description: String)
data class Hole(val range: Pair<Double,Double>, val severity: Severity, val description: String)

enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
enum class QualityLabel { EXCELENTE, BUENO, ATENCION, PROBLEMA }

data class StereoAnalysis(
    val correlation: Double, // -1 a 1
    val monoCompatibility: QualityLabel,
    val width: Double, // 0 mono a 1 super wide
    val balanceLrDb: Double, // L - R
    val balanceLabel: String,
    val phaseIssues: List<String>,
    val quality: QualityLabel
)

data class ClippingAnalysis(
    val eventCount: Int,
    val maxTruePeak: Double,
    val hasClipping: Boolean,
    val locationsMs: List<Long>, // timestamps
    val description: String
)

data class Issue(
    val id: String,
    val title: String,
    val severity: Severity,
    val evidence: String,
    val explanation: String,
    val recommendation: String,
    val impactScore: Int, // 0-100
    val frequencyRange: Pair<Double,Double>? = null,
    val confidence: Double = 1.0
)

data class MixScores(
    val overall: Int,
    val loudness: Int,
    val dynamics: Int,
    val frequencyBalance: Int,
    val stereo: Int,
    val phase: Int,
    val clipping: Int,
    val technicalQuality: Int,
    val referenceMatch: Int? = null
)

data class AnalysisResult(
    val fileInfo: AudioFileInfo,
    val loudness: LoudnessMetrics,
    val frequency: FrequencyAnalysis,
    val stereo: StereoAnalysis,
    val clipping: ClippingAnalysis,
    val silenceInitialMs: Long,
    val silenceFinalMs: Long,
    val bpm: Double? = null,
    val bpmConfidence: Double = 0.0,
    val key: String? = null,
    val keyConfidence: Double = 0.0,
    val issuesTop5: List<Issue>,
    val allIssues: List<Issue>,
    val scores: MixScores,
    val releaseCheck: ReleaseCheckResult,
    val timestamp: Long = System.currentTimeMillis(),
    val isEstimateNote: String = "Analisis basado en archivo estereo. Estimaciones de fuentes etiquetadas como estimacion."
)

data class ReleaseCheckResult(
    val status: ReleaseStatus,
    val checks: List<ReleaseCheck>,
    val explanation: String
)
enum class ReleaseStatus { READY, REVIEW, NOT_READY }
data class ReleaseCheck(val name: String, val passed: Boolean, val detail: String, val severity: Severity)

data class ComparisonResult(
    val target: AnalysisResult,
    val reference: AnalysisResult,
    val loudnessDiff: Double,
    val truePeakDiff: Double,
    val spectralDiffs: List<SpectralDiff>,
    val summary: String
)
data class SpectralDiff(val band: String, val diffDb: Double, val description: String)
