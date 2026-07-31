# Preprocessing & Negative Sampling Strategy — Recommendation

This document specifies the pipeline for transforming raw interaction logs into training tensors.

---

## 1. User & Item Mapping
- **User ID**: We map `id_student` to a zero-indexed integer encoder range.
- **Item Definition**: We define an **Item** as `code_module` (representing 7 distinct courses) rather than `code_module + code_presentation` because presentations represent the same course content taught in different semesters. Mapping items to `code_module` aligns closer with the course recommendation scenario.

---

## 2. Interaction Definition
We use **Implicit Feedback**:
- **Positive Interaction (1)**: A student registering for a module (recorded in `studentRegistration.csv`).
- **Negative Interaction (0)**: Non-registered courses.

---

## 3. Negative Sampling Strategy
Since NCF (Neural Collaborative Filtering) requires binary classification training inputs, we generate negative samples:
- **Ratio**: **4:1** (4 negative samples randomly selected per positive interaction per epoch).
- **Leakage Prevention**: We strictly exclude any registered course modules from the negative pool for that user.
- **Random Seed**: `42` is configured to ensure deterministic negative generation.

---

## 4. Data Splitting (Leave-One-Out)
- **Train Set**: All positive registrations minus the most recent registration for each user.
- **Validation Set**: The second-to-last registration.
- **Test Set**: The last registration (leave-one-out temporal split).
