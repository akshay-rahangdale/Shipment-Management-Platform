import sys
import os
import logging

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from model.trainer import train
from config.settings import settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s - %(message)s"
)

if __name__ == "__main__":
    output_path = sys.argv[1] if len(sys.argv) > 1 else settings.MODEL_PATH
    train(output_path)
    print(f"Model saved to {output_path}")
