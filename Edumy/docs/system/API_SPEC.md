# API Specification — Sentiment Analysis

This document details the request/response payloads, validation rules, and error codes for the sentiment analysis endpoint.

---

## 1. Analyze Sentiment Endpoint

- **Route**: `POST /sentiment/analyze`
- **Headers**: `Content-Type: application/json`

### A. Request Payload
```json
{
    "text": "The course content is excellent and very clear!"
}
```

### B. Response Payload (Model Loaded)
```json
{
    "label": "Positive",
    "confidence": 0.9412,
    "scores": {
        "positive": 0.9412,
        "neutral": 0.0345,
        "negative": 0.0243
    },
    "modelVersion": "v1.0.0"
}
```

---

## 2. Input Validation Rules
FastAPI validates request schemas before executing predictions:

| Validation Trigger | Condition | HTTP Response | Description |
|---|---|---|---|
| **Null/Empty text** | `text == ""` | `HTTP 400 Bad Request` | Input review comment cannot be empty |
| **Max characters limit** | `len(text) > 5000` | `HTTP 400 Bad Request` | Text is too long for memory limits |
| **Injection strings** | Matches `<script>`, `javascript:` | `HTTP 400 Bad Request` | Script injection strings rejected |

---

## 3. Server Health Endpoint

- **Route**: `GET /health`
- **Response (Degraded state)**:
  ```json
  {
      "status": "degraded",
      "model_loaded": false,
      "reason": "Model files missing in saved_models directory."
  }
  ```
- **Response (Operational state)**:
  ```json
  {
      "status": "ok",
      "model_loaded": true
  }
  ```
