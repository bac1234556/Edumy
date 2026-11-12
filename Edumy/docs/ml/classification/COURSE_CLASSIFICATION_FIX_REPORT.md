# Course Classification Blocker Fix Report — EduMy

This document details the diagnostic steps and resolution instructions for the Course Classification (Bài toán 2) blockers.

---

## 1. Current Blocker Status
- **Status**: **BLOCKED**
- **Blocker**: The raw dataset file `udemy_courses.csv` is missing from `ml-training/datasets/raw/`.
- **Action Taken**: Notebook stubs, shared Python preprocessors, FastAPI loaders, and ASP.NET Core controllers have been successfully created and configured. Actual model training cannot occur without the source dataset.

---

## 2. Dataset Download & Setup Guide

To unblock model training and activate AI category recommendations:

### Step 1: Download the Dataset
1. Go to Kaggle: [Udemy Courses Dataset by Andrewmvd](https://www.kaggle.com/datasets/andrewmvd/udemy-courses).
2. Download the `udemy_courses.csv` file.

### Step 2: Place the File
1. Move the downloaded CSV file to the following path in the workspace:
   `ml-training/datasets/raw/udemy_courses.csv`

### Step 3: Run the Training Notebooks
Execute the notebooks in the following order under `ml-training/notebooks/2-course-classification/`:
1. `01_dataset_validation.ipynb` (Verifies structure and categories)
2. `02_eda.ipynb` (Plots counts and lengths)
3. `03_preprocessing.ipynb` (Cleans titles and creates stratified train/val/test splits)
4. `04_train_svm.ipynb` (Trains and calibrates the Linear SVM model)
5. `05_train_mlp.ipynb` (Trains the MLP neural network)
6. `06_compare_models.ipynb` (Benchmarks accuracy and macro F1 scores)
7. `07_export.ipynb` (Serializes model artifacts to `saved_models/course-classification/v1/`)

---

## 3. Preprocessing Preservation
The text cleaning pipeline is implemented in the shared Python module [`MLService/preprocessing/course_text.py`](file:///e:/Edumy%20%281%29/Edumy/Edumy/MLService/preprocessing/course_text.py) to ensure consistency between training notebooks and production FastAPI inference:
- Strips HTML tags and URLs.
- Normalizes whitespaces.
- **Preserves tech tokens**: `C#`, `C++`, `.NET`, `ASP.NET`, `Node.js`, `UI/UX`.
- Avoids over-cleansing special characters.
