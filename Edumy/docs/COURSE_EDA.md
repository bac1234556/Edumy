# Exploratory Data Analysis Plan — Course Classification

This document details the exploratory data analysis (EDA) and data validation plan that will be executed in `02_eda.ipynb`.

---

## 1. Validation Checklist

We will perform the following validation checks to ensure clean text inputs:

- **Missing Titles**: Check for rows with null or empty `course_title` strings.
- **Duplicate Courses**: Identify identical titles to remove duplicates.
- **Class Balance**: Measure the representation of each of the 4 categories (`Web Development`, `Business Finance`, `Graphic Design`, `Musical Instruments`).
- **Word Counts**: Compute descriptive statistics (mean, median, standard deviation) for word lengths in course titles.

---

## 2. Statistical Visualizations

The EDA notebook will generate:
- **Bar Chart**: Frequency of each subject category to monitor imbalance.
- **Histogram**: Length distribution of course titles.
- **Word Frequency Table**: Most common and least common tokens per category after basic normalization.
