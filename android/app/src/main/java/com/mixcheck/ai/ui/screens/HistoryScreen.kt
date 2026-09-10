
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mixcheck.ai.data.models.AnalysisHistoryEntity

@Composable
fun HistoryScreen(history: List<AnalysisHistoryEntity>, onProjectClick: (String)->Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("HISTORIAL", style = MaterialTheme.typography.headlineSmall)
        Text("Proyectos: Corrido Nuevo - Versiones Mix01, Mix02, Mix03, Master", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(history) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { onProjectClick(item.projectName) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${item.projectName} - v${item.version} - Score ${item.overallScore}", style = MaterialTheme.typography.titleSmall)
                        Text("${item.fileName} | ${"%.1f".format(item.lufs)} LUFS | TP ${"%.1f".format(item.truePeak)} | ${item.topIssue}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
