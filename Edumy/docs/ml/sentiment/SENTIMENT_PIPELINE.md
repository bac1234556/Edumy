# Sentiment Analysis Pipeline Mapping

This document maps the end-to-end execution flow of the Sentiment Analysis pipeline, detailing files, interfaces, and expected outcomes at each step.

---

## Pipeline Stages

```
Dataset ──> EDA ──> Preprocessing ──> BiLSTM Training ──> Evaluation ──> Export ──> FastAPI Serving ──> ASP.NET Core ──> React UI
```

### Stage 1: Dataset Verification
- **Process**: Load Coursera Reviews metadata, validating usability parameters.
- **Files**: `ml-training/datasets/raw/`

### Stage 2: Exploratory Data Analysis (EDA)
- **Process**: Measure nulls, duplicate rates, rating skewness, and review length frequencies.
- **Files**: `ml-training/notebooks/1-sentiment-lstm/02_eda.ipynb`

### Stage 3: Preprocessing & Data Cleaning
- **Process**: Lowercase, Unicode normalization, HTML/URL stripping, retaining emojis and negation words, tokenizing, and padding.
- **Files**: `ml-training/notebooks/1-sentiment-lstm/03_preprocessing.ipynb`

### Stage 4: BiLSTM Training
- **Process**: Configure Embedding + BiLSTM model, compute class weights, and run optimizer epochs.
- **Files**: `ml-training/notebooks/1-sentiment-lstm/04_training.ipynb`

### Stage 5: Evaluation
- **Process**: Calculate Accuracy, Macro F1, Weighted F1 on the test partition. Plot the confusion matrix and perform error analysis.
- **Files**: `ml-training/notebooks/1-sentiment-lstm/05_evaluation.ipynb`

### Stage 6: Model Export
- **Process**: Serialize Keras model structure/weights and Pickle tokenizer configuration.
- **Files**: `ml-training/notebooks/1-sentiment-lstm/06_export.ipynb`

### Stage 7: FastAPI Model Serving
- **Process**: Load model/tokenizer in FastAPI startup hooks. Expose post-processing prediction routes.
- **Files**: `MLService/main.py` (FastAPI server)

### Stage 8: ASP.NET Core API Integration
- **Process**: Query the FastAPI endpoint via Polly transient failure policy wrappers.
- **Files**: `Backend/Services/MachineLearningService.cs`, `Backend/Controllers/ReviewsController.cs`

### Stage 9: React UI Visualization
- **Process**: Render sentiment badges and charts on the user frontend.
- **Files**: `Frontend/src/pages/CourseDetail.jsx`, `Frontend/src/pages/InstructorDashboard.jsx`
