
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalysisScreen(fileName: String?, progress: Float, status: String, onPickFile: ()->Unit, onStart: ()->Unit, canAnalyze: Boolean) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Analizar mezcla", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onPickFile) { Text("Seleccionar archivo (WAV/MP3/FLAC/M4A/AAC/AIFF/OGG)") }
        Spacer(Modifier.height(16.dp))
        if (fileName != null) Text("Archivo: $fileName")
        Spacer(Modifier.height(24.dp))
        if (progress>0) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(status, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStart, enabled = canAnalyze, modifier = Modifier.fillMaxWidth()) { Text("Iniciar analisis real") }
        Spacer(Modifier.height(16.dp))
        Text("Validacion: formato, sample rate, canales, duracion, clipping, silencio, DC offset. Sin barra falsa.", style = MaterialTheme.typography.labelSmall)
    }
}
