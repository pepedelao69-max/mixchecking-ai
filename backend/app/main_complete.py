
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .api.analyze import router as analyze_router
from .api.billing import router as billing_router
from .api.separate import router as separate_router

app = FastAPI(title="MIXCHECK AI Backend Completo", version="3.0.0", description="Fase 1 local + Fase 2/3 servidor con Demucs, comparación, streaming check, billing seguro")

app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

app.include_router(analyze_router, prefix="/api")
app.include_router(billing_router, prefix="/api")
app.include_router(separate_router, prefix="/api")

@app.get("/")
def root():
    return {
        "status": "MIXCHECK AI Backend Completo",
        "phases": {
            "fase1": "Análisis local Android - LUFS, TruePeak, Spectrum, Stereo, Clipping, MixScore, Regional Mexicano",
            "fase2": "Comparación referencia, Fix & Recheck, Release Check, PDF, Streaming Check simulación",
            "fase3": "Separación stems Demucs/MDX etiquetada como ESTIMACIÓN"
        },
        "billing": "Verificación segura con Google Play Developer API - no confiar solo en cliente",
        "privacy": "No vender archivos, no entrenar sin consentimiento, eliminar temporales 24h, cifrado HTTPS"
    }
