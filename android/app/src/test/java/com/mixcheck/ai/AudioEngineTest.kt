
package com.mixcheck.ai

import com.mixcheck.ai.audio.engine.*
import org.junit.Test
import kotlin.math.sin
import kotlin.math.PI
import org.junit.Assert.*

class AudioEngineTest {

    private fun generateSine(freq: Double, sr: Int, durationSec: Double, amplitude: Double = 0.1): FloatArray {
        val len = (sr * durationSec).toInt()
        return FloatArray(len) { i -> (sin(2*PI*freq*i/sr) * amplitude).toFloat() }
    }

    @Test
    fun testLufsCalculation() {
        val sr = 48000
        val mono = generateSine(1000.0, sr, 5.0, 0.1) // -20dBFS aprox
        val meter = LufsMeter()
        val metrics = meter.measure(mono, sr, 1)
        // LUFS debe estar cerca de -20
        assertTrue("LUFS ${metrics.integratedLufs} debe estar entre -25 y -15", metrics.integratedLufs in -25.0..-15.0)
        assertTrue("True Peak debe ser < -1 en tono -20dBFS", metrics.truePeakDbTp < -1)
    }

    @Test
    fun testClippingDetection() {
        val sr = 48000
        val clipped = FloatArray(sr) { 1.0f } // 0dBFS
        val detector = ClippingDetector()
        val result = detector.detect(clipped, sr, 0.0)
        assertTrue("Debe detectar clipping", result.hasClipping)
        assertTrue("Eventos >0", result.eventCount > 0)
    }

    @Test
    fun testStereoCorrelationMono() {
        val sr = 48000
        val mono = generateSine(440.0, sr, 1.0)
        val stereo = FloatArray(mono.size*2) { i -> mono[i/2] }
        val analyzer = StereoAnalyzer()
        val res = analyzer.analyze(stereo, 2)
        assertTrue("Correlación mono debe ser ~1.0, fue ${res.correlation}", res.correlation > 0.99)
    }

    @Test
    fun testStereoBalance() {
        val sr = 48000
        val left = FloatArray(sr) { 0.5f }
        val right = FloatArray(sr) { 0.1f }
        val interleaved = FloatArray(sr*2) { i -> if (i%2==0) left[i/2] else right[i/2] }
        val analyzer = StereoAnalyzer()
        val res = analyzer.analyze(interleaved, 2)
        assertTrue("Balance debe detectar desbalance izquierda, fue ${res.balanceLrDb}", res.balanceLrDb > 2)
    }

    @Test
    fun testMixScoreNotRandom() {
        val sr = 48000
        val mono = generateSine(1000.0, sr, 2.0, 0.1)
        val meter = LufsMeter()
        val loudness = meter.measure(mono, sr, 1)
        val stereoAnalyzer = StereoAnalyzer()
        val stereo = stereoAnalyzer.analyze(mono, 1)
        val spectrumAnalyzer = SpectrumAnalyzer()
        val freq = spectrumAnalyzer.analyze(mono, sr)
        val clipping = ClippingDetector().detect(mono, sr, loudness.truePeakDbTp)
        val calc = MixScoreCalculator()
        val fileInfo = com.mixcheck.ai.audio.models.AudioFileInfo("test.wav", 1000, "WAV", "audio/wav", sr, 1, 16, 2000, 2.0, true)
        val score1 = calc.calculate(loudness, freq, stereo, clipping, fileInfo)
        val score2 = calc.calculate(loudness, freq, stereo, clipping, fileInfo)
        assertEquals("MixScore debe ser determinista, no random", score1.overall, score2.overall)
    }

    @Test
    fun testRegionalProfile() {
        val freq = com.mixcheck.ai.audio.models.FrequencyAnalysis(
            bands = listOf(
                com.mixcheck.ai.audio.models.FrequencyBand("Subgraves", Pair(20.0,60.0), -10.0, -5.0),
                com.mixcheck.ai.audio.models.FrequencyBand("Graves", Pair(60.0,250.0), -5.0, 0.0),
                com.mixcheck.ai.audio.models.FrequencyBand("Low-Mid", Pair(250.0,500.0), 0.0, 6.0), // acumulación
                com.mixcheck.ai.audio.models.FrequencyBand("Medios", Pair(500.0,2000.0), -2.0, 2.0),
                com.mixcheck.ai.audio.models.FrequencyBand("Upper-Mid", Pair(2000.0,4000.0), 0.0, 5.0),
                com.mixcheck.ai.audio.models.FrequencyBand("Presencia", Pair(4000.0,6000.0), -5.0, -2.0),
                com.mixcheck.ai.audio.models.FrequencyBand("Agudos", Pair(6000.0,20000.0), -10.0, -6.0)
            ),
            spectralBalance = "test",
            accumulations = listOf(com.mixcheck.ai.audio.models.Accumulation(Pair(250.0,500.0), com.mixcheck.ai.audio.models.Severity.HIGH, "Acumulación low-mid")),
            holes = emptyList(),
            rollOffHz = null
        )
        val stereo = com.mixcheck.ai.audio.models.StereoAnalysis(0.8, com.mixcheck.ai.audio.models.QualityLabel.BUENO, 0.5, 0.0, "Centrado", emptyList(), com.mixcheck.ai.audio.models.QualityLabel.BUENO)
        val profile = RegionalMexicanoProfile()
        val issues = profile.analyze(freq, stereo)
        assertTrue("Debe detectar conflicto regional", issues.isNotEmpty())
    }
}
