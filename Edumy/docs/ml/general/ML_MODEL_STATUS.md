# ML Model Status Report — EduMy

**Date**: 2026-07-30
**Purpose**: Document the exact state of each ML task, distinguishing trained models from baselines/fallbacks.

---

## Summary Matrix

| ID | Bài toán | Thuật toán | Notebook | Model File | Endpoint | Thật/Giả | Ready |
|---|---|---|---|---|---|---|---|
| ML-1 | Sentiment Analysis | LSTM (planned) | ❌ README only | ❌ None | ✅ `/sentiment/analyze` | **FAKE** — keyword counting | ❌ |
| ML-2 | Course Classification | MLP + TF-IDF (planned) | ⚠️ 1 .ipynb exists | ❌ Unverified | ✅ `/classify/course` | **FAKE** — dictionary lookup | ❌ |
| ML-3 | Recommendation | NCF (planned) | ❌ README only | ❌ None | ✅ `/recommend/courses` | **FAKE** — returns `[1,2,3]` | ❌ |

---

## ML-1: Sentiment Analysis (LSTM)

### Current Implementation

```python
# MLService/main.py — BaselineSentimentAnalyzer (actual code)
class BaselineSentimentAnalyzer:
    positive_words = ["great", "excellent", "amazing", "good", "best", "love"]
    negative_words = ["bad", "worst", "terrible", "poor", "hate"]
    
    def analyze(self, text):
        words = text.lower().split()
        pos = len(set(words) & set(self.positive_words))
        neg = len(set(words) & set(self.negative_words))
        if pos > neg: return {"label": "Positive", "score": 0.85}
        elif neg > pos: return {"label": "Negative", "score": 0.15}
        else: return {"label": "Neutral", "score": 0.50}
```

### Where Results Are Used
1. **ReviewsController.CreateReview** → `_mlService.AnalyzeSentimentAsync(review.Comment)` → Stores `SentimentLabel` + `SentimentScore` on Review entity
2. **CoursesController** → Adds review via same flow
3. **InstructorDashboard** → Displays sentiment distribution chart from stored Review data
4. **AdminDashboard** → ML monitoring shows sentiment stats
5. **CourseDetail** → Shows sentiment badge on each review

### What Needs to Be Built
- [ ] LSTM model with embedding layer + bidirectional LSTM
- [ ] Training dataset: Udemy/Coursera reviews (English), ~50K samples
- [ ] 3-class classification: Positive/Neutral/Negative
- [ ] Tokenizer + padding pipeline
- [ ] Export: `.keras` model + tokenizer pickle
- [ ] FastAPI endpoint: Load model, preprocess text, return prediction

### Seed Data Assessment
```csharp
// DataSeeder.cs line 396-414
var sentimentLabels = new[] { "Positive", "Neutral", "Negative" };
var sentimentScores = new[] { 0.95, 0.50, 0.15 };
var comments = new[] {
    "Outstanding content! The instructor is clear...",  // Positive
    "Average course. Good basic info but lacking...",   // Neutral
    "Very disappointed. The audio quality is terrible..." // Negative
};
```
→ Seed labels are **hand-assigned, not from ML**. The pattern (i % 3) cycles: Positive, Neutral, Negative.

---

## ML-2: Course Classification (MLP)

### Current Implementation

```python
# MLService/main.py — BaselineClassifier (actual code)
class BaselineClassifier:
    categories = {
        "Development": ["programming", "coding", "web", "app", "software", ...],
        "Business": ["business", "management", "finance", "marketing", ...],
        "Design": ["design", "ui", "ux", "graphic", "photoshop", ...]
    }
    
    def classify(self, title, description):
        text = f"{title} {description}".lower()
        scores = {}
        for cat, keywords in self.categories.items():
            score = sum(1 for kw in keywords if kw in text)
            scores[cat] = sigmoid(score)  # Custom sigmoid normalization
        best = max(scores, key=scores.get)
        return {"category": best, "confidence": scores[best], "is_fallback": True}
```

### Where Results Are Used
1. **CoursesController.CreateCourse** → If `CategoryId` is null, calls `ClassifyCourseAsync()` → Auto-assigns category
2. **CoursesController.UpdateCourseStatus("Analyzing")** → Runs classification + content analysis
3. **CourseMlAnalysis** table → Stores `PrimaryCategory`, `Confidence`, `QualityScore`, `RiskLevel`
4. **AdminDashboard** → ML monitoring shows analysis history
5. **InstructorDashboard** → Shows "Run AI Analysis" button, quality scores

