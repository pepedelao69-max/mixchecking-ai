
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter()

class VerifyRequest(BaseModel):
    purchaseToken: str
    productId: str
    packageName: str = "com.mixcheck.ai"

@router.post("/billing/verify")
async def verify(req: VerifyRequest):
    # TODO: Verificacion segura con Google Play Developer API
    # from googleapiclient.discovery import build
    # service = build('androidpublisher','v3', credentials=...)
    # result = service.purchases().subscriptions().get(...).execute()
    # No confiar solo en cliente
    # Por ahora stub
    return {"valid": False, "message": "Implementar verificacion con Google Play Developer API en produccion", "productId": req.productId}
