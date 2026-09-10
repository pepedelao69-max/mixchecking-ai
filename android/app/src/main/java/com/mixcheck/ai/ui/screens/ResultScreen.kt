
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mixcheck.ai.audio.models.AnalysisResult

@Composable
fun ResultScreen(result: AnalysisResult, onSavePdf: ()->Unit, onRecheck: ()->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("MIX SCORE ${result.scores.overall}/100", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScoreChip("Loudness", result.scores.loudness)
                ScoreChip("Dynamics", result.scores.dynamics)
                ScoreChip("Freq", result.scores.frequencyBalance)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScoreChip("Stereo", result.scores.stereo)
                ScoreChip("Clipping", result.scores.clipping)
                ScoreChip("Tech", result.scores.technicalQuality)
            }
            Spacer(Modifier.height(16.dp))
            Card { Column(Modifier.padding(12.dp)) {
                Text("TECHNICAL ${result.scores.technicalQuality}", fontWeight = FontWeight.Bold)
                Text("Integrated: ${"%.1f".format(result.loudness.integratedLufs)} LUFS | TruePeak ${"%.2f".format(result.loudness.truePeakDbTp)} dBTP")
                Text("RMS ${"%.1f".format(result.loudness.rmsDb)} dB | Crest ${"%.1f".format(result.loudness.crestFactorDb)} dB | LRA ${"%.1f".format(result.loudness.loudnessRangeLra)} LU")
                Text("Correlacion ${"%.2f".format(result.stereo.correlation)} | Balance ${result.stereo.balanceLabel}")
            }}
            Spacer(Modifier.height(16.dp))
            Text("LOS 5 PROBLEMAS MAS IMPORTANTES", fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
        }
        items(result.issuesTop5) { issue ->
            Card(Modifier.fillMaxWidth().padding(vertical=4.dp), colors = CardDefaults.cardColors(containerColor = if (issue.severity.name=="CRITICAL") Color(0xFF4A0F0F) else MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp)) {
                    Text("${issue.severity} - ${issue.title}", fontWeight = FontWeight.Bold, color = if (issue.severity.name=="CRITICAL") Color.Red else MaterialTheme.colorScheme.onSurface)
                    Text("Evidencia: ${issue.evidence}", style = MaterialTheme.typography.bodySmall)
                    Text(issue.explanation, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Recomendacion: ${issue.recommendation}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Text("Release Check: ${result.releaseCheck.status} - ${result.releaseCheck.explanation}")
            Spacer(Modifier.height(8.dp))
            result.releaseCheck.checks.forEach { c ->
                Text("${if (c.passed) "✓" else "✗"} ${c.name}: ${c.detail}")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSavePdf, modifier = Modifier.fillMaxWidth()) { Text("Generar PDF profesional") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onRecheck, modifier = Modifier.fillMaxWidth()) { Text("SUBIR NUEVA VERSION - FIX & RECHECK") }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ScoreChip(label: String, score: Int) {
    AssistChip(onClick = {}, label = { Text("$label $score") })
}
