# Deployment Guide — Course Recommendation

This document specifies the deployment checklist, configuration keys, and startup loader configurations.

---

## 1. Recommendation Model Loader
- **Strategy**: Singleton & Thread-safe lazy loader inside `MLService/main.py`.
- **Logic**: Loads the Keras model (`saved_models/recommendation/model.keras`), user encoder, and item encoder on demand.
- **Fail-safe state**: If the model files do not exist, it marks `recommendation_loaded = False` and redirects queries to a Popularity-based non-personalized baseline.

---

## 2. Environment Configurations
- **Environment variables**:
  - `REC_MODEL_PATH`: Location of Keras weights (default: `saved_models/recommendation/model.keras`).
  - `REC_ENCODERS_DIR`: Directory containing student and course item lookup maps.
