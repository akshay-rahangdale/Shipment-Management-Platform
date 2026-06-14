import logging
from datetime import datetime, timezone
from typing import Optional
import numpy as np

logger = logging.getLogger(__name__)

EXCEPTION_CODES = {
    "WEATHER_DELAY", "ADDRESS_ISSUE", "CUSTOMS_HOLD",
    "DAMAGED", "LOST", "SECURITY_HOLD"
}


def extract_features(record: dict, event: dict) -> Optional[np.ndarray]:
    try:
        checkpoints = record.get("checkpoints", [])
        sla         = record.get("sla", {})

        transit_hours        = _transit_hours(record)
        checkpoint_gap_hours = event.get("checkpointGapHours", 0)
        checkpoint_count     = len(checkpoints)
        exception_count      = _count_exceptions(checkpoints)
        hours_until_sla      = _hours_until_sla(sla)
        scan_frequency       = checkpoint_count / max(transit_hours, 1)

        features = np.array([
            transit_hours,
            checkpoint_gap_hours,
            checkpoint_count,
            exception_count,
            hours_until_sla,
            scan_frequency,
        ], dtype=float)

        return features

    except Exception as ex:
        logger.error("Feature extraction failed: %s", ex)
        return None


def feature_snapshot(record: dict, event: dict) -> dict:
    checkpoints = record.get("checkpoints", [])
    sla         = record.get("sla", {})
    return {
        "transitHours":       _transit_hours(record),
        "checkpointGapHours": event.get("checkpointGapHours", 0),
        "checkpointCount":    len(checkpoints),
        "exceptionCount":     _count_exceptions(checkpoints),
        "hoursUntilSla":      _hours_until_sla(sla),
    }


def _transit_hours(record: dict) -> float:
    created_at = record.get("createdAt")
    if not created_at:
        return 0.0
    if isinstance(created_at, str):
        created_at = datetime.fromisoformat(created_at)
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    return max((now - created_at.replace(tzinfo=None)).total_seconds() / 3600, 0.0)


def _count_exceptions(checkpoints: list) -> int:
    return sum(
        1 for cp in checkpoints
        if cp.get("exceptionCode") in EXCEPTION_CODES
    )


def _hours_until_sla(sla: dict) -> float:
    expected = sla.get("expectedDelivery")
    if not expected:
        return 999.0
    if isinstance(expected, str):
        expected = datetime.fromisoformat(expected)
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    return (expected.replace(tzinfo=None) - now).total_seconds() / 3600
