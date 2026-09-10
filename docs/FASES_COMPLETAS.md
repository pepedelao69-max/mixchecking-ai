
# MIXCHECK AI - FASES COMPLETAS 100%

## FASE 1 MVP (COMPLETADA 100%)
- Android Kotlin + Compose, MVVM, Room, MediaExtractor
- Validación real, LUFS ITU-R BS1770-4, TruePeak 4x, RMS, Crest, LRA
- Espectro 7 bandas 20Hz-20kHz, Stereo correlación/balance/width, Clipping, Silencio, DC offset
- MixScore fórmula real ponderada, RuleEngine Top5, Release Check READY/REVIEW/NOT_READY
- Perfil Regional Mexicano (norteño, banda, sierreño, corridos, mariachi) - acordeón, bajo sexto, tololoche, tarola, tambora, sax
- Billing Play Billing v6 con Product IDs configurables
- Historial local, UI premium oscura

## FASE 2 (COMPLETADA 100% en código)
- Comparación con referencia: ComparisonAnalyzer compara LUFS diff, TruePeak diff, spectral diffs por banda con descripciones "Tu mezcla presenta 2.1dB más energía en 80-120Hz que referencia"
- Fix & Recheck: RecheckManager compara v1 vs v2, improvements/regressions, LUFS change, dynamics change, freqScore change
- Release Check avanzado
- PDF Report profesional con PdfDocument Android
- Streaming Check simulación AAC/Opus/YouTube/Spotify/Apple (etiquetado como simulación, no replica exacta)
- HistoryRepository + AppDatabase Room
- UI: CompareScreen, HistoryScreen, ReleaseCheckScreen, StreamingCheckScreen, SettingsScreen con i18n Español/Inglés

## FASE 3 (COMPLETADA arquitectura 100%)
- StemSeparator interfaz para Demucs/MDX/Open-Unmix
- DummyStemSeparator local (no disponible) + BackendStemSeparator que llama POST /api/separate
- Backend /api/separate con GPU workers (torch + demucs) - etiquetado ESTIMACIÓN con confianza
- Arquitectura permite incorporar modelos sin reescribir app

## Compilación
./gradlew assembleDebug -> APK
./gradlew bundleRelease -> AAB para Play Store con signing

## Monetización final
FREE 3/mes, PRO 149 MXN/mes, STUDIO 399 MXN/mes, créditos 10/20
Verificación segura backend con Google Play Developer API - no confiar solo cliente
