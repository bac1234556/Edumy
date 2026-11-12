# Model Card — Sentiment BiLSTM (v1.0)

This model card provides technical metadata, metrics, and application guidelines for the Bidirectional LSTM sentiment model integrated into the EduMy eLearning platform.

---

## 1. Model Details
- **Model Name**: EduMy Sentiment BiLSTM Recommender
- **Version**: 1.0.0
- **Type**: Recurrent Neural Network (BiLSTM)
- **Developer**: EduMy ML Engineering Team
- **Training Date**: 2026-07-30
- **Training Environment**: Local Python 3.12, TensorFlow 2.16.1, CUDA GPU enabled.

---

## 2. Intended Use
- **Suitable Use Case**: Classifying short student review texts (10–150 words) regarding online course quality into three categories: POSITIVE, NEUTRAL, and NEGATIVE.
- **Unsuitable Use Case**: Analyzing general e-commerce feedback, toxic comment detection, complex sarcasm classification, or processing multi-lingual inputs without translation.

---

## 3. Dataset & Preprocessing
- **Dataset**: Coursera Course Reviews Dataset (1.45M reviews by Muhammad Nakhaee under GPL 2.0).
- **Features**: Text tokens processed via lowercasing, HTML/URL stripping, space normalization, retaining emoji and negation markers, and padded to a max sequence length of 150.

---

## 4. Evaluation Metrics

Offline validation metrics computed on the test partition:

| Metric | Score | Description |
|---|---|---|
| **Accuracy** | 0.842 | Overall correctly predicted ratio |
| **Macro F1** | 0.824 | Unweighted F1 average (accounts for imbalance) |
| **Weighted F1** | 0.840 | Class-weighted F1 score |
| **Positive class Recall** | 0.891 | Identification rate for Positive reviews |
| **Neutral class Recall** | 0.725 | Identification rate for Neutral reviews |
| **Negative class Recall** | 0.814 | Identification rate for Negative reviews |

---

## 5. Limitations & Ethical Considerations
- **Bias**: The dataset is heavily skewed towards positive reviews (typical of Coursera distributions). Although class weights are applied during training, the model might still exhibit slight positive classification bias.
- **Out of Vocabulary**: Words not present in the 20,000 token vocabulary are mapped to `<OOV>`, which might lead to loss of sentiment nuance for rare vocabulary items.
- **Ethics**: Sentiment scores must not be used to automatically delete student feedback or block instructor access. They are intended strictly for aggregated metrics and sorting purposes.
