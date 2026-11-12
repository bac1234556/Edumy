# Test Execution Report — Sentiment Analysis

This report documents the test suites and verification results for the Sentiment Analysis integration.

---

## 1. Test Suite Coverage (Critical Paths)

To prevent regression bugs without requiring redundant work, testing focuses on the following critical integration paths:

| Test ID | Path | Type | Input | Expected Output | Status |
|---|---|---|---|---|---|
| **TEST-01** | FastAPI Validation | Unit | `""` (Empty string) | `HTTP 400 Bad Request` | PASS |
| **TEST-02** | FastAPI Validation | Unit | Long text (>5K chars) | `HTTP 400 Bad Request` | PASS |
| **TEST-03** | FastAPI Validation | Unit | Script tags injection | `HTTP 400 Bad Request` | PASS |
| **TEST-04** | API Polarity Contract | Unit | Positive review | `label: "Positive"`, score maps | PASS |
| **TEST-05** | API Polarity Contract | Unit | Negative review | `label: "Negative"`, score maps | PASS |
| **TEST-06** | API Polarity Contract | Unit | Neutral review | `label: "Neutral"`, score maps | PASS |
| **TEST-07** | Polly Error Handlers | Integration | FastAPI service offline | Review saves, Label = `"Unavailable"` | PASS |
| **TEST-08** | DB Persistence | Integration | Review submitted | Record has SentimentLabel | PASS |
| **TEST-09** | React Review Badge | E2E | Rendering | Review card renders badge | PASS |

---

## 2. Mock Contract Test Sample (FastAPI Offline)
Verification of the C# client resilience setup (Polly wait/retry and circuit breaker):

1. **Step**: Stop FastAPI server.
2. **Step**: Submit review via React frontend.
3. **Log Output**:
   ```
   [2026-07-30 02:40:12 WRN] Polly Circuit Breaker triggered on http://localhost:8000/sentiment/analyze
   [2026-07-30 02:40:12 INF] Review saved to database with SentimentLabel = "Unavailable" (HTTP 503 fallback path succeeded)
   ```
4. **Verification**: Checked SQL Server database. The review is present, `SentimentLabel` field is set to `"Unavailable"`, and no data is lost.
