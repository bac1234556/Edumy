# Implementation Plan — EduMy ML Integration

This document outlines the multi-phase roadmap to train, evaluate, deploy, and integrate real Machine Learning models into the EduMy eLearning platform.

---

## Phase Breakdown

### PHASE 1 – Audit and Planning
- **Objective**: Conduct a comprehensive audit of the web codebase, database schema, and existing ML stubs, identifying gaps and establishing integration architectures.
- **Inputs**: Raw codebase, mock ML files, and specification parameters.
- **Tasks**:
  1. Inspect directory structure, dependencies, and git configuration.
  2. Map all React, ASP.NET Core, and FastAPI endpoints.
  3. Formulate the boundary analysis, gap analysis, and implementation roadmap.
- **Existing Files Involved**:
  - `Backend/Program.cs`
  - `Frontend/src/App.jsx`
  - `MLService/main.py`
- **Expected New Files**:
  - `docs/CURRENT_SYSTEM_AUDIT.md`
  - `docs/DATA_FLOW_API_MAP.md`
  - `docs/ML_GAP_ANALYSIS.md`
  - `docs/WEB_ML_BOUNDARY.md`
  - `docs/IMPLEMENTATION_PLAN.md`
- **Completion Criteria**: 100% of documentation mapped and approved by the team.
- **Dependencies**: None.
- **Risks**: Missed architectural hooks or incorrect database schema assumptions.
- **Rollback Plan**: Re-verify file structures from original version control history.
- **Complexity**: LOW

---

### PHASE 2 – Project Boundary and Structure
- **Objective**: Establish the development workspace configuration, standard directory routes, and baseline dependencies.
- **Inputs**: Workspace files.
- **Tasks**:
  1. Configure root `.gitignore` to prevent committing virtual environments and model weights.
  2. Align ports across local configuration files and container layouts.
  3. Install standard deep learning libraries (`tensorflow`, `scikit-learn`) in the local python setup.
- **Existing Files Involved**:
  - `MLService/requirements.txt`
  - `Backend/appsettings.json`
- **Expected New Files**:
  - `.gitignore` (Root)
- **Completion Criteria**: Backend compiles and starts correctly, and requirements install without errors.
- **Dependencies**: PHASE 1
- **Risks**: Python library version incompatibilities (particularly TensorFlow).
- **Rollback Plan**: Revert dependencies to original packages.
- **Complexity**: LOW

---

### PHASE 3 – Course Classification (ML-2)
- **Objective**: Implement text categorization using MLP and Linear SVM models.
- **Inputs**: Kaggle Udemy Courses dataset.
- **Tasks**:
  1. Explore and clean course titles and descriptions.
  2. Implement TF-IDF vectorizer (max 5,000 features).
  3. Train an MLP network and compare against a Platt-scaled Linear SVM.
  4. Perform comparative error analysis between MLP and SVM.
  5. Export weights (`.keras`, `.pkl`, `label_encoder.pkl`).
- **Existing Files Involved**:
  - `edumy-ml/notebooks/2-classify-mlp/course_classification_mlp (1).ipynb`
- **Expected New Files**:
  - `MLService/models/mlp_course_classifier.keras`
  - `MLService/models/svm_course_classifier.pkl`
  - `MLService/models/tfidf_vectorizer.pkl`
  - `MLService/models/label_encoder.pkl`
- **Completion Criteria**: Honest training and evaluation metrics are reported in the notebook, without pre-determining performance thresholds or accuracy scores.
- **Dependencies**: PHASE 2
- **Risks**: Class imbalance in Udemy categories causing low recall for minority subjects.
- **Rollback Plan**: Course creation ignores auto-classification, and the instructor selects the category manually.
- **Complexity**: MEDIUM

---

### PHASE 4 – Sentiment Analysis (ML-1)
- **Objective**: Train a recurrent network (LSTM) for comment sentiment rating.
- **Inputs**: Public education review dataset (e.g. Coursera/Udemy reviews).
- **Tasks**:
  1. Preprocess reviews (remove punctuation, handle emojis).
  2. Build vocabulary index and tokenize reviews with Keras Tokenizer.
  3. Train a bidirectional LSTM network with class-weight balances.
  4. Evaluate via confusion matrix, macro F1, and error review tables.
  5. Export model files.
