# Recommendation Training Blocker Fix Report — EduMy

This document details the diagnostic status and setup instructions for the Recommendation System (Bài toán 3) model training.

---

## 1. Current Blocker Status
- **Status**: **BLOCKED**
- **Blocker**: The raw OULAD files (`studentRegistration.csv`, `studentVle.csv`) are missing from the workspace.
- **Action Taken**: The notebook pipeline templates (01 through 09) have been successfully generated in the workspace. Training cannot proceed until the dataset is resolved.

---

## 2. OULAD Dataset Download & Setup Guide

To run recommendation model training:

### Step 1: Download OULAD
1. Go to the [Open University Learning Analytics Dataset (OULAD) Portal](https://analyse.kmi.open.ac.uk/resources/open_university_learning_analytics_dataset).
2. Download the full dataset package (zip).

### Step 2: Place the Files
1. Create the raw data directory in your workspace:
   `ml-training/datasets/raw/`
2. Extract the downloaded zip and place the following CSV files in that folder:
   - `studentRegistration.csv`
   - `studentVle.csv`
   - (Optional but recommended: `courses.csv`, `studentInfo.csv`, `studentAssessment.csv`, `assessments.csv`, `vle.csv`)

### Step 3: Run the Notebooks in Sequence
Once the CSV files are placed, open your Jupyter environment and run the notebooks in the folder `ml-training/notebooks/3-course-recommendation/` in the following order:
1. `01_dataset_validation.ipynb` (Loads and validates OULAD files)
2. `02_eda.ipynb` (Computes sparsity and registrations distributions)
3. `03_preprocessing.ipynb` (Transforms student IDs and module categories to integer indices)
4. `04_negative_sampling.ipynb` (Generates 4:1 negative samples per user positive registration)
5. `05_train_popularity.ipynb` (Computes frequency-based non-personalized baseline ranks)
6. `06_train_gmf.ipynb` (Trains the GMF network using Adam and binary crossentropy)
7. `07_train_neumf.ipynb` (Trains the unified GMF + MLP NeuMF neural network)
8. `08_compare_models.ipynb` (Computes HR@10 and NDCG@10 scores on the test set)
