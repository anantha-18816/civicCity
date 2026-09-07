import cv2
import numpy as np
import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def _jpeg_bytes(image: np.ndarray) -> bytes:
    ok, encoded = cv2.imencode(".jpg", image)
    assert ok
    return encoded.tobytes()


def test_health():
    response = client.get("/api/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"


def test_analyze_returns_result_schema():
    image = np.full((120, 160, 3), 150, dtype=np.uint8)
    cv2.ellipse(image, (80, 60), (30, 18), 0, 0, 360, (35, 32, 30), -1)
    response = client.post("/api/analyze", files={"file": ("road.jpg", _jpeg_bytes(image), "image/jpeg")})
    assert response.status_code == 200
    body = response.json()
    assert body["detected_object"] in ("POTHOLE", "NO_ISSUE")
    assert body["severity"] in ("LOW", "MEDIUM", "HIGH")
    assert isinstance(body["confidence"], float)
    assert isinstance(body["image_hash"], int)


def test_analyze_missing_file_returns_400():
    response = client.post("/api/analyze")
    assert response.status_code == 400


def test_analyze_garbage_bytes_returns_400():
    response = client.post("/api/analyze", files={"file": ("junk.jpg", b"not-an-image", "image/jpeg")})
    assert response.status_code == 400