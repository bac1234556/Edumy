# Reproducibility Specification — Sentiment Analysis

This document details the exact environment configurations, seeds, and library versions required to replicate the training, evaluation, and serialization steps for the BiLSTM sentiment model.

---

## 1. Random Seed Configuration

To guarantee deterministic weights initialization and sequence splits, the following seeds are configured at the beginning of the notebooks:

```python
import numpy as np
import tensorflow as tf
import random
import os

def set_reproducibility_seeds(seed=42):
    random.seed(seed)
    os.environ['PYTHONHASHSEED'] = str(seed)
    np.random.seed(seed)
    tf.random.set_seed(seed)
    # Enable deterministic operations in TensorFlow
    os.environ['TF_DETERMINISTIC_OPS'] = '1'
    os.environ['TF_CUDNN_DETERMINISTIC'] = '1'

set_reproducibility_seeds(42)
```

---

## 2. Environment Specification

The training and evaluation steps are executed within the following software stack:

- **Python Version**: 3.12.3 (matching local development virtual environment)
- **TensorFlow Version**: 2.16.1 (configured in requirements)
- **CUDA Version**: 12.1 (for GPU acceleration, if available)
- **CuDNN Version**: 8.9

---

## 3. Package Version Manifest

| Library | Version | Role in Pipeline |
|---|---|---|
| `fastapi` | 0.111.0 | Inference API host |
| `pydantic` | 2.7.4 | Contract schema validation |
| `tensorflow` | 2.16.1 | Model training & computation engine |
| `scikit-learn` | 1.5.0 | Split, metrics, and encoder utilities |
| `pandas` | 2.2.2 | CSV dataset processing |
| `numpy` | 1.26.4 | Matrix computation |
| `python-multipart` | 0.0.9 | API media handling |
