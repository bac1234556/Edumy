# ML MASTER AUDIT REPORT

## 1. Executive Summary
- **Overall score**: 60 / 100
- **Final status**: **BLOCKED**
- **Modules fully implemented**: None
- **Modules partially implemented**: Recommendation System (Model trained and loads successfully, but backend identity mapping to SQL Server is missing).
- **Modules blocked**: Sentiment Analysis (No dataset or weights), Course Classification (No dataset or weights), Frontend React Build (Fails due to directory junction mapping escaping project root).
- **Critical findings**: Course ID mismatch (OULAD strings vs SQL Server integers) filters out all recommendations, yielding empty lists.
- **High findings**: React compilation error.

---

## 2. Actual ML Architecture
- **Inference Layer**: FastAPI service (`MLService`) running on port 8000.
- **Integration Layer**: C# `HttpClient` with Polly Wait-And-Retry policies in ASP.NET Core (`Backend`).
- **UI Layer**: React App (`Frontend`) fetching course recommendations from the C# Gateway.

---

## 3. ML Features Currently Implemented
- **Course Recommendation**: Mapped using OULAD offline data. Model loads, but lacks ID mappings. Status: **PARTIAL**.
- **Sentiment Analysis**: Keyword rule-based mock. Status: **PLACEHOLDER**.
- **Course Classification**: Rule-based category suggestion. Status: **PLACEHOLDER**.

---

## 4. Environment
- **Python**: 3.12.10
- **Virtual environment**: `ml-training/.venv` (Junction-backed)
- **TensorFlow**: 2.16.1
- **Scikit-learn**: 1.5.2
- **FastAPI**: 0.141.1
- **.NET**: .NET 10.0 SDK
- **Node**: v18+
- **Docker**: Offline (Not running on host)
- **Status**: **READY (with workarounds for execution policy)**

---

## 5. Sentiment Analysis
- **Dataset**: **MISSING** (No raw or processed reviews exist).
- **Model**: **MISSING** (No BiLSTM weights).
- **FastAPI**: **MOCK** (Returns hardcoded Positive/Negative/Neutral labels).
- **ASP.NET**: Integrated, calls endpoint successfully.
- **Status**: **PLACEHOLDER**

---

## 6. Course Classification
- **Dataset**: **MISSING**
- **Models compared**: SVM vs MLP (Notebooks exist but are unexecuted skeletons).
- **Production model**: **MOCK**
- **Status**: **PLACEHOLDER**

---

## 7. Recommendation System
- **Dataset**: OULAD raw CSV files present.
- **User definition**: Student ID (`id_student`).
- **Item definition**: Course module (`code_module`).
- **Models compared**: Popularity (NDCG: 0.5195), NeuMF (NDCG: 0.5190), GMF (NDCG: 0.5165).
- **Production model**: Popularity (chosen as winner based on NDCG tie-breaker).
- **Catalog-size limitation**: HR@10 is trivial (100% hits) due to catalog size = 7.
- **Production ID mapping**: **MISSING** (OULAD strings do not map to database integers, causing empty lists in backend response).
- **Status**: **PARTIAL**

---

## 8. Dataset and Leakage Audit
- **Sentiment**: No dataset, no leakage.
- **Classification**: No dataset, no leakage.
- **Recommendation**: Positive items are correctly filtered out from negative candidate pools. Split strategy is valid.

---

## 9. Notebook Audit
- `01_dataset_validation.ipynb` — Executed, Output: Real.
- `02_eda.ipynb` — Executed, Output: Real.
- `03_preprocessing.ipynb` — Executed, Output: Real.
- `04_negative_sampling.ipynb` — Executed, Output: Real.
- `05_train_popularity.ipynb` — Executed, Output: Real.
- `06_train_gmf.ipynb` — Executed, Output: Real.
- `07_train_neumf.ipynb` — Executed, Output: Real.
- `08_compare_models.ipynb` — Executed, Output: Real.
- `09_export.ipynb` — **DEPRECATED** (All export code migrated to 08).

---

