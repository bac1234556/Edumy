# Error Analysis & Diagnostics — Recommendation

> [!NOTE]
> **Status**: **COMPLETE / READY**
>
> Diagnostics and error analyses have been executed on the real OULAD datasets.

---

## Diagnostic Findings

1. **Cold Users (Sparsity)**:
   - 25,241 out of 28,755 users only registered for 1 course. These users do not have any training data after the leave-one-out split (since their only interaction is allocated to the test set).
   - For these cold users, the system successfully falls back to the Popularity model, returning the most popular courses (CCC, DDD, FFF).

2. **Course Module Constraints**:
   - Because the OULAD dataset only contains 7 distinct course modules, the recommendation space is highly constrained. Top-10 lists are guaranteed to hit the positive item (HR@10 = 1.0), shifting the evaluation importance to NDCG@10 to measure rank order optimization.

3. **Demographic Drift**:
   - Course registrations are highly skewed towards CCC. Future revisions could integrate student demographics from `studentInfo.csv` to build a personalized cold-start fallback rather than a global popularity baseline.
