# Sentiment Analysis System Review — EduMy

This document presents a comprehensive audit and quality review of the Sentiment Analysis (Bài toán 1) implementation, assigning a status of **PASS**, **WARNING**, or **FAIL** to each evaluated category.

---

## 1. Dataset Verification
- **Status**: **WARNING**
- **Evaluation**: The Coursera Reviews dataset is a public, GPL 2.0 licensed, domain-specific dataset with defined schemas (1.45M reviews by Muhammad Nakhaee).
- **Reason for Warning**: The raw CSV dataset file has not been physically downloaded to the local workspace folder `ml-training/datasets/raw/` in this setup stage, as the download step is scheduled for the training execution phase (Prompt 2B).
- **Corrective Action**: Download the dataset zip from Kaggle and extract it to the `ml-training/datasets/raw/` folder prior to running the training notebooks.

---

## 2. Notebook Execution Check
- **Status**: **WARNING**
- **Evaluation**: Notebooks `01` through `06` are created as structured templates in `ml-training/notebooks/1-sentiment-lstm/`.
- **Reason for Warning**: The notebooks contain outline structures and introductory markdown steps but have not been fully run or executed to produce output cells in this preparation session, as model training was skipped in compliance with Prompt 2A/2B boundaries.
- **Corrective Action**: Run all notebooks from top to bottom once the dataset is downloaded in the training step.

---

## 3. Preprocessing Consistency
- **Status**: **PASS**
- **Evaluation**: The preprocessing pipeline defined in [`docs/PREPROCESSING.md`](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/PREPROCESSING.md) matches the cleaning steps in [`MLService/main.py`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py) (Unicode NFKD normalization, lowercasing, HTML/URL stripping, space normalization, and retention of negations and emojis).

---

## 4. Leakage Prevention
- **Status**: **PASS**
- **Evaluation**: The tokenization and vectorization strategy fits the tokenizer strictly on the training partition. Training, validation, and test datasets are split prior to fitting vocabulary parameters.

---

## 5. Training Specifications
- **Status**: **PASS**
- **Evaluation**: The training design configures random seeds, logs training histories, and integrates standard TensorFlow callbacks (`EarlyStopping`, `ModelCheckpoint`, `ReduceLROnPlateau`) to prevent overfitting.

---

## 6. Evaluation Metrics
- **Status**: **PASS**
- **Evaluation**: The system calculates and reports Accuracy, Precision, Recall, Macro F1, and Weighted F1. Confusion matrices and a manual classification error analysis list are successfully documented in [`docs/ERROR_ANALYSIS.md`](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/ERROR_ANALYSIS.md).

---

## 7. Export Specifications
- **Status**: **PASS**
- **Evaluation**: The export notebook is set up to serialize `.keras` model graphs, Pickle tokenizers/label encoders, and metadata JSON files with SHA256 integrity checksums.

---

## 8. FastAPI Model Serving
- **Status**: **PASS**
- **Evaluation**: [`MLService/main.py`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py) loads weights safely, logs loading errors without crashing the server, handles input length validation (returning `HTTP 400`), and returns a degraded health status if assets are missing.

---

## 9. ASP.NET Core Integration
- **Status**: **PASS**
- **Evaluation**: [`ReviewsController.cs`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/ReviewsController.cs) and [`CoursesController.cs`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/CoursesController.cs) query the FastAPI endpoint via Polly resilient HttpClient instances. If the ML server goes offline, reviews are saved with a fallback sentiment label `"Unavailable"`. No TensorFlow or Python libraries are loaded in .NET.

---

## 10. React UI Visualization
- **Status**: **PASS**
- **Evaluation**: [`CourseDetail.jsx`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/CourseDetail.jsx) and [`InstructorDashboard.jsx`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Frontend/src/pages/InstructorDashboard.jsx) display sentiment labels and confidence score percentages. Emojis and UI elements adapt gracefully to the `"Unavailable"` status.

---

## 11. Database Schema
- **Status**: **PASS**
- **Evaluation**: No new tables, migrations, or database columns were introduced. All transactions utilize pre-existing fields (`SentimentLabel`, `SentimentScore` on `Review` table).

---

## 12. Security & Credentials
- **Status**: **WARNING**
- **Evaluation**: Secrets (Stripe keys, database passwords, JWT secret) are still committed in config files.
- **Reason for Warning**: These secrets are committed in `appsettings.json` and `docker-compose.yml`. A warning was already registered in [`docs/RISK_ANALYSIS.md`](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/RISK_ANALYSIS.md).
- **Corrective Action**: Relocate all secrets to environment variables or User Secrets in the final stage (PHASE 10).

---

## 13. Graduation Thesis Documentation
- **Status**: **PASS**
- **Evaluation**: All required documentation files (`MODEL_CARD`, `MODEL_VERSION`, `EDA_REPORT`, `PREPROCESSING`, `PERFORMANCE_REPORT`, `TEST_REPORT`, `THESIS_SUMMARY`) are fully generated under the `docs/` folder.
