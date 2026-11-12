# Model Comparison — Course Recommendation

> [!NOTE]
> **Status**: **COMPLETE / READY**
>
> All models (Popularity, GMF, NeuMF) have been evaluated using the leave-one-out protocol on the test split.

---

## Evaluation Metrics Summary

| Model | HR@10 | NDCG@10 | Better Model |
|---|---|---|---|
| **Popularity Baseline** | 1.0000 | 0.5195 | Winner (Tie-breaker) |
| **GMF** | 1.0000 | 0.5165 | - |
| **NeuMF** | 1.0000 | 0.5190 | - |

### Analysis:
- **Hit Rate@10 (HR@10)**: All models achieved 1.0000. Because the database contains only 7 courses, any Top-10 recommendation list will always contain the true positive course, meaning the hit rate is 100% for all models.
- **NDCG@10**: Popularity achieved the highest NDCG@10 of **0.5195**, followed by NeuMF (**0.5190**) and GMF (**0.5165**).
- **Winner**: Popularity was selected as the production candidate due to higher NDCG@10.