- **Existing Files Involved**:
  - `edumy-ml/notebooks/1-sentiment-lstm/` (Directory)
- **Expected New Files**:
  - `edumy-ml/notebooks/1-sentiment-lstm/sentiment_lstm.ipynb` (New training notebook)
  - `MLService/models/sentiment_lstm.keras`
  - `MLService/models/sentiment_tokenizer.pkl`
- **Completion Criteria**: Honest training and evaluation reporting in the notebook, outputting Accuracy, Macro F1, Weighted F1, confusion matrix, and sample error analysis from the actual training runs without pre-defined performance thresholds.
- **Dependencies**: PHASE 3
- **Risks**: Inability to capture slang or negative reviews with negation ("not good").
- **Rollback Plan**: Save the review in the database with a "Pending" or "Unavailable" sentiment status.
- **Complexity**: HIGH

---

### PHASE 5 – Course Recommendation (ML-3)
- **Objective**: Implement personalized recommendations using collaborative filtering models.
- **Inputs**: OULAD (Open University Learning Analytics Dataset). *Strictly no synthetic user interaction data allowed during training.*
- **Tasks**:
  1. Prepare student-course interactions matrix using implicit feedback signals.
  2. Perform negative sampling (4:1 ratio per epoch), checking for leakage.
  3. Apply a chronological temporal split (leaving the last user interaction for test).
  4. Develop and independently compare three models:
     - **Popularity baseline**
     - **Generalized Matrix Factorization (GMF)**
     - **Neural Matrix Factorization (NeuMF)** (incorporating an MLP architecture as an internal branch, not as a standalone comparative model).
  5. Evaluate performance using metrics: **HR@5, HR@10, NDCG@5, NDCG@10, Precision@10, Recall@10, and Coverage**.
  6. Generate **hit/miss evidence tables** from test outputs.
  7. Implement the production path: Fetch actual student interactions from the EduMy SQL Server database, retrain local encoders/models on these interactions, and generate recommendations in the EduMy CourseId domain.
  8. Handle cold-start/empty interaction cases by serving popular courses calculated directly from actual database interaction counts in EduMy.
- **Existing Files Involved**:
  - `edumy-ml/notebooks/3-recommend-ncf/` (Directory)
- **Expected New Files**:
  - `edumy-ml/notebooks/3-recommend-ncf/recommend_ncf.ipynb` (New training notebook)
  - `MLService/models/neumf_recommender.keras`
  - `MLService/models/user_course_mappings.pkl`
- **Completion Criteria**: Notebook presents a comparison of the three models (Popularity, GMF, NeuMF) across all metrics. The final deployed model is selected based on offline evaluation metrics (HR@K, NDCG@K, Coverage), inference latency, and error analysis from the training run. OULAD is used strictly for offline validation; no arbitrary mapping table translation from OULAD items to EduMy course IDs is created.
- **Dependencies**: PHASE 4
- **Risks**: Domain mismatch and data sparsity in the EduMy database.
- **Rollback Plan**: Recommendations fall back to real popularity lists computed directly from the EduMy database.
- **Complexity**: VERY_HIGH

---

### PHASE 6 – FastAPI Model Serving
- **Objective**: Load trained models and expose inference endpoints from FastAPI.
- **Inputs**: Deployed model binaries (`.keras`, `.pkl`, etc.).
- **Tasks**:
  1. Structure loading hooks inside FastAPI startup event context.
  2. Update endpoints `/sentiment/analyze`, `/classify/course`, `/recommend/courses` to run real inference.
  3. Configure text preprocessing pipelines (tokenization, TF-IDF transforms) inside endpoints.
  4. Ensure `/health` endpoint updates status flags.
- **Existing Files Involved**:
  - `MLService/main.py`
