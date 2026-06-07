"""
WHAT IS THIS FILE?

This is the Python equivalent of ShipmentServiceApplication.java.
When the ML detector container starts, Python runs this file.

It does three things simultaneously:
1. Loads the trained Isolation Forest model from disk
2. Starts a background thread that continuously reads from Kafka
3. Starts a Flask web server for /health and /metrics endpoints

WHY A BACKGROUND THREAD FOR KAFKA?
Flask is single-threaded by default. If we ran Kafka polling
inside Flask's request handler, we'd block the web server.
Instead, the Kafka consumer runs in its own thread, and Flask
runs in the main thread. They share the loaded model via a
module-level variable (thread-safe for reads in Python).
"""

import os
import logging
import threading
import joblib
from flask import Flask, jsonify
from prometheus_client import make_wsgi_app, Counter, Histogram
from werkzeug.middleware.dispatcher import DispatcherMiddleware

# ─────────────────────────────────────────────
# LOGGING SETUP
# ─────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(threadName)s] %(levelname)s %(name)s - %(message)s"
)
logger = logging.getLogger(__name__)

# ─────────────────────────────────────────────
# PROMETHEUS METRICS
# ─────────────────────────────────────────────
# Counter: monotonically increasing count (never decreases).
# Use for: number of predictions made, number of alerts sent.
PREDICTIONS_TOTAL = Counter(
    "ml_predictions_total",
    "Total number of shipments scored by the anomaly detector",
    ["risk_level"]   # label: LOW, MEDIUM, HIGH, CRITICAL
)

# Histogram: records the distribution of values (latency, sizes).
# Use for: how long did prediction take? -> p50, p95, p99 latency
PREDICTION_LATENCY = Histogram(
    "ml_prediction_latency_seconds",
    "Time taken to score one shipment",
    buckets=[0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0]
)

# ─────────────────────────────────────────────
# MODEL LOADING
# ─────────────────────────────────────────────
MODEL_PATH = os.environ.get("MODEL_PATH", "/app/model/isolation_forest.pkl")

def load_model():
    """
    Load the trained Isolation Forest from disk.
    joblib.load deserializes the sklearn model object.

    Why load at startup instead of on each prediction?
    The model file is ~5MB. Loading it takes ~200ms.
    Loading per-request would add 200ms to every Kafka message.
    Loading once at startup means predictions take ~1ms.
    """
    try:
        model = joblib.load(MODEL_PATH)
        logger.info(f"Model loaded from {MODEL_PATH}")
        return model
    except FileNotFoundError:
        logger.warning(f"Model file not found at {MODEL_PATH}. "
                       f"Service will start but predictions will fail "
                       f"until model is trained and saved.")
        return None

# Module-level model: loaded once, read by every prediction call.
# Thread-safe for reads in CPython due to the GIL.
model = load_model()

# ─────────────────────────────────────────────
# FLASK APP
# ─────────────────────────────────────────────
app = Flask(__name__)

@app.route("/health/liveness")
def liveness():
    """
    Kubernetes liveness probe: "is this process alive?"
    Always returns 200 as long as Flask is running.
    If this endpoint stops responding, Kubernetes restarts the pod.
    """
    return jsonify({"status": "UP"}), 200

@app.route("/health/readiness")
def readiness():
    """
    Kubernetes readiness probe: "is this service ready to serve traffic?"
    Returns 503 if the model hasn't loaded yet — Kubernetes won't
    route traffic to this pod until the model is ready.
    """
    if model is None:
        return jsonify({"status": "DOWN", "reason": "model not loaded"}), 503
    return jsonify({"status": "UP"}), 200

# ─────────────────────────────────────────────
# KAFKA CONSUMER THREAD
# ─────────────────────────────────────────────
def start_kafka_consumer():
    """
    This runs in a background daemon thread.
    It imports KafkaConsumer here (not at top level) so that if
    Kafka is unavailable at startup, the Flask server still starts
    and reports liveness. The consumer will retry on connection failure.

    We'll flesh this out fully in Phase 3 (ML pipeline implementation).
    For now, this is the structural placeholder.
    """
    from kafka.consumer import ShipmentEventConsumer
    consumer = ShipmentEventConsumer(model=model, metrics={
        "predictions_total": PREDICTIONS_TOTAL,
        "prediction_latency": PREDICTION_LATENCY,
    })
    consumer.run()  # blocking loop — runs forever in its thread

# ─────────────────────────────────────────────
# ENTRYPOINT
# ─────────────────────────────────────────────
if __name__ == "__main__":
    # Start Kafka consumer in a background thread.
    # daemon=True: thread dies automatically when the main process exits.
    # Without daemon=True, the container would hang on shutdown
    # waiting for the Kafka thread to finish (it never would).
    kafka_thread = threading.Thread(
        target=start_kafka_consumer,
        name="kafka-consumer",
        daemon=True
    )
    kafka_thread.start()
    logger.info("Kafka consumer thread started")

    # Mount Prometheus metrics at /metrics
    # DispatcherMiddleware routes /metrics to prometheus_client's WSGI app
    # and everything else to our Flask app.
    app_with_metrics = DispatcherMiddleware(app, {
        "/metrics": make_wsgi_app()
    })

    # Start Flask.
    # host="0.0.0.0": listen on all network interfaces (required in Docker).
    # If you use host="127.0.0.1", the container can't receive external traffic.
    port = int(os.environ.get("PORT", 8085))
    logger.info(f"Starting ML detector on port {port}")

    from werkzeug.serving import run_simple
    run_simple(
        hostname="0.0.0.0",
        port=port,
        application=app_with_metrics,
        use_reloader=False,   # False in production — reloader spawns extra processes
        threaded=True         # Handle multiple Flask requests simultaneously
    )
