# Machine Learning API Test Matrix

This matrix documents the discovery and test validation results for all FastAPI MLService endpoints.

---

## 1. API Endpoints Discovery

| Route | Method | Payload | Response Schema | Integration Status | Status |
|---|---|---|---|---|---|
| `/recommendations` | `POST` | `{"userId": int, "topK": int}` | `{"modelVersion": str, "trainingTimestamp": str, "recommendations": []}` | **ASP.NET Core Gateway** | **PASS** |
| `/recommendation/health` | `GET` | None | `{"status": str, "modelLoaded": bool, "modelVersion": str, "uptime": float}` | None | **PASS** |
| `/sentiment/analyze` | `POST` | `{"text": str}` | `{"label": str, "score": float}` | **ASP.NET Core Gateway** | **PASS (Mock)** |
| `/classification/course` | `POST` | `{"title": str, "description": str}` | `{"predictedCategory": str, "confidence": float, ...}` | **ASP.NET Core Gateway** | **PASS (Mock)** |
| `/course/analyze-content` | `POST` | `{"title": str, "description": str}` | `{"tags": [], "is_toxic": bool, "quality_score": float, ...}` | **ASP.NET Core Gateway** | **PASS (Mock)** |

---

## 2. Validation Test Cases

- **Valid Recommendation (Known User `3733`)**: Returns HTTP 200 with 5 recommendations (`CCC`, `DDD`, etc.).
- **Unknown User (Cold Start `99999`)**: Returns HTTP 200 with popularity recommendations.
- **Toxicity Detection (`/course/analyze-content`)**: Tested with keyword "toxic" -> returns `is_toxic: true` and `toxicity_score: 0.95` correctly.
- **Sentiment Keyword triggers**: Tested text containing "great" -> returns `Positive (0.85)` correctly.
- **Empty payload handling**: Handled by Pydantic validation (returns HTTP 422 Unprocessable Entity).
