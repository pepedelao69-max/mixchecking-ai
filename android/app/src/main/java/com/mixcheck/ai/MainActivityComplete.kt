
package com.mixcheck.ai

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.mixcheck.ai.audio.engine.*
import com.mixcheck.ai.audio.models.AnalysisResult
import com.mixcheck.ai.data.repository.HistoryRepository
import com.mixcheck.ai.ui.screens.*
import com.mixcheck.ai.ui.theme.MixCheckTheme
import com.mixcheck.ai.utils.PdfReportGenerator
import kotlinx.coroutines.launch

class MainActivityComplete : ComponentActivity() {

    private var currentUri: Uri? = null
    private var currentName: String? = null
    private var targetResult: AnalysisResult? = null
    private var refResult: AnalysisResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val engine = AudioEngine(this)
        val historyRepo = HistoryRepository(this)
        val comparisonAnalyzer = ComparisonAnalyzer()
        val recheckManager = RecheckManager()
        val streamingSimulator = StreamingSimulator()
        val pdfGen = PdfReportGenerator(this)

        val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { currentUri = it; currentName = "archivo_${System.currentTimeMillis()}" }
        }

        setContent {
            MixCheckTheme {
                var screen by remember { mutableStateOf("home") }
                var progress by remember { mutableStateOf(0f) }
                var status by remember { mutableStateOf("") }
                var result by remember { mutableStateOf<AnalysisResult?>(null) }
                var fileName by remember { mutableStateOf<String?>(null) }
                var history by remember { mutableStateOf(listOf<com.mixcheck.ai.data.models.AnalysisHistoryEntity>()) }
                var comparison by remember { mutableStateOf<com.mixcheck.ai.audio.models.ComparisonResult?>(null) }
                var streamingIssues by remember { mutableStateOf(listOf<com.mixcheck.ai.audio.engine.StreamingIssue>()) }

                Scaffold(bottomBar = {
                    NavigationBar {
                        NavigationBarItem(selected = screen=="home", onClick = { screen="home" }, label = { Text("Inicio") }, icon = {})
                        NavigationBarItem(selected = screen=="analyze", onClick = { screen="analyze" }, label = { Text("Analizar") }, icon = {})
                        NavigationBarItem(selected = screen=="history", onClick = { 
                            screen="history"
                            lifecycleScope.launch { history = historyRepo.getAll() }
                        }, label = { Text("Historial") }, icon = {})
                        NavigationBarItem(selected = screen=="release", onClick = { screen="release" }, label = { Text("Release") }, icon = {})
                        NavigationBarItem(selected = screen=="settings", onClick = { screen="settings" }, label = { Text("Ajustes") }, icon = {})
                    }
                }) { padding ->
                    Box(Modifier.padding(padding)) {
                        when(screen) {
                            "home" -> HomeScreen(onAnalyze={screen="analyze"}, onCompare={screen="compare"}, onHistory={screen="history"; lifecycleScope.launch{history=historyRepo.getAll()}}, onRelease={screen="release"})
                            "analyze" -> AnalysisScreen(fileName=fileName, progress=progress, status=status, onPickFile={ pickFile.launch("audio/*"); fileName=currentName }, onStart={
                                val uri=currentUri?:return@AnalysisScreen
                                lifecycleScope.launch {
                                    try {
                                        val res = engine.analyze(uri, fileName?:"mix.wav", enableRegional=true) { p,s -> progress=p; status=s }
                                        result=res
                                        targetResult=res
                                        streamingIssues=streamingSimulator.simulate(res)
                                        // Guardar en historial proyecto por defecto
                                        historyRepo.save("Proyecto Regional", res, (history.size+1))
                                        screen="result"
                                    } catch(e: Exception){ status="Error: ${e.message}" }
                                }
                            }, canAnalyze=currentUri!=null)
                            "result" -> result?.let { ResultScreen(it, onSavePdf={
                                lifecycleScope.launch {
                                    val file = pdfGen.generate(it)
                                    status = "PDF generado: ${file.absolutePath}"
                                }
                            }, onRecheck={ screen="analyze" }) }
                            "compare" -> CompareScreen(comparison, onPickTarget={
                                // seleccionar target: analiza y guarda como target
                                screen="analyze"
                            }, onPickReference={
                                screen="analyze"
                            })
                            "history" -> HistoryScreen(history, onProjectClick={})
                            "release" -> ReleaseCheckScreen(result?.releaseCheck)
                            "streaming" -> StreamingCheckScreen(streamingIssues)
                            "settings" -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
