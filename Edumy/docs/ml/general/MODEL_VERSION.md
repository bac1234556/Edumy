# Model Versioning & Release Log — Sentiment Analysis

This log tracks release versions, validation performance metrics, and pending changes for the Sentiment Analysis task.

---

## 1. Version Registry

### Version 1.0.0 (Release: 2026-07-30)
- **Model Type**: Bidirectional LSTM
- **Weights File**: `saved_models/sentiment_model.keras`
- **Tokenizer**: `saved_models/tokenizer.pkl` (20,000 vocab size, 150 sequence max length)
- **Label Encoder**: `saved_models/label_encoder.pkl` (`negative`, `neutral`, `positive`)
- **Dataset Version**: Coursera Reviews Dataset (Nakhaee v1, 1.45M samples)
- **Notebook Version**: v1.0.0 (covering notebooks 01 to 06)
- **Git Commit Reference**: `init-sentiment-bilstm`
- **Validation Metrics**:
  - **Accuracy**: 0.842
  - **Macro F1**: 0.824
  - **Weighted F1**: 0.840
- **Known Issues**:
  - Subjunctive sentences (e.g., *"I wish the course had more hands-on exercises"*) can be misclassified as positive due to keywords like *"wish"*.
  - Neutral reviews with numeric rating 3 sometimes drift to negative if they mention small bugs.
- **Future Improvements**:
  - Add contextual self-attention over the BiLSTM hidden states.
  - Implement parsing heuristics for conditional/subjunctive key terms.
