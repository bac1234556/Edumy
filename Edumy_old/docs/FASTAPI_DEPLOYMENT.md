# FastAPI Model Serving Deployment Guide — Sentiment Analysis

This document describes how the FastAPI inference server mounts, loads, and processes sentiment analysis queries using the trained BiLSTM model.

---

## 1. Startup & Loading Mechanism

FastAPI uses startup lifecycle hooks to load the deep learning files into RAM, ensuring high-speed prediction without reading weights from disk on each HTTP request.

```
                    FastAPI Server Startup Event
                                 │
                                 ▼
                 [ Read saved_models/ Directory ]
           Are model.keras, tokenizer.pkl, label_encoder.pkl present?
                                 ├── Yes ──> [ Load Model ] ──> Status: OK
                                 └── No  ──> [ Degraded Mode ] ──> Status: DEGRADED
```

- **Graceful Fail-safe**: If the `.keras` model or `.pkl` tokenizer files are missing, the server **does not crash**. Instead, it logs the exception, sets a flag `MODEL_LOADED = False`, and sets the server health status to `degraded`.
- **API Response on Failure**: If a client requests sentiment classification while the server is degraded, it throws an `HTTP 503 Service Unavailable` error, alerting the caller to fallback to database preservation modes without dropping reviews.

---

## 2. Environment Variables & Port Configuration

The server parses host parameters dynamically:
- **Default Port**: `8000` (aligned with `docker-compose.yml`).
- **Uvicorn Command**:
  ```bash
  uvicorn main:app --host 0.0.0.0 --port 8000 --reload
  ```

---

## 3. Container Deployment (Docker)

The service is defined in `docker-compose.yml`:
```yaml
  mlservice:
    build:
      context: ./MLService
      dockerfile: Dockerfile
    container_name: edumy_mlservice
    ports:
      - "8000:8000"
    environment:
      - PORT=8000
```
This isolates the TensorFlow environment in a separate container, preventing resource contention with the .NET backend.
