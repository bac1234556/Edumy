# Recommendation Training Execution Report — EduMy

> [!WARNING]
> **Status**: **BLOCKED**
>
> **Reason**: The python package `tensorflow` is not installed in the system environment. Model training cannot be executed in this setup session.

---

## 1. Environment Diagnostics
- **Python Executable**: System Default Python.
- **Dependency Scan**:
  - `pandas`: Available
  - `scikit-learn`: Available
  - `tensorflow`: **Missing** (ModuleNotFoundError: No module named 'tensorflow')

---

## 2. Environment Setup & Execution Guide

To resolve this blocker and compile the NeuMF model weights, please run the following setup commands on your local console:

### Step 1: Create a Python Virtual Environment
Navigate to the root directory of the workspace and run:
```bash
python -m venv .venv
.venv\Scripts\activate
```

### Step 2: Install Required Dependencies
Install the required machine learning packages:
```bash
pip install tensorflow==2.16.1 pandas scikit-learn joblib jupyter notebook
```

### Step 3: Run the Jupyter Notebooks
Start Jupyter notebook:
```bash
jupyter notebook
```
Open each notebook in `ml-training/notebooks/3-course-recommendation/` in sequence and run all cells:
1. `01_dataset_validation.ipynb`
2. `02_eda.ipynb`
3. `03_preprocessing.ipynb`
4. `04_negative_sampling.ipynb`
5. `05_train_popularity.ipynb`
6. `06_train_gmf.ipynb`
7. `07_train_neumf.ipynb` (Compiles NeuMF embeddings)
8. `08_compare_models.ipynb` (Logs HR@10 and NDCG@10 comparison matrices)
