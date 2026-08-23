import numpy as np
import cv2


MODEL_VERSION = "cv-heuristic-0.2.0"

_SEVERITY_LOW = "LOW"
_SEVERITY_MEDIUM = "MEDIUM"
_SEVERITY_HIGH = "HIGH"


class PotholeDetector:
    """Heuristic image analysis for road damage / pothole detection.

    This is an MVP detector: it segments dark, roughly elliptical regions in
    the road image (potholes typically appear as dark patches) and estimates
    size, severity and confidence from the segmented region. It is designed to
    be replaced by a real deep-learning model without changing the API contract.
    """

    def analyze(self, image: np.ndarray) -> dict:
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        blurred = cv2.GaussianBlur(gray, (7, 7), 0)

        # Dark regions on asphalt are candidate potholes.
        # Otsu's threshold finds the natural separation between dark asphalt
        # and the darker pothole interiors.
        _, mask = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

        mask = self._keep_largest_region(mask)

        coverage = float(np.count_nonzero(mask)) / float(mask.size)

        if coverage < 0.02:
            detected = False
            detected_object = "NO_ISSUE"
            severity = _SEVERITY_LOW
            road_risk = "LOW"
            confidence = max(0.5, round(1.0 - coverage * 10, 2))
            length_m = width_m = None
        else:
            detected = True
            detected_object = "POTHOLE"
            confidence = round(min(0.98, 0.55 + coverage * 2.5), 2)

            width_m, length_m = self._estimate_dimensions_m(image, mask)

            if coverage >= 0.30:
                severity = _SEVERITY_HIGH
                road_risk = "HIGH"
            elif coverage >= 0.12:
                severity = _SEVERITY_MEDIUM
                road_risk = "MEDIUM"
            else:
                severity = _SEVERITY_LOW
                road_risk = "LOW"

        return {
            "detected_object": detected_object,
            "severity": severity,
            "confidence": float(confidence),
            "estimated_length_m": length_m,
            "estimated_width_m": width_m,
            "road_risk": road_risk,
            "model_version": MODEL_VERSION,
            "image_hash": self.compute_hash(image),
        }

    @staticmethod
    def compute_hash(image: np.ndarray) -> int:
        """64-bit average hash of the image for duplicate detection.

        The image is downscaled to 8x8 grayscale; each pixel brighter than
        the mean sets one bit. Similar photos yield similar hashes, so the
        Hamming distance between two hashes approximates visual similarity
        without storing the images themselves.
        """
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        resized = cv2.resize(gray, (8, 8), interpolation=cv2.INTER_AREA)
        bits = resized > resized.mean()
        value = 0
        for bit in bits.flatten():
            value = (value << 1) | int(bit)
        # Reinterpret as signed 64-bit so Java/Jackson can parse it.
        if value >= 1 << 63:
            value -= 1 << 64
        return value

    @staticmethod
    def _keep_largest_region(mask: np.ndarray) -> np.ndarray:
        num_labels, labels, stats, _ = cv2.connectedComponentsWithStats(mask, 8)
        if num_labels <= 1:
            return mask
        # Index 0 is the background.
        largest = 1 + int(np.argmax(stats[1:, cv2.CC_STAT_AREA]))
        return np.where(labels == largest, 255, 0).astype(np.uint8)

    @staticmethod
    def _estimate_dimensions_m(image: np.ndarray, mask: np.ndarray) -> tuple:
        h, w = image.shape[:2]
        # Rough calibration: assume a typical asphalt photo frames ~4m across.
        meters_per_pixel_x = 4.0 / w
        # Equivalent to a ~3m vertical span for a 4:3 aspect frame.
        meters_per_pixel_y = 3.0 / h

        xs = np.where(mask > 0)[1]
        ys = np.where(mask > 0)[0]

        width_m = round(float((xs.max() - xs.min()) * meters_per_pixel_x), 2)
        length_m = round(float((ys.max() - ys.min()) * meters_per_pixel_y), 2)
        return width_m, length_m