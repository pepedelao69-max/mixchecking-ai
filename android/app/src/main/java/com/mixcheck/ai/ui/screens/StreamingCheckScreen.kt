
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mixcheck.ai.audio.engine.StreamingIssue

@Composable
fun StreamingCheckScreen(issues: List<StreamingIssue>) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("STREAMING CHECK", style = MaterialTheme.typography.headlineSmall)
        Text("Simulación de codificación AAC/Opus - No replica exactamente Spotify/Apple/YouTube, es estimación de riesgo", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        if (issues.isEmpty()) {
            Text("Sin problemas de streaming detectados. Simulación indica reproducción segura.", style = MaterialTheme.typography.bodyMedium)
        } else {
            issues.forEach { iss ->
                Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${iss.platform} - ${iss.severity}", style = MaterialTheme.typography.titleSmall)
                        Text(iss.issue, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
