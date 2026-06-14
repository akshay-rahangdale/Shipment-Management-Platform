import logging
from datetime import datetime

from model.features import extract_features, feature_snapshot
from model.predictor import Predictor
from service.mongo_service import MongoService
from config.settings import settings

logger = logging.getLogger(__name__)


class AnomalyService:

    def __init__(self, predictor: Predictor, mongo: MongoService):
        self.predictor = predictor
        self.mongo     = mongo

    def evaluate(self, event: dict) -> dict | None:
        tracking_number = event.get("trackingNumber")

        if not tracking_number:
            logger.warning("Event missing trackingNumber — skipping")
            return None

        record = self.mongo.get_tracking_record(tracking_number)
        if not record:
            logger.warning("No tracking record found for %s", tracking_number)
            return None

        features = extract_features(record, event)
        if features is None:
            return None

        result = self.predictor.predict(features)

        ml_signals = {
            "lastAnomalyScore":    result["anomalyScore"],
            "riskLevel":           result["riskLevel"],
            "predictedDelayHours": result["predictedDelayHours"],
            "featureSnapshot":     feature_snapshot(record, event),
            "scoredAt":            datetime.utcnow().isoformat(),
        }

        self.mongo.update_ml_signals(tracking_number, ml_signals)

        logger.info(
            "Scored trackingNumber=%s score=%.3f riskLevel=%s",
            tracking_number,
            result["anomalyScore"],
            result["riskLevel"],
        )

        if result["anomalyScore"] >= settings.ANOMALY_THRESHOLD:
            return self._build_alert(event, record, result, ml_signals)

        return None

    def _build_alert(
            self,
            event: dict,
            record: dict,
            result: dict,
            ml_signals: dict) -> dict:

        sla = record.get("sla", {})

        return {
            "eventId":             __import__("uuid").uuid4().__str__(),
            "eventType":           "ANOMALY_ALERT",
            "eventTimestamp":      datetime.utcnow().isoformat(),
            "shipmentId":          str(record.get("shipmentId", "")),
            "trackingNumber":      record.get("trackingNumber"),
            "recipientName":       event.get("recipientName", ""),
            "recipientEmail":      event.get("recipientEmail", ""),
            "recipientPhone":      event.get("recipientPhone", ""),
            "anomalyScore":        result["anomalyScore"],
            "riskLevel":           result["riskLevel"],
            "predictedDelayHours": result["predictedDelayHours"],
            "estimatedDelivery":   str(sla.get("expectedDelivery", "")),
            "lastKnownLocation":   event.get("city", ""),
        }
