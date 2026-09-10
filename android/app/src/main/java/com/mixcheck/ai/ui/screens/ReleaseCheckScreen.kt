
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mixcheck.ai.audio.models.ReleaseCheckResult

@Composable
fun ReleaseCheckScreen(result: ReleaseCheckResult?) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("RELEASE CHECK - ¿ESTÁ LISTA PARA ENTREGAR?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        if (result == null) {
            Text("Analiza una mezcla primero. Verificamos clipping, true peak, loudness, silencio accidental, sample rate, canales, duración, fase, balance L/R.", style = MaterialTheme.typography.bodySmall)
        } else {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = when(result.status.name) {
                "READY" -> MaterialTheme.colorScheme.primaryContainer
                "REVIEW" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            })) {
                Column(Modifier.padding(16.dp)) {
                    Text(result.status.name, style = MaterialTheme.typography.headlineMedium)
                    Text(result.explanation)
                }
            }
            Spacer(Modifier.height(16.dp))
            result.checks.forEach { c ->
                Row(Modifier.fillMaxWidth().padding(vertical=2.dp)) {
                    Text(if (c.passed) "✓" else "✗")
                    Spacer(Modifier.width(8.dp))
                    Column { Text(c.name, style = MaterialTheme.typography.titleSmall); Text(c.detail, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}
