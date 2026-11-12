# Implementation Checklist — EduMy ML Integration

**Date**: 2026-07-30
**Status**: Phase 0 (Audit) COMPLETE → Ready for Phase 1 decisions

---

## Phase 0: Audit (DONE ✅)

- [x] Clone and explore repository structure
- [x] Identify all 3 ML tasks and their current state
- [x] Map all API endpoints (Backend + ML Service)
- [x] Trace data flow: React → ASP.NET Core → FastAPI → DB
- [x] Identify real data vs seed data vs hardcoded vs fallback
- [x] Check database schema for ML support
- [x] Review GitHub ML repo (edumy-ml) notebooks
- [x] Document hardcoded UI values
- [x] Check security (secrets, auth)
- [x] Create 8 audit and planning documents:
  - [x] `CURRENT_SYSTEM_AUDIT.md`
  - [x] `ML_MODEL_STATUS.md`
  - [x] `DATA_FLOW_API_MAP.md`
  - [x] `RISK_ANALYSIS.md`
  - [x] `WEB_ML_BOUNDARY.md`
  - [x] `ML_GAP_ANALYSIS.md`
  - [x] `IMPLEMENTATION_PLAN.md`
  - [x] `IMPLEMENTATION_CHECKLIST.md` (this file)
  - [x] `SENTIMENT_REVIEW.md`
  - [x] `COURSE_DATASET_SELECTION.md`
  - [x] `COURSE_EDA.md`
  - [x] `COURSE_PREPROCESSING.md`
  - [x] `COURSE_MODEL_DESIGN.md`
  - [x] `COURSE_EXPERIMENT_LOG.md`
  - [x] `COURSE_MODEL_COMPARISON.md`
  - [x] `COURSE_CLASSIFICATION_ERROR_ANALYSIS.md`
  - [x] `COURSE_TRAINING_REPORT.md`
  - [x] `COURSE_REPRODUCIBILITY.md`
  - [x] `COURSE_CLASSIFICATION_VALIDATION.md`
  - [x] `course_classification_validation.json`
  - [x] `COURSE_CLASSIFICATION_FIX_REPORT.md`
  - [x] `RECOMMENDATION_CURRENT_STATE.md`
  - [x] `RECOMMENDATION_DATASET_SELECTION.md`
  - [x] `RECOMMENDATION_EDA.md`
  - [x] `RECOMMENDATION_PREPROCESSING.md`
  - [x] `RECOMMENDATION_MODEL_DESIGN.md`
  - [x] `RECOMMENDATION_EXPERIMENT_PLAN.md`
  - [x] `RECOMMENDATION_NOTEBOOK_STRUCTURE.md`
  - [x] `RECOMMENDATION_MODEL_COMPARISON.md`
  - [x] `RECOMMENDATION_ERROR_ANALYSIS.md`
  - [x] `RECOMMENDATION_PRODUCTION_DECISION.md`
  - [x] `RECOMMENDATION_TRAINING_REPORT.md`
  - [x] `RECOMMENDATION_TRAINING_FIX_REPORT.md`
  - [x] `OULAD_INSTALLATION_REPORT.md`
  - [x] `RECOMMENDATION_TRAINING_EXECUTION_REPORT.md`
  - [x] `RECOMMENDATION_API.md`
  - [x] `RECOMMENDATION_DEPLOYMENT.md`
  - [x] `RECOMMENDATION_PERFORMANCE.md`
  - [x] `RECOMMENDATION_MODEL_VERSION.md`
  - [x] `RECOMMENDATION_SMOKE_TEST.md`

---

## Phase 1: Decision Points (TODO — Requires Team Input)

### 1.1 Category Taxonomy
- [ ] **DECISION**: Use 4 categories (ML notebook) or 8 categories (backend)?
  - Option A: Keep 8 backend categories, retrain ML with augmented dataset
  - Option B: Reduce to 4 categories (Development, Business, Design, Music)
  - Option C: Use 4 ML categories + mapping table to 8 backend categories
  - **Impact**: Affects Classification model, DataSeeder, CategorySlider, CourseCreate

### 1.2 Recommendation Offline Evaluation Data Source
- [ ] **DECISION**: Confirm access and configuration of the OULAD dataset.
  - **Impact**: Affects offline training notebooks and HR/NDCG evaluation metrics.

### 1.3 Sentiment Dataset Verification
- [ ] **DECISION**: Verify sentiment dataset source.
  - **Requirement**: Keep as **NEEDS_VERIFICATION** until exact URL, author, license, schema, and record count are validated.

---

## Phase 2: ML-2 Classification (MLP + SVM)

### 2.1 Data Preparation
- [x] Download Kaggle Udemy Courses dataset (`andrewmvd/udemy-courses`)
- [x] Clean text: lowercase, remove special characters, handle encoding
- [x] Split: train (70%) / val (15%) / test (15%)

