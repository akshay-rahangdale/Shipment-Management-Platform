import logging
import joblib
import numpy as np
from config.settings import settings

logger = logging.getLogger(__name__)

RISK_LEVELS = {
    (0.90, 1.00): "CRITICAL",
    (0.72, 0.90): "HIGH",
    (0.50, 0.72): "MEDIUM",
    (0.00, 0.50): "LOW",
}


class Predictor:

    def __init__(self):
        self.model  = None
        self.scaler = None
        self._load()

    def _load(self):
        try:
            bundle      = joblib.load(settings.MODEL_PATH)
            self.model  = bundle["model"]
            self.scaler = bundle["scaler"]
            logger.info("Model loaded from %s", settings.MODEL_PATH)
        except FileNotFoundError:
            logger.warning("Model file not found at %s — predictions disabled", settings.MODEL_PATH)

    def is_ready(self) -> bool:
        return self.model is not None and self.scaler is not None

    def predict(self, features: np.ndarray) -> dict:
        if not self.is_ready():
            return {"anomalyScore": 0.0, "riskLevel": "LOW", "predictedDelayHours": 0.0}

        scaled  = self.scaler.transform(features.reshape(1, -1))
        score   = self.model.decision_function(scaled)[0]

        # Isolation Forest returns negative scores for anomalies.
        # We normalise to 0-1 where 1 = most anomalous.
        normalised = float(np.clip(1 - (score + 0.5), 0.0, 1.0))
        risk_level = self._risk_level(normalised)

        predicted_delay = 0.0
        if normalised >= settings.ANOMALY_THRESHOLD:
            predicted_delay = round((normalised - settings.ANOMALY_THRESHOLD) * 24, 1)

        return {
            "anomalyScore":        normalised,
            "riskLevel":           risk_level,
            "predictedDelayHours": predicted_delay,
        }

    def _risk_level(self, score: float) -> str:
        for (low, high), level in RISK_LEVELS.items():
            if low <= score < high:
                return level
        return "LOW"
