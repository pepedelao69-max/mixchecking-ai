
package com.mixcheck.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var lang by remember { mutableStateOf("Español") }
    var regional by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("CONFIGURACIÓN", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Idioma")
        Row {
            FilterChip(selected = lang=="Español", onClick = { lang="Español" }, label = { Text("Español") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = lang=="English", onClick = { lang="English" }, label = { Text("English") })
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Perfil Regional Mexicano")
            Switch(checked = regional, onCheckedChange = { regional=it })
        }
        Spacer(Modifier.height(16.dp))
        Text("Privacidad: No vendemos archivos. No usamos canciones para entrenar sin consentimiento. Archivos temporales eliminados. Transferencias cifradas.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text("Cuenta: Google Sign-In / Email", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text("Suscripción: FREE 3/mes, PRO 149 MXN/mes, STUDIO 399 MXN/mes. Product IDs configurables desde Play Console.", style = MaterialTheme.typography.bodySmall)
    }
}
