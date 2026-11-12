# Model Comparison — Course Classification

> [!WARNING]
> **Status**: **BLOCKED**
>
> **Reason**: The raw dataset file (`ml-training/datasets/raw/udemy_courses.csv`) is missing from the local workspace. No model comparison metrics can be computed.

---

## Planned Comparison Metrics

Once the models are successfully trained, they will be benchmarked on the identical test split using the following metrics:

| Metric | Linear SVM | MLP | Better Model |
|---|---|---|---|
| **Accuracy** | *TBD* | *TBD* | *TBD* |
| **Macro Precision** | *TBD* | *TBD* | *TBD* |
| **Macro Recall** | *TBD* | *TBD* | *TBD* |
| **Macro F1** | *TBD* | *TBD* | *TBD* |
| **Weighted F1** | *TBD* | *TBD* | *TBD* |
| **Training Time** | *TBD* | *TBD* | *TBD* |
| **Inference Time (Single)** | *TBD* | *TBD* | *TBD* |
| **Model Size** | *TBD* | *TBD* | *TBD* |

---

## Model Selection Criteria
The production model candidate will be selected based on:
1. **Macro F1**: Evaluation weighting for minority categories.
2. **Inference Latency**: Target latency under 15ms per request.
3. **Model Footprint**: RAM utilization within FastAPI boundaries.
