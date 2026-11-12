# Notebook Structure Plan — Course Recommendation

This document details the planned Jupyter Notebook files that will execute the recommendation training pipeline.

---

## 1. Planned Notebook Files

Notebook files will be located in the folder `ml-training/notebooks/3-course-recommendation/`:

1. **`01_dataset_validation.ipynb`**: Validates OULAD files (`studentRegistration.csv` and `studentVle.csv`).
2. **`02_eda.ipynb`**: Generates sparsity matrices and click histograms.
3. **`03_preprocessing.ipynb`**: Implements user/item encoding pipelines.
4. **`04_negative_sampling.ipynb`**: Performs 4:1 negative sampling exclusions.
5. **`05_train_popularity.ipynb`**: Implements the non-personalized baseline.
6. **`06_train_gmf.ipynb`**: Defines and trains GMF embedding networks.
7. **`07_train_neumf.ipynb`**: Trains the NeuMF model.
8. **`08_compare_models.ipynb`**: Computes HR@10 and NDCG@10 comparison matrices.
9. **`09_export.ipynb`**: Exports NeuMF parameters and encoders.

*Note*: These notebooks represent the planned steps. No code cells will be executed or evaluated in this preparation stage.
