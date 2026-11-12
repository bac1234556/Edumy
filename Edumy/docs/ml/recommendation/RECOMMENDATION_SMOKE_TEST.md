# Smoke Test Checklist — Course Recommendation

This checklist verifies the recommendation API behavior:

- **Test case 1: Known User**: Call `POST /recommendations` with user ID 123. Verify it returns 10 recommended courses with positive float scores.
- **Test case 2: Unknown User (Cold Start)**: Call `POST /recommendations` with user ID 99999. Verify it returns popularity-based course recommendations.
- **Test case 3: Degraded State**: Delete model files, restart FastAPI. Call `POST /recommendations` with user ID 123. Verify it falls back to the popularity baseline list instead of throwing an error.
- **Test case 4: Health Check**: Call `GET /recommendation/health`. Verify it returns `status: ok` and `modelLoaded: true` (or `status: degraded` if files are missing).
