# Current Recommendation State — EduMy

This document reports on the existing recommendation service placeholders and routing logic in the EduMy project.

---

## 1. Backend Integration Details
- **Interface method**: `RecommendCoursesAsync(int userId)` in [`IMachineLearningService`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Services/MachineLearningService.cs).
- **HTTP Gateway Route**: `POST /recommend/courses` on FastAPI.
- **Client implementation**:
  ```csharp
  var response = await _httpClient.PostAsJsonAsync("/recommend/courses", new { user_id = userId });
  ```
- **Controller Endpoint**: `GET /api/courses/recommend` in [`CoursesController.cs`](file:///e:/Edumy%20%281%29/Edumy/Edumy/Backend/Controllers/CoursesController.cs). Requires `Student` role.

---

## 2. ML Service Placeholder
- **File**: `MLService/main.py`
- **Logic**: Returns a static list:
  ```python
  return RecommendResponse(recommendedCourseIds=[1, 2, 3], scores=[0.9, 0.8, 0.7])
  ```
- **Analysis**: A static rule-based fallback return. No real matrix factorization or user activity logs are currently queried.
