# Model Architecture Specification — Sentiment Analysis

This document describes the Bidirectional LSTM (BiLSTM) neural network architecture designed for Sentiment Analysis on online learning course reviews.

---

## 1. Network Topology

The model consists of a word embedding layer, a bidirectional recurrent layer, dropout regularization, and a dense classification head:

```
                  Input Text Sequence (padded to 150 tokens)
                                     │
                                     ▼
                     [ Embedding Layer (Input: 20000) ]
       Converts integer word tokens to 128-dimensional dense vectors.
                                     │
                                     ▼
                        [ SpatialDropout1D (0.2) ]
          Drops entire 1D feature maps to prevent co-adaptation.
                                     │
                                     ▼
                  [ Bidirectional LSTM (128 units) ]
    Forward LSTM (128 units)  <──────────────>  Backward LSTM (128 units)
   Processes from left-to-right                  Processes from right-to-left
                                     │
                 (Concatenation yields 256-dimensional vector)
                                     │
                                     ▼
                        [ GlobalMaxPooling1D ]
           Extracts the most salient features over time steps.
                                     │
                                     ▼
                              [ Dropout (0.5) ]
              Drops hidden units randomly during training.
                                     │
                                     ▼
                          [ Dense (64, ReLU) ]
                             Fully-connected
                                     │
                                     ▼
                              [ Dense (3) ]
                    Logits output for classification
                                     │
                                     ▼
                         [ Softmax Activation ]
           Yields probability distribution over 3 target classes.
```

---

## 2. Hyperparameters & Configuration

| Parameter | Value | Description |
|---|---|---|
| **Max Vocabulary Size** | 20,000 | Number of most frequent tokens retained |
| **Max Sequence Length** | 150 | Input text sequence limit (padded post) |
| **Embedding Dimension** | 128 | Vector dimension representing each word |
| **LSTM Hidden Units** | 128 | State space size of each recurrent cell |
| **Spatial Dropout Rate** | 0.2 | Embedding feature-wise dropout |
| **Recurrent Dropout Rate** | 0.0 (using standard CuDNN) | Speeds up GPU training considerably |
| **Classification Head Dropout** | 0.5 | Dense layer regularization rate |
| **Optimizer** | Adam | Adaptive moment estimation |
| **Learning Rate** | 1e-3 (initial) | Adjusted dynamically via scheduler |
| **Loss Function** | Sparse Categorical Crossentropy | Matches label encoding index |
| **Batch Size** | 64 | Sequence batch size per training step |

---

## 3. Ablation Study & Architecture Selection

To select the optimal network configuration, three architectural configurations were evaluated during the training runs:

| Architecture | Dropout | Hidden Units | Val Loss | Val Macro F1 | Status |
|---|---|---|---|---|---|
| **Config A** | 0.3 | 64 | 0.521 | 0.781 | Candidate |
| **Config B (Selected)** | **0.5** | **128** | **0.478** | **0.824** | **Best Performance** |
| **Config C** | 0.5 | 256 | 0.495 | 0.819 | Overfitting |

- **Selection Justification**: Config B was selected because it achieved the lowest validation loss and highest Macro F1 score. Increasing hidden units to 256 (Config C) led to faster training loss minimization but higher validation loss, indicating overfitting.
