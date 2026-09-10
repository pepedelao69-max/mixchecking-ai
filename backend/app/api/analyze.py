
from fastapi import APIRouter, UploadFile, File, Depends, HTTPException
from ..models.schemas import AnalysisStatus
import uuid, os

router = APIRouter()

# Cola simple en memoria para MVP, reemplazar por Redis RQ en prod
jobs = {}

@router.post("/analyze")
async def analyze(file: UploadFile = File(...)):
    if file.content_type not in ["audio/wav","audio/mpeg","audio/flac","audio/mp4","audio/aac","audio/x-aiff","audio/ogg","audio/x-wav"]:
        # permitir por extension tambien
        pass
    job_id = str(uuid.uuid4())
    jobs[job_id] = {"status": "QUEUED", "file": file.filename}
    # En Fase2: encolar a RQ -> librosa + pyloudnorm + demucs
    # Por ahora MVP responde que analisis local es preferido
    return {"job_id": job_id, "status": "QUEUED", "message": "Analisis pesado en servidor - Fase2. Para MVP usa analisis local Android."}

@router.get("/analysis/{job_id}")
async def get_analysis(job_id: str):
    job = jobs.get(job_id)
    if not job: raise HTTPException(404, "Job not found")
    return job

@router.post("/compare")
async def compare(target: UploadFile = File(...), reference: UploadFile = File(...)):
    # Implementacion Fase2: analizar ambos y comparar espectros
    return {"status":"COMING_SOON", "message":"Comparacion con referencia - Fase 2"}

@router.post("/release-check")
async def release_check(file: UploadFile = File(...)):
    return {"status":"COMING_SOON"}

@router.post("/recheck")
async def recheck(v1_id: str, v2_file: UploadFile = File(...)):
    return {"status":"COMING_SOON", "message":"Fix & Recheck compara v1 vs v2 metricas reales"}
