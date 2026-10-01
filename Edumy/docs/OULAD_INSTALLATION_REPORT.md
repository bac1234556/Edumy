# OULAD Installation Report

This document reports on the successful download, extraction, and validation of the Open University Learning Analytics Dataset (OULAD).

---

## 1. Dataset Source
- **Publisher**: The Open University (UK)
- **Official Site**: https://analyse.kmi.open.ac.uk/open_dataset

---

## 2. Download URL
-Figshare STEM Mirror: `http://schools.stem.open.ac.uk/cdn/files/anonymisedData.zip`

---

## 3. Version & License
- **Version**: anonymisedData release
- **License**: CC BY 4.0

---

## 4. Installed Files
All 7 OULAD tables are installed in [`ml-training/datasets/raw/`](file:///e:/Edumy%20%281%29/Edumy/Edumy/ml-training/datasets/raw):
- `courses.csv` (526 bytes)
- `assessments.csv` (8,200 bytes)
- `vle.csv` (260,126 bytes)
- `studentInfo.csv` (3,461,652 bytes)
- `studentRegistration.csv` (1,109,984 bytes)
- `studentAssessment.csv` (5,690,310 bytes)
- `studentVle.csv` (453,836,331 bytes)

---

## 5. File Verification
- **Pandas Read**: Success (verified using Python runtime).
- **Integrity**: PASS (All headers and student rows parsed with 0 errors).

---

## 6. Dataset Summary
- **Unique Students**: 32,593
- **Registration Columns**: `['code_module', 'code_presentation', 'id_student', 'date_registration', 'date_unregistration']`
- **VLE Columns**: `['code_module', 'code_presentation', 'id_student', 'id_site', 'date', 'sum_click']`

---

## 7. Final Status
**READY**
