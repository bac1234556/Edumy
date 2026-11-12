# ML Gap Analysis — EduMy

This document details the gaps between the current project state (stubs, README plans, fallback mock rules) and the expected academic/technical targets for the three Machine Learning tasks.

---

## 1. Sentiment Analysis Gaps (Bài toán 1)

### GAP-SA-01: Dataset is Synthetic or Lacks Reputable Source
- **Gap ID**: GAP-SA-01
- **Severity**: HIGH
- **Current State**: The repository references "Udemy reviews + Vietnamese" but contains no raw dataset files, download links, or details on acquisition.
- **Expected State**: Use a reputable, publicly accessible educational/online learning review dataset with clearly documented license, source, and size (~50,000 samples). **Status is NEEDS_VERIFICATION** until a specific URL, author name, license type, schema definition, and exact record count are obtained and validated.
- **Evidence**: [`notebooks/1-sentiment-lstm/README.md`](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/ML_MODEL_STATUS.md#ml-1-sentiment-analysis-lstm) contains only text descriptions, no dataset downloading scripts or references.
- **Risk**: Academic evaluation will fail due to lack of reproducible data lineage. Model bias cannot be studied.
- **Planned Fix**: Adopt a public dataset such as the *Udemy Course Reviews* dataset from Kaggle, document its schema, and add download/loading steps to the training notebooks.
- **Planned Phase**: PHASE 4

### GAP-SA-02: Missing Evaluation Metrics (Macro F1, Confusion Matrix, Error Analysis)
- **Gap ID**: GAP-SA-02
- **Severity**: HIGH
- **Current State**: Notebooks for training, evaluation, and exporting (`03_model_training.ipynb`, `04_evaluation.ipynb`, `05_export_model.ipynb`) do not exist.
- **Expected State**: Evaluation notebook must calculate Macro F1 (due to class imbalance: 60/25/15), plot a confusion matrix, and output a manual error analysis of misclassified samples.
- **Evidence**: Ripgrep search confirms `notebooks/1-sentiment-lstm/` contains only `README.md` and no `.ipynb` files.
- **Risk**: Performance claims are unverifiable. Class imbalance might hide poor performance on minority classes (Negative/Neutral reviews).
- **Planned Fix**: Create the missing training and evaluation notebooks. Log Macro F1 and plot the confusion matrix using `matplotlib`/`seaborn` in python.
- **Planned Phase**: PHASE 4

### GAP-SA-03: Model is Not Loaded in FastAPI
- **Gap ID**: GAP-SA-03
- **Severity**: CRITICAL
- **Current State**: The local FastAPI service executes basic keyword-counting checks on a list of 11 words. No LSTM model is loaded.
- **Expected State**: FastAPI server loads a trained `.keras` LSTM model, preprocesses string input using an exported Keras tokenizer, and returns predictions.
- **Evidence**: [`MLService/main.py:46-59`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py) class `BaselineSentimentAnalyzer` uses rule-based string comparison.
- **Risk**: The live application displays fake sentiment labels (`Positive`/`Neutral`/`Negative`) on reviews.
- **Planned Fix**: Deploy the exported `.keras` model and tokenizer to the FastAPI directory, load them on startup, and implement tokenization/inference inside the `/sentiment/analyze` endpoint.
- **Planned Phase**: PHASE 6

---

## 2. Course Classification Gaps (Bài toán 2)

### GAP-CC-01: Missing Baseline Model (Linear SVM) & Comparison
- **Gap ID**: GAP-CC-01
- **Severity**: HIGH
- **Current State**: The repository only outlines an MLP model in its planning. No Linear SVM baseline exists.
- **Expected State**: Implement both MLP and Linear SVM classifiers, trained on the same train/validation/test split, and provide a comparison table.
- **Evidence**: [`notebooks/2-classify-mlp/README.md`](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/ML_MODEL_STATUS.md#ml-2-course-classification-mlp) outlines MLP architecture but does not specify or implement SVM comparison.
- **Risk**: Fails project specification to contrast deep learning models against traditional machine learning baselines.
- **Planned Fix**: Write training steps for both Keras MLP and scikit-learn `LinearSVC` in the classification notebook, verifying they use identical TF-IDF vectors.
- **Planned Phase**: PHASE 3

### GAP-CC-02: Missing Comparative Error Analysis (MLP vs. SVM)
- **Gap ID**: GAP-CC-02
- **Severity**: MEDIUM
- **Current State**: No evaluation notebooks compare the specific errors made by the two classifiers.
- **Expected State**: Document examples where MLP predicts incorrectly while SVM is correct, and vice versa, to understand structural model behaviors.
- **Evidence**: No classification notebook or evaluation files are present comparing sample-level predictions.
- **Risk**: Hard to justify the added complexity of the MLP model over SVM without diagnostic error analysis.
- **Planned Fix**: Add an evaluation section in the notebook that isolates and prints text samples where the predictions of MLP and SVM diverge.
- **Planned Phase**: PHASE 3

### GAP-CC-03: SVM Probability Calibration
- **Gap ID**: GAP-CC-03
- **Severity**: MEDIUM
- **Current State**: SVM does not output confidence scores directly, and no calibration is planned.
- **Expected State**: Apply Platt Scaling (e.g. `CalibratedClassifierCV` in scikit-learn) so that the SVM output can be compared as a probability score against the MLP confidence.
- **Evidence**: The specification requests confidence validation, but standard SVM decision functions are uncalibrated.
- **Risk**: Admin dashboard cannot reliably moderate SVM classifications because the confidence score is missing or uncalibrated.
- **Planned Fix**: Wrap the `LinearSVC` model in `CalibratedClassifierCV` during training.
- **Planned Phase**: PHASE 3

### GAP-CC-04: Category Mismatch & Lack of Web Integration
- **Gap ID**: GAP-CC-04
- **Severity**: HIGH
- **Current State**: FastAPI uses dictionary logic matching 3 categories. The backend seeds 8 categories. The notebook trains on 4 categories (Development, Business, Design, Music).
- **Expected State**: Alignment of categories between ML training and Web Database. The backend should handle prediction confidence thresholds correctly.
- **Evidence**: [DataSeeder.cs](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Data/DataSeeder.cs#L153-L164) seeds 8 category names. [`MLService/main.py`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py) uses 3 categories.
- **Risk**: Categorization fails because ML predictions do not exist in the database, resulting in null foreign keys.
- **Planned Fix**: Align the category list between the database seeder and the training datasets (e.g., map predicted categories to the closest database equivalents).
- **Planned Phase**: PHASE 7

---

## 3. Recommendation Gaps (Bài toán 3)

### GAP-REC-01: Interaction Data is Synthetic & Lacks Real Dataset
- **Gap ID**: GAP-REC-01
- **Severity**: CRITICAL
- **Current State**: No interaction dataset is stored or downloaded. The README mentions a mock matrix of 5,000 users.
- **Expected State**: Use a real, documented educational dataset (e.g., OULAD - Open University Learning Analytics Dataset) to evaluate the recommendation system.
- **Evidence**: [`notebooks/3-recommend-ncf/README.md`](file:///e:/Edumy%20%281%29/Edumy/Edumy/docs/ML_MODEL_STATUS.md#ml-3-course-recommendation-ncf) contains only descriptions, no data loading scripts.
- **Risk**: Recommendations are completely simulated; evaluation metrics (HR, NDCG) are meaningless.
- **Planned Fix**: Download the OULAD dataset, preprocess student-module interactions as implicit feedback, and document the schema.
- **Planned Phase**: PHASE 5

### GAP-REC-02: Missing Popularity Baseline & Model Implementations (GMF, NeuMF)
- **Gap ID**: GAP-REC-02
- **Severity**: CRITICAL
- **Current State**: Recommendation is hardcoded to return `[1, 2, 3]`. GMF and NeuMF do not exist.
- **Expected State**: Train a GMF branch (linear embeddings interaction) and NeuMF (GMF + MLP combined) model, and compare them against a simple popularity baseline (most-enrolled courses).
- **Evidence**: [`MLService/main.py:147-151`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py) returns fixed values. `notebooks/3-recommend-ncf/` has no `.ipynb` files.
- **Risk**: Core academic component of the recommendation system is missing.
- **Planned Fix**: Create recommendation notebooks implementing Popularity baseline, GMF model, and NeuMF model in TensorFlow/Keras.
- **Planned Phase**: PHASE 5

### GAP-REC-03: Temporal Split & Negative Sampling Leakage
- **Gap ID**: GAP-REC-03
- **Severity**: HIGH
- **Current State**: No training script or negative sampling logic is implemented.
- **Expected State**: Partition training/test sets using a temporal split (e.g., leave-one-last interaction per user for test) to avoid chronological data leakage. Negative samples must be drawn strictly from courses the user has never interacted with.
- **Evidence**: Absence of data splitting code in the repository.
- **Risk**: Overoptimistic validation metrics due to data leakage.
- **Planned Fix**: Implement a custom data loader that splits the interaction history chronologically and performs negative sampling (ratio 4:1) for each training epoch.
- **Planned Phase**: PHASE 5

### GAP-REC-04: Missing Evaluation Metrics & Hit/Miss Evidence
- **Gap ID**: GAP-REC-04
- **Severity**: HIGH
- **Current State**: HR@10 and NDCG@10 metrics are described but not implemented.
- **Expected State**: Compute HR@5, HR@10, NDCG@5, NDCG@10, Precision@10, Recall@10, and coverage across all users. Provide tabular evidence of hit vs. miss samples.
- **Evidence**: No evaluation scripts or results exist.
- **Risk**: Inability to quantify recommendations quality.
- **Planned Fix**: Write evaluation loops in python to compute these metrics on the test set and display sample predictions side-by-side with ground truth.
- **Planned Phase**: PHASE 5

### GAP-REC-05: Missing Cold-Start Handling
- **Gap ID**: GAP-REC-05
- **Severity**: MEDIUM
- **Current State**: Endpoint returns static list `[1, 2, 3]` regardless of user type or login state.
- **Expected State**: Detect anonymous or new users (who have zero database interactions) and serve them top courses via the popularity-based fallback.
- **Evidence**: [`MLService/main.py:147-151`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/main.py) does not inspect user history or database counts.
- **Risk**: Recommendations crash or return empty lists for new students.
- **Planned Fix**: Implement user history checking in the FastAPI recommend endpoint, returning the popular course list as a fallback if the user is new.
- **Planned Phase**: PHASE 5

### GAP-REC-06: OULAD to EduMy Course ID Mapping (Domain Mismatch)
- **Gap ID**: GAP-REC-06
- **Severity**: CRITICAL
- **Current State**: OULAD contains module IDs like `AAA`, `BBB`, while the EduMy database contains integer `CourseId` keys.
- **Expected State**: OULAD is used strictly for offline training and evaluation in notebooks. The model trained on OULAD MUST NOT directly output CourseId of EduMy. Production recommendation must use actual student interactions recorded in the EduMy SQL Server database to retrain the model and return EduMy CourseIds. When the EduMy interaction history is insufficient (e.g., initial launch), the production system falls back to a popularity-based recommendation list computed directly from EduMy database metrics. No arbitrary mapping table translation from OULAD items to EduMy course IDs is allowed in production.
- **Evidence**: [`appsettings.json`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/appsettings.json) and [`DataSeeder.cs`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Data/DataSeeder.cs) contain no mapping structures.
- **Risk**: Directly outputting OULAD IDs in production results in query failures or recommending non-existent courses to the user.
- **Planned Fix**: Restrict OULAD to offline training validation. Program the production API pipeline to fetch user interactions directly from the EduMy `Enrollments`, `Reviews`, and `UserActivities` tables, train local encoders using those values, and issue recommendations within the EduMy ID domain. If database logs are empty, use popularity fallback.
- **Planned Phase**: PHASE 5

