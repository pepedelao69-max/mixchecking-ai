
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onAnalyze: ()->Unit, onCompare: ()->Unit, onHistory: ()->Unit, onRelease: ()->Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(40.dp))
        Text("MIXCHECK AI", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text("Tu mezcla. Analizada de verdad.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onAnalyze, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("ANALIZAR MEZCLA") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onCompare, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("COMPARAR CON REFERENCIA") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("HISTORIAL") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRelease, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("RELEASE CHECK") }
        Spacer(Modifier.weight(1f))
        Text("Perfil Regional Mexicano activado", style = MaterialTheme.typography.labelSmall)
    }
}
