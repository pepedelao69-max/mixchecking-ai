
package com.mixcheck.ai.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.mixcheck.ai.audio.models.AnalysisResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfReportGenerator(private val context: Context) {

    fun generate(result: AnalysisResult, referenceName: String? = null): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var y = 40f

        fun drawText(text: String, size: Float = 12f, bold: Boolean = false, color: Int = Color.BLACK) {
            paint.color = color
            paint.textSize = size
            paint.isFakeBoldText = bold
            if (y > 800) {
                doc.finishPage(page)
                val newPage = doc.startPage(pageInfo)
                page = newPage
                canvas = newPage.canvas
                y = 40f
            }
            canvas.drawText(text, 40f, y, paint)
            y += size + 8
        }

        drawText("MIXCHECK AI - Informe Profesional", 20f, true, Color.BLACK)
        drawText("Tu mezcla. Analizada de verdad.", 12f, false, Color.GRAY)
        y += 10f
        drawText("Archivo: ${result.fileInfo.fileName}", 10f)
        drawText("Fecha: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 10f)
        drawText("Formato: ${result.fileInfo.format} | ${result.fileInfo.sampleRate}Hz | ${result.fileInfo.channels}ch | ${"%.1f".format(result.fileInfo.durationSeconds)}s", 10f)
        y += 10f
        drawText("MIX SCORE ${result.scores.overall}/100", 18f, true)
        drawText("Loudness ${result.scores.loudness} | Dynamics ${result.scores.dynamics} | Freq ${result.scores.frequencyBalance} | Stereo ${result.scores.stereo} | Phase ${result.scores.phase} | Clipping ${result.scores.clipping} | Tech ${result.scores.technicalQuality}", 9f)
        y += 10f
        drawText("TECHNICAL METRICS", 12f, true)
        drawText("Integrated LUFS: ${"%.1f".format(result.loudness.integratedLufs)} | True Peak: ${"%.2f".format(result.loudness.truePeakDbTp)} dBTP | RMS: ${"%.1f".format(result.loudness.rmsDb)} dB | Crest: ${"%.1f".format(result.loudness.crestFactorDb)} dB | LRA: ${"%.1f".format(result.loudness.loudnessRangeLra)} LU", 9f)
        drawText("Correlación: ${"%.2f".format(result.stereo.correlation)} | Balance: ${result.stereo.balanceLabel} | Mono: ${result.stereo.monoCompatibility}", 9f)
        y += 10f
        drawText("FREQUENCY BALANCE", 12f, true)
        result.frequency.bands.forEach { b ->
            drawText("${b.name} ${b.rangeHz.first.toInt()}-${b.rangeHz.second.toInt()}Hz: ${"%.1f".format(b.relativeEnergyDb)} dB rel", 9f)
        }
        y += 10f
        drawText("TOP 5 PROBLEMAS PRIORITARIOS", 12f, true)
        result.issuesTop5.forEachIndexed { i, iss ->
            drawText("${i+1}. [${iss.severity}] ${iss.title}", 10f, true, if (iss.severity.name=="CRITICAL") Color.RED else Color.BLACK)
            drawText("   Evidencia: ${iss.evidence}", 8f)
            drawText("   Explicación: ${iss.explanation}", 8f)
            drawText("   Recomendación: ${iss.recommendation}", 8f, false, Color.BLUE)
            y += 4f
        }
        y += 10f
        drawText("RELEASE CHECK - ${result.releaseCheck.status}", 12f, true)
        drawText(result.releaseCheck.explanation, 9f)
        result.releaseCheck.checks.forEach { c ->
            drawText("${if (c.passed) "✓" else "✗"} ${c.name}: ${c.detail}", 9f, false, if (c.passed) Color.BLACK else Color.RED)
        }
        if (referenceName != null) {
            y += 10f
            drawText("Referencia utilizada: $referenceName", 9f)
        }
        drawText("Nota: Análisis basado en archivo estéreo. Estimaciones de fuentes etiquetadas como estimación. No se afirman valores exactos de stems sin separación.", 7f, false, Color.GRAY)

        doc.finishPage(page)
        val file = File(context.cacheDir, "MIXCHECK_${result.fileInfo.fileName}_${System.currentTimeMillis()}.pdf")
        doc.writeTo(FileOutputStream(file))
        doc.close()
        return file
    }
}
