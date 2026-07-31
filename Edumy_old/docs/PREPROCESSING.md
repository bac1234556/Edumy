# Preprocessing Pipeline — Sentiment Analysis

This document describes the exact text cleaning and tokenization steps applied in `03_preprocessing.ipynb` before feeding the inputs to the BiLSTM neural network.

---

## 1. Text Cleaning Steps (Sequential Pipeline)

To prepare raw feedback text for model consumption while retaining sentiment-bearing operators, the pipeline executes the following steps sequentially:

```
Raw Review Text
   │
   ▼
[ Lowercase ] ── Convert all text characters to lowercase
   │
   ▼
[ Unicode Normalize ] ── Clean characters using NKFD normalizations (standardizing accents)
   │
   ▼
[ Remove HTML ] ── Strip tags like `<br />` or `<p>` using regular expressions or BeautifulSoup
   │
   ▼
[ Remove URLs ] ── Strip hyperlinks (`https://...`, `www...`)
   │
   ▼
[ Normalize Spaces ] ── Condense multiple consecutive spaces/newlines into a single space
   │
   ▼
[ Retain Essential Punctuation & Tokens ]
```

---

## 2. Negations & Emojis Retention Policy

> [!IMPORTANT]
> **Crucial Rule**: Standard Stop-word removal filters (such as NLTK's default stop-word list) remove key negation words. This completely reverses sentiment meaning (e.g. *"not bad"* becomes *"bad"*).
>
> We enforce the following retention rules:

### A. Keep Negations
We **never** strip the following negation tokens:
- `not`, `no`, `never`, `none`, `neither`, `nor`
- `don't`, `can't`, `won't`, `shouldn't`, `couldn't`, `wouldn't`, `haven't`, `hasn't`, `isn't`, `aren't`, `wasn't`, `weren't`

### B. Keep Emojis
Emojis (e.g., 😊, 😞, 👍) contain direct sentiment information and must be kept in the text sequence.

### C. Keep Punctuation Needed
We preserve exclamation marks (`!`) and question marks (`?`) as they indicate sentiment intensity and expressions.

---

## 3. Tokenization Strategy

Since the model uses a bidirectional LSTM network, we **do not** use bag-of-words or TF-IDF representations (which discard word order). We utilize:

1. **Vocabulary Indexing**: Fit a Keras `Tokenizer` or `TextVectorization` layer on the cleaned training dataset (limiting vocabulary size to `max_features = 20000`).
2. **Text-to-Sequence Conversion**: Convert text strings to arrays of integer indices.
3. **Padding**: Pad sequences using `pad_sequences(..., maxlen=150, padding='post')` to ensure equal tensor shapes for training.
4. **Tokenizer Serialization**: Export the fitted tokenizer to `models/sentiment_tokenizer.pkl` so that the FastAPI service can perform identical pre-processing on request inputs.
