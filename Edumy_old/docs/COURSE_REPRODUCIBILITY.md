# Reproducibility Specification — Course Classification

This document details the environment configuration, random seeds, and notebook execution orders required to reproduce the course classification training pipelines.

---

## 1. Random Seeds
To ensure identical data splitting, TF-IDF mapping, and weight initializations:
- **Scikit-learn random_state**: `42`
- **TensorFlow random seed**: `42`
- **NumPy random seed**: `42`

---

## 2. Software Versions
- **Python Version**: 3.12.3
- **Scikit-learn Version**: 1.5.0
- **TensorFlow Version**: 2.16.1
- **Pandas Version**: 2.2.2
- **NumPy Version**: 1.26.4
- **Git Commit Reference**: `NEEDS_VERIFICATION` (not tracked in local repository)

---

## 3. Execution Sequence
To replicate the experiment, notebooks must be run in the following order after placing `udemy_courses.csv` in `ml-training/datasets/raw/`:
1. `01_dataset_validation.ipynb`
2. `02_eda.ipynb`
3. `03_preprocessing.ipynb`
4. `04_train_svm.ipynb`
5. `05_train_mlp.ipynb`
6. `06_compare_models.ipynb`
