# Error Analysis — Course Classification

> [!WARNING]
> **Status**: **BLOCKED**
>
> **Reason**: The raw dataset file (`ml-training/datasets/raw/udemy_courses.csv`) is missing from the local workspace. No prediction logs exist to analyze misclassifications.

---

## Planned Diagnostic Categories

Once model validation splits are complete, we will sample 30 misclassifications to check:
1. **Ambiguous Phrasing**: Titles containing keywords across multiple categories (e.g. *"Python for Graphic Designers"*).
2. **Missing Metadata**: Extremely short course titles lacking descriptive features.
3. **Inconsistent Labels**: Identical titles labeled differently in the raw dataset.
