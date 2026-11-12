# Dataset Selection — Course Classification

This document details the selection and validation process for the course classification dataset in the EduMy project.

---

## Evaluation of Candidates

### Candidate 1: Udemy Courses Dataset (Selected)
- **Author**: Larxel (scraped from Chase Willden)
- **Source/URL**: https://www.kaggle.com/datasets/andrewmvd/udemy-courses
- **License**: CC BY 4.0 (or Other specified in description)
- **Size**: 3,682 courses
- **Columns**: `course_title`, `url`, `is_paid`, `price`, `num_subscribers`, `num_reviews`, `num_lectures`, `level`, `content_duration`, `published_timestamp`, `subject`
- **Language**: English
- **Usability**: High. It has explicit categorical labels (`subject`) matching key online learning subjects.
- **Categories**: 4 subjects:
  - `Web Development` (maps to Web domain)
  - `Business Finance` (maps to Business domain)
  - `Graphic Design` (maps to Design domain)
  - `Musical Instruments` (maps to Music domain)

### Candidate 2: Coursera Courses Dataset (Not Selected)
- **Source/URL**: https://www.kaggle.com/datasets/siddharthm135/coursera-course-dataset
- **Reason for Exclusion**: Categories are highly fragmented, and many classes contain extremely few samples (long tail distribution), making classification metrics less stable for comparison.

---

## Category Strategy
We will extract the categories directly from the `subject` column of the dataset:
1. `Web Development`
2. `Business Finance`
3. `Graphic Design`
4. `Musical Instruments`

*Note*: If a category contains too few samples during exploratory data analysis, we will report the statistics in the EDA notebook and avoid arbitrary merging unless documented in the EDA report.
