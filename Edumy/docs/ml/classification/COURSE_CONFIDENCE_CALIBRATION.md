# Confidence Calibration Strategy — Course Classification

This document specifies the method used to extract calibrated confidence probabilities from the Linear SVM classifier.

---

## 1. Context of Support Vector Classifiers
Linear Support Vector Classifiers (`LinearSVC`) compute decision boundaries by maximizing margins. They do not naturally output probability distributions; they output raw decision values (distance to hyperplane).
Using raw decision values directly as confidence metrics is inaccurate because:
- Values are unbounded.
- Distances do not map linearly to actual error probabilities.

---

## 2. Selected Calibration Method
We wrap the `LinearSVC` instance inside scikit-learn's `CalibratedClassifierCV` using:
- **Method**: Platt scaling (sigmoid calibration).
- **Fitting Phase**: Calibration parameters are fit strictly on the **training fold** using internal cross-validation (e.g. `cv=5`) to prevent leakage. The test set is never used.

```
Linear SVC output ──> Platt Sigmoid Scaling ──> Calibrated Confidence (0.0 to 1.0)
```
- **Fallback**: If calibration models fail to load or are unavailable, the classification API returns `confidence: null` and `confidenceAvailable: false` rather than rendering uncalibrated raw scores.