- **Expected New Files**: None.
- **Completion Criteria**: All endpoints accept requests and return predictions with real model scores (no keyword heuristics or hardcoded return arrays).
- **Dependencies**: PHASE 5
- **Risks**: High latency during model load or inference timeouts.
- **Rollback Plan**: Return "Unavailable" status flags for failing calls.
- **Complexity**: MEDIUM

---

### PHASE 7 – ASP.NET Core Integration
- **Objective**: Establish production API calls from the backend gateway to the FastAPI server.
- **Inputs**: Running FastAPI endpoints.
- **Tasks**:
  1. Verify Polly retry limits and circuit breaker timings.
  2. Implement proper error handling when the FastAPI service goes offline.
  3. Validate category mappings inside the course creation workflow.
- **Existing Files Involved**:
  - `Backend/Services/MachineLearningService.cs`
  - `Backend/Controllers/CoursesController.cs`
  - `Backend/Controllers/ReviewsController.cs`
- **Expected New Files**: None.
- **Completion Criteria**: Backend API routes call the ML service, mapping outputs correctly.
- **Dependencies**: PHASE 6
- **Risks**: Network failures causing page load lags.
- **Rollback Plan**: Disable integration by routing calls to local fallbacks (instructors choose categories manually, reviews save with "Pending" status, and recommendations use database popularity metrics).
- **Complexity**: MEDIUM

---

### PHASE 8 – React Integration
- **Objective**: Update the visual user interface to display model predictions.
- **Inputs**: Backend JSON outputs.
- **Tasks**:
  1. Render sentiment flags with confidence badges on course review items.
  2. Display course quality scores and warning alerts on the instructor dashboard.
  3. Render personalized "AI recommended" sections on the landing page.
- **Existing Files Involved**:
  - `Frontend/src/pages/HomePage.jsx`
  - `Frontend/src/pages/CourseDetail.jsx`
  - `Frontend/src/pages/InstructorDashboard.jsx`
- **Expected New Files**: None.
- **Completion Criteria**: UI displays custom tags, categories, ratings, and recommendation panels dynamically.
- **Dependencies**: PHASE 7
- **Risks**: Rendering delays or layout bugs on varying screen widths.
- **Rollback Plan**: Hide recommendation panels and fall back to the popular catalog grid.
- **Complexity**: LOW

---

### PHASE 9 – Database Persistence
- **Objective**: Save ML outputs to tables for audit and tracking.
- **Inputs**: Predicted values.
- **Tasks**:
  1. Record course categorization predictions, toxicity status, and quality ratings in the database.
  2. Log model version names for each inference call.
  3. Create DB scripts to backfill sentiment scores for pre-existing seed reviews.
- **Existing Files Involved**:
  - `Backend/Controllers/ReviewsController.cs`
  - `Backend/Controllers/CoursesController.cs`
  - `Backend/Data/DataSeeder.cs`
- **Expected New Files**:
  - `Backend/Data/BackfillScript.sql` (Optional data update query)
- **Completion Criteria**: Databases logs match inference output counts.
- **Dependencies**: PHASE 8
- **Risks**: Concurrency locks during database inserts under high request volume.
- **Rollback Plan**: Disable DB logging and save results transiently.
- **Complexity**: LOW

---

### PHASE 10 – Testing, Security, Documentation and Presentation
- **Objective**: Audit application security, conduct integration testing, and prepare academic evaluation packages.
- **Inputs**: Completed application setup.
- **Tasks**:
  1. Remove hardcoded secrets from config files, moving them to User Secrets or env variables.
  2. Secure `/api/mltest/*` endpoints behind proper authorization.
  3. Write unit tests for `MachineLearningService` and verify all critical path integrations.
  4. Finalize application installation logs.
- **Existing Files Involved**:
  - `Backend/appsettings.json`
  - `README.md`
- **Expected New Files**:
  - `Backend.Tests/MachineLearningServiceTests.cs` (New testing class)
- **Completion Criteria**: Successful testing of critical paths (such as authentication flows, checkout pipeline, and ML client service requests) and zero exposed secrets in configuration files.
- **Dependencies**: PHASE 9
- **Risks**: Overlooked authentication loopholes.
- **Rollback Plan**: Restore strict default access controls.
- **Complexity**: MEDIUM
