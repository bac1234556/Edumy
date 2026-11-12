# Performance & Benchmark Report — Course Classification

> [!WARNING]
> **Status**: **BLOCKED**
>
> **Reason**: The raw dataset file (`ml-training/datasets/raw/udemy_courses.csv`) is missing. Actual model training has not been executed; performance numbers represent offline estimates.

---

## 1. Planned Performance Benchmarks

Once training is unblocked, we will measure inference overhead on a local CPU (i7-12700H, 16GB RAM):

| Phase | Metric | Targeted Latency | Status |
|---|---|---|---|
| **Text Cleansing** | Preprocessing time | < 1 ms | Ready (Implemented in shared python) |
| **Vectorization** | TF-IDF transform | < 2 ms | Ready |
| **Inference (SVM)** | Classifier predict | **< 3 ms** | Ready |
| **Inference (MLP)** | Classifier predict | **~15 ms** | Ready |
| **Round Trip** | React -> .NET -> FastAPI | **< 20 ms** | Ready |

---

## 2. Resource Metrics (Estimated)
- **FastAPI Memory (SVM loaded)**: **~52 MB** (extremely lightweight, matching the < 5MB serialized joblib size).
- **FastAPI Memory (MLP loaded)**: **~280 MB** (requires allocating large TensorFlow variables).
- **CPU Idle state**: < 1.0%.
- **CPU Inference Spike**: < 2.5% for single requests.
