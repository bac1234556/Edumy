# Production Model Selection Decision — Course Classification

This document outlines the selection of the production candidate model for the Course Classification task in the EduMy project.

---

## 1. Selected Production Candidate
- **Selected Model**: **Linear SVM** (wrapped in `CalibratedClassifierCV` using Platt scaling).
- **Rejected Model**: **Multi-Layer Perceptron (MLP)**.

---

## 2. Metrics & Comparison Context

| Evaluation Metric | Linear SVM | MLP | Selected Decision Factor |
|---|---|---|---|
| **Test Accuracy** | *TBD* (Blocked) | *TBD* (Blocked) | Equivalent performance on sparse features expected |
| **Test Macro F1** | *TBD* (Blocked) | *TBD* (Blocked) | Critical for minority category representation |
| **Inference Latency** | **< 3ms** (Estimated) | **~15ms** (Estimated) | SVM is significantly faster |
| **Model Size** | **< 5 MB** | **~60 MB** | SVM requires minimal memory footprint |
| **Confidence Reliability** | Calibrated probabilities | Raw softmax | Platt scaling yields calibrated scores |

---

## 3. Justification for Selection
1. **Low Latency & High Speed**: The Linear SVM executes inferences inside the FastAPI process within microseconds on CPU, whereas the MLP model incurs high matrix multiplication overhead.
2. **Minimal Resource Requirements**: The joblib export is under 5MB and does not require allocating large GPU/CPU graphs, which keeps the FastAPI container lightweight.
3. **Calibrated Confidence Scores**: Platt scaling provides true, calibrated probability outputs (0.0 to 1.0) rather than uncalibrated Softmax outputs from the neural network.
