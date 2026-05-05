import os
import redis
import json
from dotenv import load_dotenv

load_dotenv()

_local_cache = {}

redis_host = os.getenv("REDIS_HOST", "localhost")
redis_port = int(os.getenv("REDIS_PORT", 6379))
redis_password = os.getenv("REDIS_PASSWORD")

try:
    _redis_client = redis.Redis(
        host=redis_host,
        port=redis_port,
        password=redis_password,
        decode_responses=True,
        socket_timeout=2
    )
    _redis_client.ping()
    _use_redis = True
except Exception:
    _use_redis = False
    print("Warning: Redis not available. Using local in-memory cache.")

def get_cached(key: str) -> str | None:
    if _use_redis:
        try:
            return _redis_client.get(key)
        except Exception:
            return None
    return _local_cache.get(key)

def set_cached(key: str, value: str, ttl: int = 900):
    if _use_redis:
        try:
            _redis_client.setex(key, ttl, value)
            return
        except Exception:
            pass
    _local_cache[key] = value
