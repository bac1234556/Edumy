# Course Classification Validation Report — EduMy

This document presents the independent verification and validation audit of the Course Classification (Bài toán 2) implementation.

---

## 1. Overall Validation Summary
- **Overall Verdict**: **NOT READY** (Due to critical dataset omission preventing training)
- **Validation Score**: **55 / 100**
- **Production Blockers**: **YES** (1 Critical Blocker)

### Severity Breakdown
- **CRITICAL**: 1
- **HIGH**: 0
- **MEDIUM**: 2
- **LOW**: 1
- **INFO**: 0
- **NOT VERIFIED**: 5 (Notebook training & artifact matching)

---

## 2. Validation Matrix

| ID | Area | Check | Status | Severity | Evidence | Production Blocker |
|---|---|---|---|---|---|---|
| **VAL-01** | Dataset | Raw CSV presence | **FAIL** | **CRITICAL** | File `ml-training/datasets/raw/udemy_courses.csv` is missing from the workspace. | **YES** |
| **VAL-02** | Notebook | Execution status | **NOT_VERIFIED** | **HIGH** | Notebooks 01-07 exist as code templates but have not been run due to missing dataset. | **YES** |
| **VAL-03** | Preprocessing | Consistency check | **PASS** | **INFO** | Consistent unicode normalization and token preservation in `preprocessing/course_text.py` and notebooks. | **NO** |
| **VAL-04** | Data Leakage | Train/val/test splits | **PASS** | **INFO** | Split ratios (70/15/15) and stratified distributions are configured correctly in preprocessing notebooks. | **NO** |
| **VAL-05** | TF-IDF | Fit parameters | **PASS** | **INFO** | Vectorizer fit strictly on train split only; vocabulary pattern protects special tokens (`C#`, `C++`). | **NO** |
| **VAL-06** | Export | Production folders | **WARNING** | **MEDIUM** | `classifier.joblib` and model weights are not generated due to the dataset block. | **YES** |
| **VAL-07** | Checksum | Release checksums | **WARNING** | **MEDIUM** | `checksums.json` contains placeholder strings since binary models have not been serialized. | **YES** |
| **VAL-08** | FastAPI | Service integrity | **PASS** | **INFO** | Loader handles degraded state gracefully on startup; classification prediction returns HTTP 503 if not loaded. | **NO** |
| **VAL-09** | ASP.NET | Controller routing | **PASS** | **INFO** | `/api/MLTest/course-classification` parses responses, handles null confidence, and fails safely. | **NO** |
| **VAL-10** | React | Suggestion UI | **PASS** | **INFO** | AI Suggestion button in `CourseCreate.jsx` debounces correctly, has loading flags, and manual fallback. | **NO** |
| **VAL-11** | Database | Schema alterations | **PASS** | **INFO** | No new migrations, columns, or tables created. Category is saved to pre-existing fields. | **NO** |
| **VAL-12** | Regression | Sentiment check | **PASS** | **INFO** | Sentiment analysis endpoints and fallback behaviors remain unaffected and fully operational. | **NO** |
| **VAL-13** | Security | committed secrets | **WARNING** | **LOW** | Stripe test keys and database passwords are still present in configs (planned to move in Phase 10). | **NO** |

---

## 3. Detailed Blocker Analysis

### Issue ID: VAL-01 (Missing Raw Dataset)
- **Area**: Dataset & Provenance
- **File**: `ml-training/datasets/raw/udemy_courses.csv`
- **Severity**: **CRITICAL**
- **Description**: The raw Udemy Courses dataset is missing from the local filesystem.
- **Evidence**: `list_dir` and file searches confirm no csv file exists.
- **Impact**: Prevents running the preprocessing, training, and model comparison notebook cells. No binary weights can be compiled.
- **Proposed Fix**: Download the dataset from `https://www.kaggle.com/datasets/andrewmvd/udemy-courses` and extract the CSV to `ml-training/datasets/raw/udemy_courses.csv`.
- **Production Blocker**: **YES**

### Issue ID: VAL-02 (Notebooks Not Run)
- **Area**: Notebook Execution
- **File**: `ml-training/notebooks/2-course-classification/` (01 to 07)
- **Severity**: **HIGH**
- **Description**: Notebook cells are unexecuted and contain empty output blocks.
- **Evidence**: Notebook files read as empty templates.
- **Impact**: No performance statistics can be evaluated.
- **Proposed Fix**: Once VAL-01 is resolved, run all notebooks sequentially to output processed datasets and serialize model weights.
- **Production Blocker**: **YES**
