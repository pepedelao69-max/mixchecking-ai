
package com.mixcheck.ai

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.mixcheck.ai.audio.engine.AudioEngine
import com.mixcheck.ai.audio.models.AnalysisResult
import com.mixcheck.ai.ui.screens.AnalysisScreen
import com.mixcheck.ai.ui.screens.HomeScreen
import com.mixcheck.ai.ui.screens.ResultScreen
import com.mixcheck.ai.ui.theme.MixCheckTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var currentUri: Uri? = null
    private var currentName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val engine = AudioEngine(this)

        val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                currentUri = it
                currentName = "archivo_seleccionado" // resolver real con ContentResolver en prod
            }
        }

        setContent {
            MixCheckTheme {
                var screen by remember { mutableStateOf("home") }
                var progress by remember { mutableStateOf(0f) }
                var status by remember { mutableStateOf("") }
                var result by remember { mutableStateOf<AnalysisResult?>(null) }
                var fileName by remember { mutableStateOf<String?>(null) }

                when (screen) {
                    "home" -> HomeScreen(
                        onAnalyze = { screen = "analyze" },
                        onCompare = { /* Fase 2 */ },
                        onHistory = { /* Room */ },
                        onRelease = { /* integrado en result */ }
                    )
                    "analyze" -> AnalysisScreen(
                        fileName = fileName,
                        progress = progress,
                        status = status,
                        onPickFile = { pickFile.launch("audio/*"); fileName = currentName },
                        onStart = {
                            val uri = currentUri ?: return@AnalysisScreen
                            lifecycleScope.launch {
                                try {
                                    val res = engine.analyze(uri, fileName ?: "mix.wav", enableRegional = true) { p, s ->
                                        progress = p; status = s
                                    }
                                    result = res
                                    screen = "result"
                                } catch (e: Exception) {
                                    status = "Error: ${e.message}"
                                }
                            }
                        },
                        canAnalyze = currentUri != null
                    )
                    "result" -> result?.let {
                        ResultScreen(it, onSavePdf = { /* PDF generator */ }, onRecheck = { screen = "analyze" })
                    }
                }
            }
        }
    }
}
