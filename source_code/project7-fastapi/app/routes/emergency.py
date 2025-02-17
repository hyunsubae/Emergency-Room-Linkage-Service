from fastapi import APIRouter, HTTPException
from typing import Dict
from app.service.emergency import EmergencyService
from app.config import C_ID, C_KEY
import traceback

router = APIRouter()

@router.get("/emergency/analyze")
async def analyze_emergency(
    text: str,
    latitude: float,
    longitude: float
) -> Dict:
    try:
        emergency_service = EmergencyService()
        result = await emergency_service.process_emergency(
            text=text,
            latitude=latitude,
            longitude=longitude,
            client_id=C_ID,
            client_key=C_KEY
        )
        return result
    except Exception as e:
        error_detail = f"Error: {str(e)}\n{traceback.format_exc()}"
        print(error_detail)  # 서버 콘솔에 에러 출력
        raise HTTPException(status_code=500, detail=str(e))
