# Sentiment Analysis Design Specification

This document outlines the model architecture, training split parameters, and training techniques designed for the Sentiment Analysis task.

---

## 1. BiLSTM Model Architecture

We use a Bidirectional LSTM (BiLSTM) network to capture contextual dependencies from both past and future words in a review.

```
Input Tokens (Max Length: 150)
      │
      ▼
[ Embedding Layer ] ── Dimension: 128 (translates word index to dense vector)
      │
      ▼
[ SpatialDropout1D (0.2) ] ── Prevents overfitting in text structures
      │
      ▼
[ Bidirectional LSTM (128 units) ] ── Recursively processes sequence forward and backward
      │
      ▼
[ GlobalMaxPooling1D / Dense(64, relu) ] ── Condenses sequence features
      │
      ▼
[ Dropout (0.5) ] ── Regularization layer
      │
      ▼
[ Output Layer (Dense 3, softmax) ] ── Predicts probabilities for [NEGATIVE, NEUTRAL, POSITIVE]
```

---

## 2. Train / Validation / Test Split Strategy

To ensure evaluation results are representative of the true data distribution:
- **Split Ratio**: 70% Train, 15% Validation, 15% Test.
- **Split Method**: **Stratified Split** (e.g., using `StratifiedShuffleSplit` from scikit-learn).
- **Reason**: The Coursera review dataset is highly imbalanced (heavily skewed towards positive ratings). A random split might lead to insufficient positive/neutral representation in the test set or validation set. A stratified split preserves the exact class ratio across all subsets.

---

## 3. Class Imbalance Handling

We do **not** use SMOTE or duplicate reviews to balance classes. Over-sampling natural language text via duplication or synthetic noise (SMOTE) leads to overfitting on minority expressions.
Instead, we apply:
- **Class Weights**: Calculate inverse class frequencies during training and pass them to `model.fit(class_weight=class_weights)`.
- **Loss Penalization**: The loss function penalizes errors on minority classes (Negative and Neutral reviews) more heavily, encouraging the model to learn their distinct characteristics without modifying the underlying sequence data.
