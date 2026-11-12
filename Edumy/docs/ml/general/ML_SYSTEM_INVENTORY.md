# Machine Learning System Inventory

This document maps all ML-related components in the repository and classifies their implementation status.

---

## 1. Inventory Summary

| Component | Path | Status | Description |
|---|---|---|---|
| **Sentiment Notebooks** | `ml-training/notebooks/1-sentiment-lstm/` | **PLACEHOLDER** | Contains only markdown cells or placeholder prints. |
| **Sentiment Models** | - | **MISSING** | No trained BiLSTM weights or tokenizers. |
| **Classification Notebooks** | `ml-training/notebooks/2-course-classification/` | **PLACEHOLDER** | Contains skeleton imports and prints only. |
| **Classification Models** | - | **MISSING** | No trained SVM/MLP models or vectorizers. |
| **Recommendation Notebooks** | `ml-training/notebooks/3-course-recommendation/` | **IMPLEMENTED** | All notebooks (01 to 08) are fully executed. |
| **Recommendation Models** | `ml-training/artifacts/recommendation/` | **IMPLEMENTED** | Valid encoders, config, metadata, and models exist. |
| **FastAPI ML Service** | `MLService/` | **PARTIAL** | FastAPI routes are live, but sentiment/classification use mocks. |
| **ASP.NET Core Gateway** | `Backend/` | **IMPLEMENTED** | Full HTTP client integration with Polly error handling. |
| **React UI components** | `Frontend/` | **PARTIAL** | UI consumes recommendations but build fails due to junction paths. |
| **Database Schema** | `Backend/Data/` | **IMPLEMENTED** | SQL Server database context exists. |

---

## 2. Recommendation Artifact Checklist

- `metadata.json`: **IMPLEMENTED** (Valid, points to Popularity model)
- `training_config.json`: **IMPLEMENTED** (Valid, describes epochs/batch)
- `recommendation_metrics.json`: **IMPLEMENTED** (Valid, lists test metrics)
- `user_encoder.joblib`: **IMPLEMENTED** (Valid, maps user IDs to index)
- `item_encoder.joblib`: **IMPLEMENTED** (Valid, maps course codes to index)
- `model.json`: **IMPLEMENTED** (Valid, stores Popularity baseline weights)
- `checksums.json`: **IMPLEMENTED** (Valid, lists SHA256 hashes)
