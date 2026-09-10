# MIXCHECK AI - Arquitectura Tecnica Profesional

Concepto: Herramienta profesional de control de calidad de mezclas. Analisis medido, no inventado.

Arquitectura Hibrida (FASE1 MVP = 100% local)

[Android - Kotlin + Compose MVVM]
 - AudioFileValidator
 - AudioEngine: LufsMeter ITU-R BS1770-4, TruePeak 4x oversampling, RMS, Crest, Spectrum FFT 4096, Stereo correlation/balance/width, Clipping, Silence
 - MixScoreCalculator (metricas reales)
 - RuleEngine IF metric > threshold THEN issue
 - ReportGenerator, Room DB, Billing v6, Backend client Retrofit

[Backend FastAPI] (Fase2)
 POST /analyze -> queue Redis -> librosa, pyloudnorm, demucs
 Estados: QUEUED, PROCESSING, COMPLETED, FAILED

Regla fundamental: Todo viene de medicion. Sin stems no afirmar "bajo a -8.4dB". Usar lenguaje estimado.

MixScore formula real ponderada, no random.
