# Machine Learning Documentation Inconsistencies

This report highlights claim discrepancies between repository documents and the actual implementation.

---

## 1. Discrepancies Found

- **BiLSTM Sentiment Model**:
  - *Claim*: `docs/SENTIMENT_PIPELINE.md` claims the sentiment analysis pipeline is fully implemented using an Embedding + BiLSTM model.
  - *Reality*: There are no model weights, no vectorizers, and no executed notebooks. The FastAPI server returns hardcoded mock labels based on simple keyword lookups.

- **SVM/MLP Course Classification**:
  - *Claim*: `docs/COURSE_CLASSIFICATION_PERFORMANCE.md` presents SVM and MLP benchmarking results.
  - *Reality*: The notebooks contain basic skeletons but do not fit or export any models. The FastAPI server returns mock categories.

- **HitRate@10 Evaluation Protocol**:
  - *Claim*: `docs/RECOMMENDATION_MODEL_COMPARISON.md` highlights HR@10 as a key discriminator.
  - *Reality*: The OULAD dataset has only 7 courses. Recommending top-10 items guarantees a 100% HitRate (HR@10 = 1.0) for every user, making it a trivial discriminator. NDCG@10 must be used instead.
