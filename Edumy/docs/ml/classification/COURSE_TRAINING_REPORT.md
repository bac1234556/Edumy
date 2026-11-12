# Course Classification Training Report

> [!WARNING]
> **Status**: **BLOCKED**
>
> **Reason**: The raw dataset file (`ml-training/datasets/raw/udemy_courses.csv`) is missing from the local workspace. Training has not started.

---

## Planned Architecture Configurations

### 1. Data Splitting Split
- **Split Ratio**: 70% Train, 15% Validation, 15% Test.
- **Method**: Stratified split according to subject distributions.

### 2. Feature Extraction (TF-IDF)
- **Token Pattern**: Retain special characters to preserve language markers (`c#`, `c++`, `.net`).
- **Dimensions**: Maximum features capped at 5000.

### 3. Classifiers Deployed
- **Linear SVM**: Platt-calibrated `LinearSVC`.
- **MLP**: Feedforward dense layers with dropout and early stopping thresholds.
