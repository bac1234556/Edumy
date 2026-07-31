# Exploratory Data Analysis Report — Course Recommendation

This document outlines the real statistics and findings from analyzing the OULAD student registration dataset.

---

## 1. Interaction Statistics

For the interaction dataset (`studentRegistration.csv`):
- **Number of Users ($U$)**: 28,755 unique students.
- **Number of Items ($I$)**: 7 unique courses (`code_module`).
- **Interaction Count ($N$)**: 32,548 registration records (after dropping records with missing registration date).
- **Sparsity**: 83.83%
- **Density**: 16.17%

---

## 2. Distribution of Registrations per Course Module (Popularity Skew)

Based on the training set interactions:
- **CCC**: 137 registrations
- **DDD**: 47 registrations
- **FFF**: 40 registrations
- **EEE**: 31 registrations
- **BBB**: 13 registrations
- **AAA**: 11 registrations (rest modules are lower)

This shows a significant popularity skew, with course CCC being the most heavily registered module in the training partition.
