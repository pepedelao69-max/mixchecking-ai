
from fastapi import FastAPI, UploadFile, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from .api.analyze import router as analyze_router
from .api.billing import router as billing_router

app = FastAPI(title="MIXCHECK AI Backend", version="1.0.0-mvp")

app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

app.include_router(analyze_router, prefix="/api")
app.include_router(billing_router, prefix="/api")

@app.get("/")
def root():
    return {"status":"MIXCHECK AI Backend running", "phase":"MVP local-first, backend ready for Fase2"}

@app.get("/health")
def health():
    return {"status":"ok"}
