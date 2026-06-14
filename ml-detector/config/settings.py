import os


class Settings:
    # Kafka
    KAFKA_BOOTSTRAP_SERVERS = os.environ.get("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    KAFKA_GROUP_ID          = os.environ.get("KAFKA_GROUP_ID", "ml-detector-group")
    TOPIC_TRACKING_UPDATES  = os.environ.get("TOPIC_TRACKING_UPDATES", "tracking.updates")
    TOPIC_ANOMALY_ALERTS    = os.environ.get("TOPIC_ANOMALY_ALERTS", "anomaly.alerts")

    # MongoDB
    MONGO_URI = os.environ.get("MONGO_URI", "mongodb://localhost:27017")
    MONGO_DB  = os.environ.get("MONGO_DB", "tracking_db")

    # Redis
    REDIS_HOST     = os.environ.get("REDIS_HOST", "localhost")
    REDIS_PORT     = int(os.environ.get("REDIS_PORT", "6379"))
    REDIS_PASSWORD = os.environ.get("REDIS_PASSWORD", "")

    # Model
    MODEL_PATH         = os.environ.get("MODEL_PATH", "/app/model/isolation_forest.pkl")
    ANOMALY_THRESHOLD  = float(os.environ.get("ANOMALY_THRESHOLD", "0.72"))
    SLA_WARNING_HOURS  = int(os.environ.get("SLA_WARNING_HOURS", "3"))

    # Flask
    PORT = int(os.environ.get("PORT", "8085"))


settings = Settings()