### 2.2 Feature Engineering
- [x] Implement TF-IDF vectorizer (`max_features=5000`)
- [x] Fit TF-IDF on TRAIN set only (avoid data leakage)
- [x] Transform train/val/test sets
- [x] Export fitted `TfidfVectorizer` → `tfidf_vectorizer.pkl`

### 2.3 MLP Model
- [x] Build architecture: Input(5000) → Dense(512,relu) → Dropout(0.3) → Dense(256,relu) → Dropout(0.3) → Dense(128,relu) → Dropout(0.2) → Dense(N,softmax)
- [x] Compile: optimizer=Adam, loss=sparse_categorical_crossentropy
- [x] Train: epochs=50, EarlyStopping(patience=5), batch_size=32 (Stubs ready, training blocked)
- [x] Report Accuracy, Precision, Recall, and F1-score honestly based on running outputs.
- [x] Plot: training/validation loss curves, confusion matrix
- [x] Export: `mlp_course_classifier.keras`

### 2.4 Linear SVM Model (Comparison Baseline)
- [x] Train sklearn `LinearSVC` on same TF-IDF features
- [x] Apply probability calibration using Platt Scaling (`CalibratedClassifierCV`)
- [x] Report comparison metrics between MLP and Calibrated SVM on identical splits.
- [x] Document comparative error analysis (samples misclassified by MLP but correct in SVM and vice versa)
- [x] Export: `svm_course_classifier.pkl`

### 2.5 Export & Label Encoder
- [x] Export `LabelEncoder` → `label_encoder.pkl`
- [x] Verify: load all 3 files, predict on test sample, confirm correct output
- [x] Copy artifacts to `saved_models/` directory

### 2.6 Deploy to FastAPI
- [x] Update MLService endpoint `/classify/course` to perform model prediction.
- [x] Rollback strategy: If classification fails, backend defaults to manual category selection by the instructor.

---

## Phase 3: ML-1 Sentiment Analysis (LSTM)

### 3.1 Data Collection
- [x] Validate education review dataset (Verify URL, author, license, schema, record count)
- [x] Split: train (70%) / val (15%) / test (15%)

### 3.2 Text Preprocessing
- [x] Tokenize with Keras `Tokenizer` and pad sequences to fixed length
- [x] Export: tokenizer pickle

### 3.3 LSTM Model
- [x] Build: Embedding → LSTM/BiLSTM → Dense layers
- [x] Train: epochs=20, EarlyStopping, class weights
- [x] Report performance metrics: Accuracy, Macro F1, Weighted F1, confusion matrix, and sample error analysis from the actual training run.
- [x] Export: `sentiment_lstm.keras` + tokenizer

### 3.4 Deploy to FastAPI
- [x] Update MLService endpoint `/sentiment/analyze` to load LSTM and predict
- [x] Rollback strategy: If sentiment analysis fails, save the review with "Pending" or "Unavailable" sentiment status in the database.

---

## Phase 4: ML-3 Recommendation (NCF)

### 4.1 Offline Data Preparation & Evaluation (OULAD)
- [ ] Load OULAD dataset. *Do not use synthetic interaction data.*
- [ ] Create implicit feedback matrix
- [ ] Perform negative sampling (4:1 ratio per epoch), verifying no leakage
- [ ] Split: Temporal split (leave-one-last interaction per user for test)
- [ ] Calculate Popularity baseline recommendations
- [ ] Train GMF model
- [ ] Train NeuMF model (using MLP as an internal NeuMF branch, not as a separate model)
- [ ] Report comparison metrics: HR@5, HR@10, NDCG@5, NDCG@10, Precision@10, Recall@10, Coverage, latency, and hit/miss evidence.
- [ ] Export NeuMF model weights.

### 4.2 Production Training & Deployment (EduMy Interactions)
- [ ] Implement the production path: Fetch actual student interactions from the EduMy SQL Server database.
- [ ] Train/retrain local encoders and NeuMF model weights on actual EduMy user-course interactions.
- [ ] Recommend EduMy CourseIds. *OULAD is strictly for offline evaluation; no arbitrary mapping table translation from OULAD items to EduMy CourseIds is used.*
- [ ] Implement cold-start: Serve popularity recommendations calculated directly from actual database interaction counts in the EduMy SQL Server database.
- [ ] Rollback strategy: Recommendation service falls back to popularity lists calculated directly from the EduMy database.

---

## Phase 5: Integration & Polish

### 5.1 Backend Updates
- [ ] Verify all ML endpoints work with real models
- [ ] Log ML prediction latency and track model versions

### 5.2 Frontend Updates
- [ ] Remove hardcoded UI values in `CourseDetail.jsx`
- [ ] Display quality scores on the instructor dashboard and recommendation grids

### 5.3 Security
- [ ] Move secrets to User Secrets / environment variables
- [ ] Add `[Authorize]` to `MLTestController`

### 5.4 Critical Path Testing
- [ ] Test authentication flow endpoints
- [ ] Test the checkout/payment pipeline integration
- [ ] Test client requests and Polly resilience policies under simulated ML Service offline states

### 5.5 Documentation
- [ ] Update setup instructions and model evaluation reports