### What Needs to Be Built
- [ ] TF-IDF vectorizer (max_features=5000) fit on Kaggle Udemy dataset
- [ ] MLP model: Input(5000)→Dense(512)→Dense(256)→Dense(128)→Dense(N_categories, softmax)
- [ ] Linear SVM model (for comparison — required by specification)
- [ ] Resolve category mismatch: Notebook=4 categories vs Backend=8 categories
- [ ] Export: `.keras` model + `tfidf_vectorizer.pkl` + `label_encoder.pkl`
- [ ] FastAPI endpoint: Load models, TF-IDF transform, predict

### Category Mismatch Detail

| # | ML Notebook Categories | Backend Seed Categories |
|---|---|---|
| 1 | Development | Development |
| 2 | Business | Business |
| 3 | Design | Design |
| 4 | Music | Marketing |
| 5 | — | IT & Software |
| 6 | — | Office Productivity |
| 7 | — | Personal Development |
| 8 | — | Photography |

**Decision needed**: Either retrain with 8 categories using augmented dataset, or consolidate backend to 4 categories.

---

## ML-3: Course Recommendation (NCF)

### Current Implementation

```python
# MLService/main.py — Recommendation endpoint (actual code)
@app.post("/recommend/courses")
async def recommend_courses(data: dict):
    user_id = data.get("user_id", 0)
    return {
        "recommendedCourseIds": [1, 2, 3],
        "scores": [0.9, 0.8, 0.7]
    }
```

### Where Results Are Used
1. **CoursesController.GetRecommendedCourses** → Calls `RecommendCoursesAsync(userId)` → Returns list of Course IDs
2. **HomePage.jsx** → `GET /courses/recommend` → Shows "AI Recommended For You" section
3. **CourseCard** → Displays with "AI Match" badge

### What Needs to Be Built
- [ ] Data preparation: user-course interaction matrix from EduMy DB
- [ ] Negative sampling strategy
- [ ] GMF (Generalized Matrix Factorization) branch
- [ ] MLP branch
- [ ] NeuMF (combined model)
- [ ] Popularity baseline for comparison
- [ ] Evaluation: HR@10, NDCG@10
- [ ] Export: `.keras` model + embedding matrix + user/item ID mappings
- [ ] FastAPI endpoint: Load model, generate top-K recommendations
- [ ] Cold-start handling for new users (popularity fallback)

### Data Source Decision
- **Option A**: Use EduMy seed data (15 enrollments, 10 students, 20 courses) — too small
- **Option B**: Use OULAD dataset from Open University — different domain, ID mapping issue
- **Option C**: Generate synthetic data matching EduMy schema — needs careful design
- **Decision needed**: Which data source for training? OULAD for validation + EduMy synthetic for demo is likely best.

---

## Data Source Inventory

### Real Data
| Source | File/Location | Format | Size | Used By |
|---|---|---|---|---|
| Kaggle Udemy Courses | External download | CSV | 3,682 rows | ML-2 Classification |

### Seed Data (Auto-generated at startup)
| Table | Rows | Source | Purpose |
|---|---|---|---|
| Users | 14 (1 admin + 3 instructors + 10 students) | `DataSeeder.cs` | Demo accounts |
| Courses | 20 | `DataSeeder.cs` | Course catalog |
| Categories | 8 | `DataSeeder.cs` | Course taxonomy |
| Enrollments | 15 | `DataSeeder.cs` | Student-course links |
| Reviews | 15 | `DataSeeder.cs` | Review content + fake sentiment |
| Orders | 15 | `DataSeeder.cs` | Purchase records |
| Sections | 40 (2 per course) | `DataSeeder.cs` | Course structure |
| Lessons | 100 (5 per course) | `DataSeeder.cs` | Lesson placeholders |
| Quizzes | 20 (1 per course) | `DataSeeder.cs` | Assessment stubs |

### Missing/Needed Data
| Dataset | Needed For | Size Estimate | Source |
|---|---|---|---|
| Education reviews (English) | ML-1 Sentiment | ~50,000 reviews | Udemy/Coursera reviews from Kaggle |
| Udemy Courses CSV | ML-2 Classification | 3,682 courses | Kaggle `andrewmvd/udemy-courses` |
| User-course interactions | ML-3 Recommendation | ~50,000 interactions | OULAD / Synthetic |

---

## Glossary

| Term | Meaning |
|---|---|
| **Fallback** | Heuristic/keyword-based logic used when no ML model is loaded |
| **Baseline** | Simple rule-based implementation (NOT a trained model) |
| **Hardcoded** | Fixed return values (e.g., `[1,2,3]`) with no computation |
| **Seed data** | Data auto-inserted by `DataSeeder.cs` on first startup |
| **Mock** | Simulated behavior (e.g., `MockPaymentGateway`) |
| **OULAD** | Open University Learning Analytics Dataset |
| **NCF** | Neural Collaborative Filtering |
| **GMF** | Generalized Matrix Factorization |
| **NeuMF** | Neural Matrix Factorization (GMF + MLP combined) |
