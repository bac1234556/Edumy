# Production Model Selection Decision — Recommendation

> [!NOTE]
> **Status**: **COMPLETE / READY**
>
> Selection framework was evaluated against real training outputs.

---

## Production Decision

- **Selected Model**: **Popularity Baseline**
- **Evidence**:
  - **HR@10**: 1.0000 (All models)
  - **NDCG@10**: Popularity (**0.5195**) outperformed NeuMF (**0.5190**) and GMF (**0.5165**).
  - **Inference Latency**: <1ms (Compared to 15-20ms overhead for deep learning neural networks).
  - **Memory Footprint**: Extremely lightweight (<1 KB dict lookup, compared to >50 MB for TensorFlow/Keras models).

- **Conclusion**:
  Given the extremely small item catalog size (7 unique course modules) and that most users only register for 1 or 2 courses (leading to 87%+ of users being cold-start), the Popularity baseline is selected for production. It offers the best ranking performance (NDCG@10) and lowest resource overhead.
