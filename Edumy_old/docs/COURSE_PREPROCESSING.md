# Preprocessing & Vectorization Strategy — Course Classification

This document outlines the text preprocessing pipeline and feature extraction configurations for the Course Classification task.

---

## 1. Preprocessing Pipeline

For each course sample, we combine inputs (Title + Headline if available) and run them through the following cleaning steps:

```
Text Field
   │
   ▼
[ Lowercase ] ── Convert all text characters to lowercase
   │
   ▼
[ Unicode Normalize ] ── Clean characters using NKFD normalizations
   │
   ▼
[ Remove HTML ] ── Strip any tags
   │
   ▼
[ Remove URLs ] ── Remove hyperlinks
   │
   ▼
[ Normalize Spaces ] ── Condense multiple consecutive spaces
```

- **Stemming / Lemmatization**: We **do not** apply stemming or lemmatization. Standard English suffixes help distinguish domains (e.g. *"programming"* vs *"programmer"*).

---

## 2. TF-IDF Vectorization Strategy

To feed text features to the machine learning classifiers, we use scikit-learn's `TfidfVectorizer`:
- **max_features**: `5000` (limiting the vocabulary size to avoid high dimensionality).
- **ngram_range**: `(1, 2)` (extracts both single words and two-word combinations like *"web design"*, *"python programming"*).
- **min_df**: `2` (retains words that appear in at least 2 documents, filtering out typos).
- **max_df**: `0.95` (removes words that appear in more than 95% of documents, filtering out common stop-words).

> [!IMPORTANT]
> **Leakage Prevention**: The `TfidfVectorizer` must be fit strictly on the **training set**. The validation and test sets must be transformed using the fitted vectorizer without refitting.
