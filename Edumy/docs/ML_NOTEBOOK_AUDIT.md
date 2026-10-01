# Machine Learning Notebook Audit Report

This report details the execution status, kernel metadata, and validity of all notebooks in the repository.

---

## 1. Sentiment Analysis Notebooks (`ml-training/notebooks/1-sentiment-lstm/`)
All notebooks in this directory are **PLACEHOLDERS** containing only markdown cells detailing the theoretical steps. No code cells exist, and no models were ever trained or exported.

- `01_dataset_validation.ipynb` — **PLACEHOLDER**
- `02_eda.ipynb` — **PLACEHOLDER**
- `03_preprocessing.ipynb` — **PLACEHOLDER**
- `04_training.ipynb` — **PLACEHOLDER**
- `05_evaluation.ipynb` — **PLACEHOLDER**
- `06_export.ipynb` — **PLACEHOLDER**

---

## 2. Course Classification Notebooks (`ml-training/notebooks/2-course-classification/`)
Notebooks contain basic code skeletons but do not fit or save any models.

- `01_dataset_validation.ipynb` — **PLACEHOLDER**
- `02_eda.ipynb` — **PLACEHOLDER**
- `03_preprocessing.ipynb` — **PLACEHOLDER**
- `04_train_svm.ipynb` — **PLACEHOLDER**
- `05_train_mlp.ipynb` — **PLACEHOLDER** (Contains code structure but no `.fit()`)
- `06_compare_models.ipynb` — **PLACEHOLDER**
- `07_export.ipynb` — **PLACEHOLDER**

---

## 3. Course Recommendation Notebooks (`ml-training/notebooks/3-course-recommendation/`)
All notebooks are **IMPLEMENTED** and executed successfully. Output cells contain real OULAD validation logs.

- `01_dataset_validation.ipynb` — **PASS** (Executed, verified OULAD files)
- `02_eda.ipynb` — **PASS** (Executed, printed sparsity/density)
- `03_preprocessing.ipynb` — **PASS** (Executed, saved `user_encoder.joblib` and `item_encoder.joblib`)
- `04_negative_sampling.ipynb` — **PASS** (Executed, generated 4:1 ratios)
- `05_train_popularity.ipynb` — **PASS** (Executed, printed baseline metrics)
- `06_train_gmf.ipynb` — **PASS** (Executed, GMF model fit and evaluated)
- `07_train_neumf.ipynb` — **PASS** (Executed, NeuMF model fit and evaluated)
- `08_compare_models.ipynb` — **PASS** (Executed, selected Popularity candidate based on NDCG tie-breaker, wrote checksums)
- `09_export.ipynb` — **DEPRECATED** (All export code was migrated directly to notebook 08 for integrity checks)