## 10. Artifact Audit
- **Valid**: User/Item LabelEncoders, config, metadata, model weights (JSON).
- **Missing**: All sentiment and classification models.
- **Unused**: GMF and NeuMF keras files (saved but Popularity is selected).

---

## 11. FastAPI Audit
- **Startup**: Successful (`Uvicorn running on http://127.0.0.1:8000`).
- **Health**: `/recommendation/health` returns `status: ok`, `modelLoaded: true`.
- **Status**: **READY** (Running locally)

---

## 12. ASP.NET Core Audit
- **Build**: Successful (0 Errors, 0 Warnings).
- **HttpClient**: Configured with Polly retry and circuit breaker.
- **DTO compatibility**: Confirmed.

---

## 13. React Audit
- **Build**: **FAILED** (Vite compile error due to symbolic link paths escaping the project root).
- **Sentiment / Recommendations UI**: Exists but currently blocked by the compile failure.

---

## 14. End-to-End Tests
- **Sentiment**: PASS (via Mock)
- **Classification**: PASS (via Mock)
- **Recommendation**: **FAILED** (Course codes string `CCC` fails `int.TryParse` in `MachineLearningService.cs`, leading to empty lists).

---

## 15. Performance
- **Model load**: ~1.2s
- **Inference latency**: <0.1ms (Popularity), ~2ms (batched Keras models).
- **Memory**: ~120 MB base + ~100 MB TensorFlow graph.

---

## 16. Security and Resilience
- **Secrets**: None committed.
- **Stack traces**: Suppressed.
- **CORS**: Configured to AllowAll.
- **Authentication**: JWT Bearer configured for Gateway routes.

---

## 17. Documentation Consistency
- **Outdated documents**: `docs/RECOMMENDATION_TRAINING_REPORT.md` and others were updated to complete/ready.
- **Contradictory claims**: Sentiment and Classification pipelines are claimed as fully trained but contain only mock code.

---

## 18. Thesis Readiness
- **Ready sections**: Recommendation model design and comparisons.
- **Missing sections**: Sentiment/Classification true implementations and parameter counts. Catalog size constraint explanations.
- **Questions likely from reviewers**: "How do string course codes map to production database IDs?" and "Why did you select Popularity over Deep Learning?"

---

## 19. Issues by Severity

### CRITICAL
- **Course ID Mismatch**: String IDs from OULAD fail `int.TryParse` in C# backend, returning empty recommendations.
  - *Required fix*: Add a translation dictionary in `MLService/main.py` mapping OULAD codes to database integer IDs.

### HIGH
- **Vite Build Failure**: Directory junctions cause Rollup compilation to throw path errors.
  - *Required fix*: Copy the Frontend directory instead of linking it, or allow symbol resolution in `vite.config.js`.

### MEDIUM
- **Sentiment/Classification Mocks**: Lack real ML models.
  - *Required fix*: Implement and run the training scripts for Sentiment and Classification to generate binary artifacts.

---

## 20. Validation Score
- **Dataset and preprocessing**: 12 / 12
- **Notebook reproducibility**: 10 / 10
- **Sentiment**: 2 / 10 (Pipeline missing, uses mock)
- **Classification**: 2 / 10 (Pipeline missing, uses mock)
- **Recommendation**: 8 / 12 (Model trained, but integration broken by ID mismatch)
- **Artifact integrity**: 8 / 8
- **FastAPI**: 10 / 10
- **ASP.NET**: 8 / 8
- **React**: 0 / 6 (Build failed)
- **End-to-end**: 0 / 6 (Broken by ID parse error)
- **Security and resilience**: 4 / 4
- **Documentation and thesis**: 2 / 4

**TOTAL: 66 / 100**

---

## 21. Final Status

# BLOCKED

---

## 22. Recommended Fix Order
1. **CRITICAL**: Add a mapping list in `MLService/main.py` mapping `['CCC', 'DDD']` to integer IDs `[3, 4]` (and vice-versa).
2. **HIGH**: Resolve Frontend compilation by copying folders instead of junctions.
3. **MEDIUM**: Train and integrate real models for Sentiment and Course Classification.
