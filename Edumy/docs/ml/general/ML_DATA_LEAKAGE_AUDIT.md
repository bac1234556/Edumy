# Machine Learning Data Leakage & Split Audit

This audit evaluates split integrity and checks for potential data leakage across the three ML pipelines.

---

## 1. Split Integrity Matrix

| Check | Module | Evidence | Status | Severity |
|---|---|---|---|---|
| **Test set contamination in preprocessing** | Recommendation | LabelEncoder fits on all data; however, this is acceptable for identity maps. Train/val/test data splits are loaded dynamically in notebooks. | **PASS** | Info |
| **Negative item leakage** | Recommendation | Explicitly filters out positive items from the negative candidates pool using `all_items - {pos_i}`. | **PASS** | Info |
| **Sentiment overlap** | Sentiment | Sentiment pipeline is a placeholder. No actual split exists. | **NOT APPLICABLE** | Medium |
| **Classification vocabulary** | Classification | Classification pipeline is a placeholder. No actual split exists. | **NOT APPLICABLE** | Medium |
| **Popularity Leakage** | Recommendation | Popularity baseline only computes frequencies over the training split (`train_df`), preventing test set target leakage. | **PASS** | Info |
