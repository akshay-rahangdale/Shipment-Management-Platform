import os
import logging
import joblib
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler

logger = logging.getLogger(__name__)


def generate_normal_samples(n: int = 5000) -> np.ndarray:
    rng = np.random.default_rng(42)
    return np.column_stack([
        rng.normal(48,  12,  n),   # transit_hours       — avg 2 days
        rng.normal(4,   2,   n),   # checkpoint_gap_hours — scan every 4h
        rng.normal(12,  4,   n),   # checkpoint_count
        rng.poisson(0.1,     n),   # exception_count      — rare
        rng.normal(24,  12,  n),   # hours_until_sla      — plenty of time
        rng.normal(0.25, 0.1, n),  # scan_frequency
    ])


def generate_anomaly_samples(n: int = 500) -> np.ndarray:
    rng = np.random.default_rng(99)
    return np.column_stack([
        rng.normal(96,  24,  n),   # transit_hours       — very long
        rng.normal(20,  5,   n),   # checkpoint_gap_hours — long gaps
        rng.normal(5,   2,   n),   # checkpoint_count     — few scans
        rng.poisson(1.5,     n),   # exception_count      — many exceptions
        rng.normal(2,   1,   n),   # hours_until_sla      — almost breached
        rng.normal(0.05, 0.02, n), # scan_frequency       — very low
    ])


def train(output_path: str):
    logger.info("Generating training data")
    normal   = generate_normal_samples(5000)
    anomaly  = generate_anomaly_samples(500)

    X = np.vstack([normal, anomaly])

    scaler  = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    logger.info("Training Isolation Forest")
    model = IsolationForest(
        n_estimators=200,
        contamination=0.08,
        max_samples="auto",
        random_state=42,
        n_jobs=-1,
    )
    model.fit(X_scaled)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    joblib.dump({"model": model, "scaler": scaler}, output_path)
    logger.info("Model saved to %s", output_path)
