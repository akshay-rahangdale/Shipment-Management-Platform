import json
import logging
from confluent_kafka import Consumer, Producer, KafkaError

from service.anomaly_service import AnomalyService
from config.settings import settings

logger = logging.getLogger(__name__)


class ShipmentEventConsumer:

    def __init__(self, anomaly_service: AnomalyService):
        self.anomaly_service = anomaly_service

        self.consumer = Consumer({
            "bootstrap.servers":  settings.KAFKA_BOOTSTRAP_SERVERS,
            "group.id":           settings.KAFKA_GROUP_ID,
            "auto.offset.reset":  "earliest",
            "enable.auto.commit": False,
        })

        self.producer = Producer({
            "bootstrap.servers": settings.KAFKA_BOOTSTRAP_SERVERS,
            "acks":              "all",
            "retries":           3,
        })

    def run(self):
        self.consumer.subscribe([settings.TOPIC_TRACKING_UPDATES])
        logger.info("Subscribed to topic=%s", settings.TOPIC_TRACKING_UPDATES)

        while True:
            msg = self.consumer.poll(timeout=1.0)

            if msg is None:
                continue

            if msg.error():
                if msg.error().code() != KafkaError._PARTITION_EOF:
                    logger.error("Kafka error: %s", msg.error())
                continue

            try:
                event = json.loads(msg.value().decode("utf-8"))
                self._process(event)
                self.consumer.commit(message=msg)

            except Exception as ex:
                logger.error("Failed to process message offset=%s error=%s",
                             msg.offset(), ex)

    def _process(self, event: dict):
        alert = self.anomaly_service.evaluate(event)

        if alert:
            self.producer.produce(
                topic     = settings.TOPIC_ANOMALY_ALERTS,
                key       = alert["trackingNumber"],
                value     = json.dumps(alert),
                callback  = self._delivery_callback,
            )
            self.producer.flush()

    def _delivery_callback(self, err, msg):
        if err:
            logger.error("Failed to deliver alert to Kafka: %s", err)
        else:
            logger.info("Alert delivered topic=%s partition=%s offset=%s",
                        msg.topic(), msg.partition(), msg.offset())
