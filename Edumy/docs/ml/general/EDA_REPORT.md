# EDA Report & Data Validation Plan — Sentiment Analysis

This document details the exploratory data analysis (EDA) and verification rules that will be executed in `02_eda.ipynb` on the selected Coursera reviews dataset.

---

## 1. Data Validation Checklist

The following structural checks are performed to filter out noisy, invalid, or corrupted text inputs:

| Check | Metric / Target | Action on Failure |
|---|---|---|
| **Missing Values** | Null or empty string cells | Drop row |
| **Duplicates** | Exact identical string entries | Keep first occurrence, drop duplicates |
| **Null/Empty Reviews** | Strings containing only whitespaces | Drop row |
| **HTML Entities** | Presence of `&amp;`, `<br>`, `<div>`, etc. | Flag for removal in preprocessing |
| **URLs** | Presence of `http://`, `https://`, `www.` | Flag for replacement in preprocessing |
| **Extremely Short Reviews** | Text length less than 3 characters | Drop row |
| **Extremely Long Reviews** | Text length greater than 1,000 characters | Truncate or drop row to prevent gradient explosion |

---

## 2. Statistical Distributions Planned

The EDA notebook will compute and plot the following profiles to diagnose data quality:

### A. Rating Distribution
- Visualizes the frequency of ratings from 1 to 5 stars.
- Displays the proportion of positive, neutral, and negative classes.
- Used to verify class imbalances (typical skewness is heavily positive: ~70% positive).

### B. Review Length Distribution
- Tracks word count and character count per review using histograms and box plots.
- Measures the mean, median, standard deviation, and 95th percentile of text lengths.
- Informs the selection of the maximum sequence padding length (e.g. `max_len = 150` or `200` words).

### C. Language Distribution
- Inspects character encodings to ensure only valid UTF-8/Unicode data is processed.
- Filters out non-English character blocks if found in the dataset.

---

## 3. Label Mapping Strategy

As the dataset contains numerical ratings from 1 to 5, we apply the following mapping function:

```python
def convert_rating_to_sentiment(rating: int) -> str:
    """
    Map numerical rating values (1-5) to categorical sentiment classes.
    """
    if rating in [1, 2]:
        return "NEGATIVE"
    elif rating == 3:
        return "NEUTRAL"
    elif rating in [4, 5]:
        return "POSITIVE"
    else:
        raise ValueError(f"Invalid rating value: {rating}")
```

This ensures a balanced, standard mapping strategy to convert explicit numeric ratings to target labels.
