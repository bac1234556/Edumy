# Model Design Specification — Course Recommendation

This document specifies the network structures for the collaborative filtering recommendation models.

---

## 1. Popularity Baseline (Non-Personalized)
- **Calculation**: Rank items based on the total count of registrations in the training set.
- **Serving**: Output the top $K$ most popular courses.
- **Use Case**: Serves as the baseline comparative metric and is used to handle cold-start users (users with no prior history).

---

## 2. Generalized Matrix Factorization (GMF)
GMF models linear interactions between user and item embeddings:
- **User Embeddings**: $p_u \in \mathbb{R}^k$ (Latent dimension $k = 16$).
- **Item Embeddings**: $q_i \in \mathbb{R}^k$.
- **Computation**: Element-wise product of embeddings:
  $$\phi^{\text{GMF}} = p_u \odot q_i$$
- **Output Layer**: Single Dense node with a Sigmoid activation:
  $$\hat{y}_{ui} = \sigma(h^T \phi^{\text{GMF}})$$

---

## 3. Neural Matrix Factorization (NeuMF)
NeuMF combines the GMF linear branch and MLP non-linear branch:

```
                  Input: (User, Item)
                   /               \
       GMF Branch                     MLP Branch
       Embeddings: 16                 Embeddings: 32
           |                              |
      Element-wise                    Concatenate
        Product                           |
           |                        Dense(64, ReLU)
           |                              |
           |                        Dense(32, ReLU)
           |                              |
           |                        Dense(16, ReLU)
           \                              /
            \                            /
             Concatenate both branches (16 + 16 = 32)
                            |
                     Dense(1, Sigmoid)
```

- **Loss**: Binary Crossentropy.
- **Optimizer**: Adam.
- **Callbacks**: `EarlyStopping` (patience=5) and `ModelCheckpoint` tracking validation Hit Rate.
