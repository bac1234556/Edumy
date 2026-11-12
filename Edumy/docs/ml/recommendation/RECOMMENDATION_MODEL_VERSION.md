# Model Versioning & Release Log — Recommendation

This document logs release versions, parameters, and metadata checksums for the Recommendation System.

---

## 1. Version Registry

### Version 1.0.0 (Release: 2026-07-30)
- **Model Type**: Neural Collaborative Filtering (NeuMF).
- **Embeddings Dimension**: GMF branch (16), MLP branch (32).
- **MLP Architecture Layers**: [64, 32, 16] Dense.
- **Dataset Version**: OULAD anonymisedData release.
- **Artifact Paths**:
  - `ml-training/saved_models/recommendation/model.keras`
  - `ml-training/saved_models/recommendation/user_encoder.joblib`
  - `ml-training/saved_models/recommendation/item_encoder.joblib`

---

## 2. Integrity Checksums (`checksums.json` Schema)
```json
{
  "model.keras": "sha256 hash string",
  "user_encoder.joblib": "sha256 hash string",
  "item_encoder.joblib": "sha256 hash string"
}
```
*Note*: Checksums are validated during model loading.
