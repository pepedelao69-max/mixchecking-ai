
package com.mixcheck.ai.audio.engine

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import com.mixcheck.ai.audio.models.*
import java.nio.ByteBuffer
import kotlin.math.abs

/**
 * Motor central de analisis - MVP local
 * Flujo: validar -> decodificar PCM -> analisis paralelos
 */
class AudioEngine(private val context: Context) {

    private val validator = AudioFileValidator(context)
    private val lufsMeter = LufsMeter()
    private val stereoAnalyzer = StereoAnalyzer()
    private val spectrumAnalyzer = SpectrumAnalyzer()
    private val clippingDetector = ClippingDetector()
    private val scoreCalc = MixScoreCalculator()
    private val ruleEngine = RuleEngine()
    private val regionalProfile = RegionalMexicanoProfile()

    suspend fun analyze(uri: android.net.Uri, fileName: String, enableRegional: Boolean = true, onProgress: (Float, String)->Unit = {_,_ ->}): AnalysisResult {
        onProgress(0.05f, "Validando archivo...")
        val info = validator.validate(uri, fileName)
        if (!info.isValid) throw IllegalArgumentException(info.validationError ?: "Archivo no valido")

        onProgress(0.15f, "Decodificando audio...")
        val pcm = decodePcm(uri, info)

        onProgress(0.35f, "Midiendo loudness...")
        val monoForLoudness = downmixToMono(pcm.data, pcm.channels)
        val loudness = lufsMeter.measure(monoForLoudness, info.sampleRate, 1)

        onProgress(0.50f, "Analizando estereo y fase...")
        val stereo = stereoAnalyzer.analyze(pcm.data, pcm.channels)

        onProgress(0.65f, "Analizando espectro...")
        val freq = spectrumAnalyzer.analyze(monoForLoudness, info.sampleRate)

        onProgress(0.75f, "Detectando clipping...")
        val clipping = clippingDetector.detect(monoForLoudness, info.sampleRate, loudness.truePeakDbTp)

        onProgress(0.85f, "Calculando score y diagnostico...")
        val issues = mutableListOf<Issue>()
        issues.addAll(ruleEngine.generateIssues(loudness, freq, stereo, clipping))
        if (enableRegional) issues.addAll(regionalProfile.analyze(freq, stereo))

        val sorted = issues.sortedByDescending { it.impactScore }
        val top5 = sorted.take(5)

        val scores = scoreCalc.calculate(loudness, freq, stereo, clipping, info)
        val release = ruleEngine.releaseCheck(loudness, clipping, stereo, info)

        // Silencio inicial/final simple
        val silenceInit = detectSilenceMs(monoForLoudness, info.sampleRate, fromStart = true)
        val silenceFinal = detectSilenceMs(monoForLoudness, info.sampleRate, fromStart = false)

        onProgress(1.0f, "Analisis completo")

        return AnalysisResult(
            fileInfo = info,
            loudness = loudness,
            frequency = freq,
            stereo = stereo,
            clipping = clipping,
            silenceInitialMs = silenceInit,
            silenceFinalMs = silenceFinal,
            issuesTop5 = top5,
            allIssues = sorted,
            scores = scores,
            releaseCheck = release
        )
    }

    private data class PcmData(val data: FloatArray, val channels: Int)

    private fun decodePcm(uri: android.net.Uri, info: AudioFileInfo): PcmData {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        val format = extractor.getTrackFormat(0)
        val mime = format.getString(MediaFormat.KEY_MIME)!!
        extractor.selectTrack(0)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val pcmList = mutableListOf<Float>()
        var isEOS = false
        val bufferInfo = MediaCodec.BufferInfo()

        while (!isEOS) {
            val inIndex = codec.dequeueInputBuffer(10000)
            if (inIndex >=0) {
                val inBuf = codec.getInputBuffer(inIndex)!!
                val sampleSize = extractor.readSampleData(inBuf, 0)
                if (sampleSize <0) {
                    codec.queueInputBuffer(inIndex, 0,0,0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    isEOS = true
                } else {
                    codec.queueInputBuffer(inIndex,0,sampleSize, extractor.sampleTime,0)
                    extractor.advance()
                }
            }
            val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outIndex >=0) {
                val outBuf = codec.getOutputBuffer(outIndex)!!
                // PCM 16-bit to float
                val pcm16 = ShortArray(bufferInfo.size/2)
                outBuf.asShortBuffer().get(pcm16)
                for (s in pcm16) pcmList.add(s / 32768f)
                codec.releaseOutputBuffer(outIndex, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM !=0) break
            }
        }
        codec.stop(); codec.release(); extractor.release()
        return PcmData(pcmList.toFloatArray(), info.channels)
    }

    private fun downmixToMono(interleaved: FloatArray, channels: Int): FloatArray {
        if (channels==1) return interleaved
        val frames = interleaved.size / channels
        return FloatArray(frames) { i ->
            var sum=0f
            for (c in 0 until channels) sum+= interleaved[i*channels + c]
            sum/channels
        }
    }

    private fun detectSilenceMs(mono: FloatArray, sr: Int, fromStart: Boolean, thresholdDb: Double = -60.0): Long {
        val thresh = kotlin.math.pow(10.0, thresholdDb/20.0).toFloat()
        var count=0
        val range = if (fromStart) mono.indices else mono.indices.reversed()
        for (i in range) {
            if (abs(mono[i]) < thresh) count++ else break
            if (count > sr*0.5) break // max 0.5s detect
        }
        return (count*1000L)/sr
    }
}
