import logging
import threading
import joblib
from flask import Flask, jsonify
from prometheus_client import make_wsgi_app, Counter, Histogram
from werkzeug.middleware.dispatcher import DispatcherMiddleware
from werkzeug.serving import run_simple

from config.settings import settings
from model.predictor import Predictor
from service.mongo_service import MongoService
from service.anomaly_service import AnomalyService
from kafka.consumer import ShipmentEventConsumer

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(threadName)s] %(levelname)s %(name)s - %(message)s"
)
logger = logging.getLogger(__name__)

PREDICTIONS_TOTAL = Counter(
    "ml_predictions_total",
    "Total shipments scored",
    ["risk_level"]
)

PREDICTION_LATENCY = Histogram(
    "ml_prediction_latency_seconds",
    "Time to score one shipment",
    buckets=[0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0]
)

predictor       = Predictor()
mongo_service   = MongoService()
anomaly_service = AnomalyService(predictor=predictor, mongo=mongo_service)
consumer        = ShipmentEventConsumer(anomaly_service=anomaly_service)

app = Flask(__name__)


@app.route("/health/liveness")
def liveness():
    return jsonify({"status": "UP"}), 200


@app.route("/health/readiness")
def readiness():
    if not predictor.is_ready():
        return jsonify({"status": "DOWN", "reason": "model not loaded"}), 503
    return jsonify({"status": "UP"}), 200


if __name__ == "__main__":
    kafka_thread = threading.Thread(
        target=consumer.run,
        name="kafka-consumer",
        daemon=True
    )
    kafka_thread.start()
    logger.info("Kafka consumer thread started")

    app_with_metrics = DispatcherMiddleware(app, {"/metrics": make_wsgi_app()})

    run_simple(
        hostname="0.0.0.0",
        port=settings.PORT,
        application=app_with_metrics,
        use_reloader=False,
        threaded=True
    )
