# Graduation Thesis Readiness Audit

This document reviews the readiness of the ML subsystem for academic defense.

---

## 1. Gaps Identified for Defense

- **Parameter Counts**:
  - *Gap*: The thesis claims BiLSTM and MLP models are fully designed and benchmarked, but parameter counts are not calculated.
  - *Reviewer Question*: "Can you show the exact formula and parameter count of your BiLSTM embedding and hidden recurrent cells?"
  - *Recommendation*: Calculate:
    - BiLSTM parameters: $4 \times (d_{emb} \times d_{hidden} + d_{hidden}^2 + d_{hidden})$ for LSTM cells, multiplied by 2 for bidirectional.

- **HitRate@10 Metric Triviality**:
  - *Gap*: Claiming 100% HitRate@10 on GMF/NeuMF as a breakthrough.
  - *Reviewer Question*: "Since your total course catalog only contains 7 items, is a HitRate@10 metric meaningful?"
  - *Recommendation*: Acknowledge this constraint. Emphasize NDCG@10 or NDCG@3 as the true discriminative ranking metrics.

- **Identity Mapping Logic**:
  - *Gap*: No explanation of how anonymous OULAD demographics translate to real users.
  - *Recommendation*: Clearly define that OULAD is used for offline hyperparameter exploration and cold-start fallback mining, while production utilizes a mapped course identity lookup table.
