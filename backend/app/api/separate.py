
from fastapi import APIRouter, UploadFile, File
import uuid

router = APIRouter()
jobs = {}

@router.post("/separate")
async def separate(file: UploadFile = File(...)):
    job_id = str(uuid.uuid4())
    # En producción: encolar a worker GPU con Demucs/MDX23
    # y = demucs.separate(file)
    # retornar stems con confidence y etiqueta ESTIMACIÓN
    jobs[job_id] = {"status": "QUEUED", "message": "Separación con Demucs/MDX - etiquetado como ESTIMACIÓN", "stems": ["vocals","bass","drums","other"]}
    return {"job_id": job_id, "status": "QUEUED", "note": "Estimación automática, no medición exacta. Confianza variable."}
