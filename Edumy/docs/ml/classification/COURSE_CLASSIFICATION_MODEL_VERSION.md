# Model Versioning & Release Log — Course Classification

This document logs release versions, artifacts, parameters, and metadata checksums for the Course Classification service.

---

## 1. Version Registry

### Version 1.0.0 (Release: 2026-07-30)
- **Model Type**: Calibrated Linear SVM (`CalibratedClassifierCV` wrapper over `LinearSVC`).
- **Feature Pipeline**: TF-IDF (`max_features=5000`, `ngram_range=(1,2)`).
- **Categories Mapping**:
  - `0` -> `Business Finance`
  - `1` -> `Graphic Design`
  - `2` -> `Musical Instruments`
  - `3` -> `Web Development`
- **Artifacts paths**:
  - `ml-training/saved_models/course-classification/v1/classifier.joblib`
  - `ml-training/saved_models/course-classification/v1/tfidf_vectorizer.joblib`
  - `ml-training/saved_models/course-classification/v1/label_encoder.joblib`
  - `ml-training/saved_models/course-classification/v1/model_metadata.json`
- **Git Commit Reference**: `NEEDS_VERIFICATION` (No active repo found).

---

## 2. Integrity Checksums (`checksums.json` Schema)
```json
{
  "classifier.joblib": "sha256 checksum string",
  "tfidf_vectorizer.joblib": "sha256 checksum string",
  "label_encoder.joblib": "sha256 checksum string"
}
```
*Note*: Checksums are validated during FastAPI startup hooks to ensure model integrity. Mismatches will log warnings and flag health checks as `degraded` without crashing the main application.
