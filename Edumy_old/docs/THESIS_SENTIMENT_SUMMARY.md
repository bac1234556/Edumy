# Thesis Summary Report — Sentiment Analysis (BiLSTM)

**Project Title**: EduMy eLearning Platform Integration
**Academic Sub-discipline**: Natural Language Processing / Deep Learning
**Thesis Section**: Chapter IV — Sentiment Classification and System Integration

---

## 1. Abstract & Introduction
This section details the design and deployment of the Sentiment Analysis subsystem within the EduMy eLearning platform. The system classifies textual student reviews to monitor course quality, flag warning comments for instructors, and feed student feedback data to recommendation frameworks.

---

## 2. Methodology & Model Architecture

### A. Preprocessing Pipeline
We employ a custom preprocessing pipeline to clean raw review texts while retaining negation tokens (`not`, `never`, `can't`) and emotional markers (emojis, punctuation) that directly influence semantic meaning.

### B. Recurrent Architecture (BiLSTM)
We construct a Bidirectional LSTM network:
1. **Embedding Layer**: Converts sequence indexes to dense vector spaces.
2. **Bidirectional Recurrent Layer**: Processes tokens in both left-to-right and right-to-left directions, preserving long-range dependencies.
3. **Regularization**: Spatial Dropout and standard Dropout are added to prevent co-adaptation.
4. **Classification Head**: Dense feedforward layer mapped to a Softmax activation predicting three distinct sentiment classes (`Positive`, `Neutral`, `Negative`).

---

## 3. System Integration & Resilience

The integration follows a decoupled, resilient architecture:
- **FastAPI**: Serves the model predictions over HTTP. If the model files fail to load, the server operates in a degraded health state without crashing.
- **Polly Resilience Gateway**: The ASP.NET Core client connects via a Polly policy (exponential retry and circuit breaker). If FastAPI goes offline, the system gracefully degrades: the review is preserved, and its sentiment label is saved as `"Unavailable"`.
- **Database Persistence**: ML outputs are saved directly to the database for historical analytics.

---

## 4. Key Limitations & Future Work
- **Multilingual Support**: Current tokenizers are fit on English reviews only. Future iterations will adopt multilingual encoders (e.g. XLM-RoBERTa) to parse mixed languages without external translation services.
- **Subjunctive Expressions**: Enhancing parser features to recognize conditional clauses ("I wish the course had...") to reduce false positive predictions.
