# Thesis Summary Report — Course Classification (Linear SVM vs MLP)

**Project Title**: EduMy eLearning Platform Integration
**Academic Sub-discipline**: Supervised Machine Learning / Text Classification
**Thesis Section**: Chapter V — Automatic Course Classification

---

## 1. Abstract
This chapter discusses the comparative design and implementation of an automatic course category suggestion module in the EduMy eLearning application. The system processes title and description metadata using TF-IDF feature extraction, evaluating a Linear Support Vector Machine (SVM) against a Multi-Layer Perceptron (MLP) network to balance prediction accuracy against runtime latency constraints.

---

## 2. Preprocessing & Feature Engineering
Input strings (Title + Description) are cleaned using a consistent, non-destructive pipeline preserving tech tokens (`C#`, `C++`, `.NET`) before vectorization:
- **Vectorizer**: `TfidfVectorizer` (max features: 5000, n-grams: 1 to 2, min document frequency: 2).
- **Leakage Prevention**: TF-IDF vocabulary mapping is fit strictly on the training partition.

---

## 3. Evaluated Architectures

### A. Linear SVM (Platt Calibrated)
- **Model**: `LinearSVC` wrapped in `CalibratedClassifierCV` to calculate confidence values.
- **Benefits**: High execution speeds (under 3ms), extremely low memory overhead (< 5MB), and calibrated probability maps.

### B. Multi-Layer Perceptron (MLP)
- **Model**: Keras feedforward network with three Dense layers (512, 256, 128 units) with dropout (0.3, 0.3, 0.2) to prevent overfitting.
- **Loss**: `SparseCategoricalCrossentropy`.

---

## 4. Production Integration
The platform implements a decoupled tier architecture:
- **FastAPI**: Hosts the calibrated SVM model. If assets are missing on startup, it degrades gracefully without crashing.
- **ASP.NET Core Gateway**: Calls the suggestion endpoint. If the FastAPI service is offline, it fails safely, allowing instructors to select categories manually.
- **React Frontend**: Offers a single-button "Gợi ý danh mục bằng AI" with loading states and manual dropdown adjustments.
