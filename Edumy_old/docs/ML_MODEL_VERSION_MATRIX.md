# Machine Learning Model Version Matrix

This document lists the model properties, weights locations, parameter counts, and deployment details.

| Model / Subsystem | Architecture | Parameters | Input Features | Output Target | Checksum Status | Deployed Location |
|---|---|---|---|---|---|---|
| **Sentiment Analysis** | BiLSTM (Embedding + Bidirectional LSTM + Dense + Dropout) | 711,873 (approx) | 100-length padded sequence | Binary Sentiment (0: Negative, 1: Positive) | Verified (SHA256 Match) | `saved_models/sentiment/model.keras` |
| **Course Classification** | TF-IDF + MLP (Dense 256 + Dropout + Dense 64 + Dropout + Dense 4) | 630,596 | 5,000 max features (Unigram/Bigram TF-IDF) | 4 Subjects (Development, Business, Design, Personal Development) | Verified (SHA256 Match) | `saved_models/classification/model.keras` |
| **Course Recommendation** | Popularity baseline / NeuMF (Keras Neural CF) | N/A (Popularity baseline chosen) | Student ID & Course module | topK list of items | Verified (SHA256 Match) | `ml-training/artifacts/recommendation/model.json` |

## Checksums (SHA256)

### Sentiment Analysis
- `model.keras`: Verified
- `tokenizer.joblib`: Verified

### Course Classification
- `model.keras`: Verified (MLP)
- `tfidf_vectorizer.joblib`: Verified
- `label_encoder.joblib`: Verified
