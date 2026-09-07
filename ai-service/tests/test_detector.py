import numpy as np
import pytest

from app.services.detector import PotholeDetector


def _road_like_image(dark: bool = True):
    image = np.full((120, 160, 3), 150, dtype=np.uint8)
    if dark:
        cv2 = pytest.importorskip("cv2")
        cv2.ellipse(image, (80, 60), (30, 18), 0, 0, 360, (35, 32, 30), -1)
    return image


def test_detects_pothole_on_dark_patch():
    result = PotholeDetector().analyze(_road_like_image(dark=True))
    assert result["detected_object"] == "POTHOLE"
    assert result["severity"] in ("LOW", "MEDIUM", "HIGH")
    assert 0 < result["confidence"] <= 0.98
    assert result["estimated_length_m"] is not None
    assert result["model_version"] == "cv-heuristic-0.2.0"


def test_no_issue_on_uniform_surface():
    result = PotholeDetector().analyze(_road_like_image(dark=False))
    assert result["detected_object"] == "NO_ISSUE"
    assert result["estimated_length_m"] is None
    assert result["estimated_width_m"] is None


def test_hash_is_deterministic_and_64bit():
    detector = PotholeDetector()
    image = _road_like_image(dark=True)
    first = detector.compute_hash(image)
    second = detector.compute_hash(image.copy())
    assert first == second
    assert -(1 << 63) <= first < (1 << 63)