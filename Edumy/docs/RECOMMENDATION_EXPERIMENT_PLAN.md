# Experiment & Evaluation Plan — Recommendation

This document outlines the benchmarking metrics and evaluation procedure to compare recommendation models.

---

## 1. Evaluation Protocol (Leave-One-Out)
For each student, we retrieve their last positive interaction (from the test set) and mix it with **99 randomly selected negative items** (modules the user has never registered for).
We then rank these 100 items using each model.

---

## 2. Target Metrics
- **Hit Rate@10 (HR@10)**: Measures if the true test item is ranked in the top 10 positions:
  $$\text{HR@10} = \begin{cases} 1 & \text{if Rank(true\_item)} \le 10 \\ 0 & \text{otherwise} \end{cases}$$
- **Normalized Discounted Cumulative Gain@10 (NDCG@10)**: Weights the position of the hit:
  $$\text{NDCG@10} = \frac{\ln(2)}{\ln(\text{Rank(true\_item)} + 1)}$$
- **Precision@10 & Recall@10**
- **Mean Average Precision (MAP)**

---

## 3. Comparative Experiments

Once the training dataset is processed, we will run and compare:

| Model ID | Algorithm | HR@10 | NDCG@10 | Latency (ms) |
|---|---|---|---|---|
| **REC-EXP-01** | Popularity Baseline | *TBD* | *TBD* | *TBD* |
| **REC-EXP-02** | GMF | *TBD* | *TBD* | *TBD* |
| **REC-EXP-03** | NeuMF | *TBD* | *TBD* | *TBD* |
