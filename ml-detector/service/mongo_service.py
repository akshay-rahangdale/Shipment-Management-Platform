import logging
from pymongo import MongoClient
from config.settings import settings

logger = logging.getLogger(__name__)


class MongoService:

    def __init__(self):
        self.client = MongoClient(settings.MONGO_URI)
        self.collection = self.client[settings.MONGO_DB]["tracking_records"]

    def get_tracking_record(self, tracking_number: str) -> dict | None:
        try:
            record = self.collection.find_one(
                {"trackingNumber": tracking_number}
            )
            return record
        except Exception as ex:
            logger.error("Failed to fetch tracking record %s: %s", tracking_number, ex)
            return None

    def update_ml_signals(self, tracking_number: str, ml_signals: dict):
        try:
            self.collection.update_one(
                {"trackingNumber": tracking_number},
                {"$set": {"mlSignals": ml_signals}}
            )
        except Exception as ex:
            logger.error("Failed to update ml_signals for %s: %s", tracking_number, ex)
