# Dataset Selection — Sentiment Analysis

This document details the evaluation and selection process for the public sentiment analysis dataset used in the EduMy project.

---

## Evaluation of Candidates

### Candidate 1: Coursera Course Reviews Dataset (Selected)
- **Author**: Muhammad Nakhaee
- **Source/URL**: https://www.kaggle.com/datasets/nadintamer/coursera-questions-and-answers (or similar community mirrors such as `https://www.kaggle.com/datasets/muhammadnakhaee/coursera-course-reviews`)
- **License**: GPL 2.0
- **Size**: 1.45 million samples
- **Columns**: `reviewsText` (text), `rating` (integer 1-5)
- **Language**: English
- **Usability**: High. It has clear rating metrics, a massive collection of genuine student feedback, and is publicly available under an open-source license.

### Candidate 2: Udemy Course Reviews Dataset (Not Selected)
- **Author**: Community contributors (e.g. Chase Willden / Larxel)
- **Source/URL**: https://www.kaggle.com/datasets/andrewmvd/udemy-courses (Only course metadata, not student reviews) or custom review scrapers.
- **License**: Unknown / Other (scraped directly from Udemy).
- **Reason for Exclusion**: Most public Udemy datasets contain only course metadata (titles, pricing) rather than individual review comments. Review datasets are often scraped without clear licenses or permissions, making Candidate 1 a more legally and academically compliant choice.

### Candidate 3: MOOC Reviews Dataset (Not Selected)
- **Reason for Exclusion**: Extremely small sample sizes (typically <10,000 reviews) compared to the Coursera dataset, limiting the representation of language patterns required to train a robust BiLSTM model.

### Candidate 4: Amazon Product Reviews Dataset (Not Selected)
- **Reason for Exclusion**: General e-commerce reviews do not reflect the specific educational domain context, terminology, and feedback patterns characteristic of online course evaluations.

---

## Conclusion & Selection
We select **Candidate 1: Coursera Course Reviews Dataset** because it is a large, domain-specific, public dataset under the GPL 2.0 license. It contains real student learning feedback with numeric ratings (1-5) matching our target sentiment distribution.
