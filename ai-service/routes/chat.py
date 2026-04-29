"""
Chat blueprint — POST /api/ai/chat
Sends a prompt to Groq (LLaMA-3.3-70b) and returns the AI response.
Cached in Redis for 15 minutes per unique prompt hash.
"""

import hashlib
import json
import os

from flask import Blueprint, request, jsonify
from services.groq_service import get_groq_response
from services.cache_service import get_cached, set_cached

chat_bp = Blueprint("chat", __name__)

CACHE_TTL = 900  # 15 minutes


@chat_bp.post("/chat")
def chat():
    body = request.get_json(silent=True) or {}
    prompt: str = body.get("prompt", "").strip()
    context: str = body.get("context", "")   # optional extra context

    if not prompt:
        return jsonify({"error": "prompt is required"}), 400

    # Cache key = SHA-256 of (prompt + context)
    cache_key = "ai:chat:" + hashlib.sha256(
        (prompt + context).encode()
    ).hexdigest()

    cached = get_cached(cache_key)
    if cached:
        return jsonify({"response": cached, "cached": True}), 200

    try:
        response_text = get_groq_response(prompt, context)

        # groq_service returns a JSON string with is_fallback=True on failure
        try:
            parsed = json.loads(response_text)
            if isinstance(parsed, dict) and parsed.get("is_fallback"):
                return jsonify(parsed), 503  # Service Unavailable — AI is down
        except (json.JSONDecodeError, TypeError):
            pass  # Normal string response — not JSON, continue normally

        set_cached(cache_key, response_text, ttl=CACHE_TTL)
        return jsonify({"response": response_text, "cached": False}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@chat_bp.post("/summarize")
def summarize():
    """Summarise an audit finding or document excerpt."""
    body = request.get_json(silent=True) or {}
    text: str = body.get("text", "").strip()

    if not text:
        return jsonify({"error": "text is required"}), 400

    prompt = (
        "You are an expert auditor. Summarise the following audit content "
        "concisely in 3-5 bullet points:\n\n" + text
    )
    try:
        summary = get_groq_response(prompt, context="")
        return jsonify({"summary": summary}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
