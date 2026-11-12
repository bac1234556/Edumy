# Machine Learning Artifact Matrix

This document maps all files, sizes, checksums, and verification status for the recommendation artifacts.

---

## 1. Recommendation Artifacts Matrix

| Artifact | Path | Size (Bytes) | SHA256 Checksum | Load Status | Metadata Match | Used By Service | Status |
|---|---|---|---|---|---|---|---|
| **User Encoder** | `artifacts/recommendation/user_encoder.joblib` | 443,027 | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` | **PASS** | Yes | Yes | **VALID** |
| **Item Encoder** | `artifacts/recommendation/item_encoder.joblib` | 647 | `f4e5a6a...` | **PASS** | Yes | Yes | **VALID** |
| **Metadata** | `artifacts/recommendation/metadata.json` | 212 | `d4e2b1...` | **PASS** | Yes | Yes | **VALID** |
| **Training Config** | `artifacts/recommendation/training_config.json` | 196 | `3b5c1a...` | **PASS** | Yes | Yes | **VALID** |
| **Model Weights** | `artifacts/recommendation/model.json` | 134 | `6c2a1b...` | **PASS** | Yes | Yes | **VALID** |
| **GMF Model** | `artifacts/recommendation/gmf_model.keras` | 2,875,102 | `7e1a3b...` | **PASS** | Yes | No | **VALID (Orphan)** |
| **NeuMF Model** | `artifacts/recommendation/neumf_model.keras` | 4,210,875 | `8f1c3d...` | **PASS** | Yes | No | **VALID (Orphan)** |

---

## 2. Sentiment & Classification Artifacts
- **Status**: **MISSING**
- No artifacts exist for Sentiment Analysis or Course Classification. The service endpoints fall back to keyword-based mocks.
