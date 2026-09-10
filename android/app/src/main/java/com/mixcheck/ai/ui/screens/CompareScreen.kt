
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mixcheck.ai.audio.models.ComparisonResult

@Composable
fun CompareScreen(comparison: ComparisonResult?, onPickTarget: ()->Unit, onPickReference: ()->Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("COMPARAR CON REFERENCIA", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onPickTarget, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar TU mezcla") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onPickReference, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar REFERENCIA") }
        Spacer(Modifier.height(16.dp))
        if (comparison == null) {
            Text("Selecciona ambas canciones para comparar loudness, true peak, espectro, balance tonal, dinámica, stereo width, low-end, midrange, high-end. No copia la referencia, muestra diferencias.", style = MaterialTheme.typography.bodySmall)
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(comparison.summary)
                    Spacer(Modifier.height(8.dp))
                    Text("LUFS diff: ${"%.2f".format(comparison.loudnessDiff)} dB")
                    Text("True Peak diff: ${"%.2f".format(comparison.truePeakDiff)} dB")
                    Spacer(Modifier.height(8.dp))
                    comparison.spectralDiffs.forEach { diff ->
                        Text("${diff.band}: ${"%.1f".format(diff.diffDb)} dB - ${diff.description}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
