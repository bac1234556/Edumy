# Recommendation Training Report

> [!NOTE]
> **Status**: **COMPLETE / READY**
>
> Training completed successfully on CPU mode. Real models, encoders, and metrics have been generated.

---

## Model Specifications

### 1. Preprocessing & Splits
- **Splits**: Leave-One-Out temporal split.
- **Negative Ratio**: 4:1 negative samples generated per positive interaction.
- **Train Size**: 279 interactions
- **Validation Size**: 3,514 interactions
- **Test Size**: 28,755 interactions

### 2. Hyperparameters Config
- **GMF Embedding Dimension**: 16.
- **MLP Embedding Dimension**: 32.
- **MLP Layers**: [64, 32, 16] Dense network nodes.
- **Loss**: Binary Crossentropy.
- **Optimizer**: Adam.
- **Epochs**: 3 epochs.
