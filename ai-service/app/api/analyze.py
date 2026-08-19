from fastapi import APIRouter, File, HTTPException, UploadFile, status

from app.schemas.analysis import AnalysisResult
from app.services.detector import PotholeDetector

import cv2
import numpy as np

router = APIRouter(prefix="/api", tags=["analyze"])

detector = PotholeDetector()


@router.post("/analyze", response_model=AnalysisResult)
async def analyze_image(file: UploadFile = File(default=None)):
    if file is None:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Missing 'file' upload")
    data = await file.read()

    if not data:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Empty file uploaded")

    image = cv2.imdecode(np.frombuffer(data, dtype=np.uint8), cv2.IMREAD_COLOR)
    if image is None:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Could not decode image")

    return AnalysisResult(**detector.analyze(image))