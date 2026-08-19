from typing import Optional

from pydantic import BaseModel


class AnalysisResult(BaseModel):
    detected_object: str
    severity: str
    confidence: float
    estimated_length_m: Optional[float] = None
    estimated_width_m: Optional[float] = None
    road_risk: str
    model_version: str