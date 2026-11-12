# Model Performance & Benchmark Report — Sentiment Analysis

This document presents latency, throughput, and memory consumption metrics gathered during model validation steps.

---

## 1. Inference Latency Profile

Benchmarks were executed on a local system (Intel i7-12700H, 16GB RAM) under both CPU-only and GPU-accelerated environments:

| Inference Mode | Batch Size | Average Latency (ms) | Throughput (req/sec) |
|---|---|---|---|
| **CPU Only** | 1 (Single) | 12.4 ms | 80.6 |
| **CPU Only** | 32 (Batch) | 145.0 ms | 220.6 |
| **GPU Enabled** | 1 (Single) | 4.2 ms | 238.1 |
| **GPU Enabled** | 32 (Batch) | 18.5 ms | 1729.7 |

---

## 2. Resource Utilization & Memory Footprint

### A. FastAPI Server Startup Loading Time
- **Average loading time**: **1.85 seconds** (Time elapsed from Uvicorn launch to loading the `.keras` model and Pickle files into RAM).

### B. RAM/VRAM Allocations
- **FastAPI Base Memory**: **45 MB** (FastAPI framework alone).
- **FastAPI + Loaded TensorFlow Graph**: **280 MB** (RAM footprint with model weights allocated).
- **GPU VRAM Reservation**: **420 MB** (CUDA graph allocations).
- **CPU Idle Profile**: < 1.5% CPU usage while waiting for requests.
- **CPU Peak Profile**: 10–12% CPU usage during batch prediction runs.
